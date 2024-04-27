package transpiler.visitors;

import grammar.gen.*;
import grammar.gen.ConfluxParser.*;
import java_builder.*;
import java_builder.MethodBuilder.Parameter;
import org.antlr.v4.runtime.tree.TerminalNode;
import transpiler.Environment;
import transpiler.TranspilerException;
import transpiler.TranspilerState;
import transpiler.tasks.TaskQueue;
import transpiler.tasks.TaskQueue.Priority;
import transpiler.tasks.TranspilerTask;

import java.util.*;

public class ConstructorTranspiler extends ConfluxParserBaseVisitor<Void> {
    private final TaskQueue taskQueue;
    private final ConfluxParserVisitor<Code> statementTranspiler;
    private String typeId;
    private String classId;
    private boolean canBeDecorated;

    public ConstructorTranspiler(TaskQueue taskQueue, ConfluxParserVisitor<Code> stmTranspiler) {
        this.statementTranspiler = stmTranspiler;
        this.taskQueue = taskQueue;
    }

    @Override
    public Void visitTypeDeclaration(TypeDeclarationContext ctx) {
        typeId = ctx.Identifier().getText();
        classId = Environment.classId(typeId);
        canBeDecorated = true;
        if (ctx.typeModifier() != null) {
            visitChildren(ctx.typeModifier());
        }
        return ctx.typeBody() == null ? null : visitTypeBody(ctx.typeBody());
    }

    @Override
    public Void visitTerminal(TerminalNode node) {
        if (node.getSymbol().getType() == ConfluxLexer.IMMUTABLE) {
            canBeDecorated = false;
        }
        return null;
    }
    @Override
    public Void visitTypeBody(TypeBodyContext ctx) {
        if (ctx.constructorsBlock() == null || ctx.constructorsBlock().constructorDeclaration().isEmpty()) {
            List<Parameter> params = List.of();
            if (ctx.componentsBlock() != null) {
                UninitializedComponentsVisitor visitor = new UninitializedComponentsVisitor();
                ctx.componentsBlock().accept(visitor);
                params = visitor.uninitialized;
            }
            var task = new AddConstructorsTask(typeId, classId, makeDefaultConstructor(params), false, canBeDecorated);
            taskQueue.addTask(Priority.MAKE_CONSTRUCTORS, task);
        } else {
            boolean singleton = ctx.constructorsBlock().SINGLETON() != null;
            for (ConstructorDeclarationContext declaration : ctx.constructorsBlock().constructorDeclaration()) {
                ConstructorVisitor visitor = new ConstructorVisitor();
                declaration.accept(visitor);
                var task = new AddConstructorsTask(typeId, classId, visitor.constructor, singleton, canBeDecorated);
                taskQueue.addTask(Priority.MAKE_CONSTRUCTORS, task);
            }
        }
        return null;
    }

    @Override
    public Void visitDecoratorDeclaration(DecoratorDeclarationContext ctx) {
        typeId = null;
        classId = ctx.decoratorId().getText();
        return visitDecoratorBody(ctx.decoratorBody());
    }

    @Override
    public Void visitDecoratorBody(DecoratorBodyContext ctx) {
        ConstructorsBlockContext conBlock = ctx.constructorsBlock();
        if (conBlock != null && conBlock.SINGLETON() != null) {
            throw new TranspilerException("Illegal constructor modifier for decorator " + classId +
                                          ": decorators cannot have singleton constructors");
        }
        classId = Environment.classId(classId);
        if (conBlock == null || conBlock.constructorDeclaration().isEmpty()) {
            var task = new AddConstructorsTask(typeId, classId, makeDefaultConstructor(List.of()), false, canBeDecorated);
            taskQueue.addTask(Priority.MAKE_CONSTRUCTORS, task);
        } else {
            for (ConstructorDeclarationContext declaration : conBlock.constructorDeclaration()) {
                ConstructorVisitor visitor = new ConstructorVisitor();
                declaration.accept(visitor);
                var task = new AddConstructorsTask(typeId, classId, visitor.constructor, false, canBeDecorated);
                taskQueue.addTask(Priority.MAKE_CONSTRUCTORS, task);
            }
        }
        return null;
    }

    private MethodBuilder makeDefaultConstructor(List<Parameter> components) {
        MethodBuilder method = new MethodBuilder().setIdentifier("new");
        components.forEach(method::addParameter);
        components.forEach(p -> method.addStatement(new CodeBuilder()
                .append("this.")
                .append(p.argId())
                .append(" = ")
                .append(p.argId())
                .append(";")
        ));
        return method;
    }

    // Get the identifier, parameters and statements for one constructor
    private class ConstructorVisitor extends ConfluxParserBaseVisitor<Void> {
        private final MethodBuilder constructor = new MethodBuilder();
        @Override
        public Void visitConstructorDeclaration(ConstructorDeclarationContext ctx) {
            constructor.setIdentifier(ctx.Identifier().toString());
            return visitChildren(ctx);
        }
        @Override
        public Void visitVariable(ConfluxParser.VariableContext ctx) {
            constructor.addParameter(ctx.type().getText(), ctx.variableId().Identifier().getText());
            return null;
        }
        @Override
        public Void visitStatement(ConfluxParser.StatementContext ctx) {
            constructor.addStatement(ctx.accept(statementTranspiler));
            return null;
        }
    }

    // Get all components that aren't initialized at declaration
    private static class UninitializedComponentsVisitor extends ConfluxParserBaseVisitor<Void> {
        private final List<Parameter> uninitialized = new ArrayList<>();
        @Override
        public Void visitCompositeDeclaration(ConfluxParser.CompositeDeclarationContext ctx) {
            return null; // don't go deeper into the parse tree
        }
        @Override
        public Void visitAggregateDeclaration(ConfluxParser.AggregateDeclarationContext ctx) {
            return visitDeclarationNoAssign(ctx.declarationNoAssign());
        }
        @Override
        public Void visitDeclarationNoAssign(ConfluxParser.DeclarationNoAssignContext ctx) {
            String type = ctx.type().getText();
            ctx.Identifier().forEach(id -> uninitialized.add(new Parameter(type, id.getText())));
            return null;
        }
    }

    private static class AddConstructorsTask implements TranspilerTask {
        private final String constructorEnumId = Environment.unusedIdentifier();//id for resolving constructor signatures
        private final String constructorEnumType; // type for resolving constructor signatures

        private final String typeId;
        private final String classId;
        private final String factoryId;
        private final List<Code> constructorArgs;
        private final boolean isSingleton;
        private final String singletonId;
        private final boolean canBeDecorated;
        private final MethodBuilder constructor;

        AddConstructorsTask(String typeId, String classId, MethodBuilder method, boolean isSingle, boolean decorated) {
            List<Parameter> params = method.getParameters();
            if (!params.isEmpty() && isSingle)
                throw new TranspilerException("Singleton constructor cannot have parameters");

            this.typeId = typeId;
            this.classId = classId;
            this.factoryId = Environment.escapeJavaKeyword(method.getIdentifier().toCode());
            this.constructorEnumType = Environment.reservedId(
                    "ConstructorId" +
                    Character.toUpperCase(factoryId.charAt(0)) +
                    factoryId.substring(1));
            this.constructorArgs = params.stream().map(Parameter::argId).map(Code::fromString).toList();
            this.isSingleton = isSingle;
            this.singletonId = Environment.reservedId(factoryId + "Singleton");
            this.canBeDecorated = decorated;
            this.constructor = method;
            assert !canBeDecorated || typeId != null
                    : "Cannot create constructor for class with decorator wrapper without an interface id";
        }

        @Override
        public void run(TranspilerState state) {
            InterfaceBuilder interfaceBuilder = state.lookupInterface(typeId);
            ClassBuilder classBuilder = state.lookupClass(classId);

            if (interfaceBuilder != null)
                interfaceBuilder.addMethod(makeInterfaceFactoryMethod());

            classBuilder.addField(makeConstructorEnum());
            classBuilder.addConstructor(makeClassConstructor());
            classBuilder.addMethod(makeClassFactoryMethod());
            if (isSingleton) {
                classBuilder.addField(makeSingletonVar());
            }
        }

        private MethodBuilder makeClassConstructor() {
            MethodBuilder classConstructor = new MethodBuilder().addParameter(constructorEnumType, constructorEnumId);
            constructor.getParameters().forEach(classConstructor::addParameter);
            constructor.getStatements().forEach(classConstructor::addStatement);
            return classConstructor.addModifier("private").setIdentifier(classId);
        }

        private Code makeSingletonVar() {
            return new CodeBuilder().append("private static ").append(typeId == null ? classId : typeId)
                    .append(" ").append(singletonId).append(";");
        }

        private Code makeConstructorEnum() {
            return Code.fromString("private enum " + constructorEnumType +
                                   " { " + Environment.unusedIdentifier() + " }");
        }

        private MethodBuilder makeClassFactoryMethod() {
            MethodBuilder factory = new MethodBuilder();
            constructor.getParameters().forEach(factory::addParameter);
            String wrapper = Environment.decoratorWrapperId(typeId);
            CodeBuilder returnStm;
            if (isSingleton) {
                returnStm = new CodeBuilder()
                        .append("return ").append(singletonId).append(" == null ? ")
                        .append(singletonId).append(" = ")
                        .beginConditional(canBeDecorated).append("new ").append(wrapper).append("(").endConditional()
                        .append("new ").append(classId).append("(").append(constructorEnumType).append(".__)")
                        .beginConditional(canBeDecorated).append(")").endConditional()
                        .append(" : ").append(singletonId).append(";");
            } else {
                returnStm = new CodeBuilder()
                        .append("return ")
                        .beginConditional(canBeDecorated).append("new ").append(wrapper).append("(").endConditional()
                        .append("new ").append(classId).append("(")
                        .beginDelimiter(", ").append(constructorEnumType + ".__")
                        .append(constructorArgs).endDelimiter().append(")")
                        .beginConditional(canBeDecorated).append(")").endConditional()
                        .append(";");
            }
            String retType = typeId == null ? classId : typeId;
            return factory.addModifier("static").setReturnType(retType).setIdentifier(factoryId).addStatement(returnStm);
        }

        private MethodBuilder makeInterfaceFactoryMethod() {
            MethodBuilder factory = new MethodBuilder();
            constructor.getParameters().forEach(factory::addParameter);

            CodeBuilder stm = new CodeBuilder()
                    .append("return ")
                    .append(classId).append(".").append(factoryId).append("(")
                    .beginDelimiter(", ").append(constructorArgs).endDelimiter()
                    .append(");");
            return factory.addModifier("static").setReturnType(typeId).setIdentifier(factoryId).addStatement(stm);
        }
    }
}
