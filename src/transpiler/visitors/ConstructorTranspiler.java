package transpiler.visitors;

import grammar.gen.ConfluxParser;
import grammar.gen.ConfluxParser.ConstructorDeclarationContext;
import grammar.gen.ConfluxParser.TypeBodyContext;
import grammar.gen.ConfluxParser.TypeDeclarationContext;
import grammar.gen.ConfluxParserBaseVisitor;
import grammar.gen.ConfluxParserVisitor;
import java_builder.*;
import java_builder.MethodBuilder.Parameter;
import transpiler.Environment;
import transpiler.TranspilerException;
import transpiler.TranspilerState;
import transpiler.tasks.TaskQueue;
import transpiler.tasks.TaskQueue.Priority;
import transpiler.tasks.TranspilerTask;

import java.util.*;

public class ConstructorTranspiler extends ConfluxParserBaseVisitor<Void> {
    private final String CONSTRUCTOR_ENUM_ID = Environment.reservedId("ConstructorID");

    private final TaskQueue taskQueue;
    private final ConfluxParserVisitor<String> statementTranspiler;
    private final ConfluxParserVisitor<String> defaultTranspiler;
    private final Set<String> constructorIdClashes;
    private String typeId;
    private String classId;

    public ConstructorTranspiler(TaskQueue taskQueue) {
        this.statementTranspiler = new StatementTranspiler(new ObserverTranspiler(taskQueue));
        this.defaultTranspiler = new DefaultTranspiler();
        this.constructorIdClashes = new HashSet<>();
        this.taskQueue = taskQueue;
    }

    @Override
    public Void visitTypeDeclaration(TypeDeclarationContext ctx) {
        typeId = ctx.Identifier().toString();
        classId = Environment.classId(typeId);
        return ctx.typeBody() == null ? null : visitTypeBody(ctx.typeBody());
    }

    @Override
    public Void visitTypeBody(TypeBodyContext ctx) {
        boolean isSingleton = false;
        List<MethodBuilder> constructors = new ArrayList<>();
        if (ctx.constructorsBlock() == null || ctx.constructorsBlock().constructorDeclaration().isEmpty()) {
            ConfluxParserVisitor<List<Parameter>> visitor = new UninitializedComponentsVisitor();
            List<Parameter> params = ctx.containsBlock() == null ? List.of() : ctx.containsBlock().accept(visitor);
            constructors.add(makeDefaultConstructor(params));
        } else {
            isSingleton = ctx.constructorsBlock().SINGLETON() != null;
            for (ConstructorDeclarationContext declaration : ctx.constructorsBlock().constructorDeclaration()) {
                ConstructorVisitor visitor = new ConstructorVisitor();
                declaration.accept(visitor);
                constructors.add(visitor.constructor);
            }
        }
        createTasks(isSingleton, constructors).forEach(t -> taskQueue.addTask(Priority.MAKE_CONSTRUCTORS, t));
        taskQueue.addTask(Priority.MAKE_CONSTRUCTORS, new ConstructorEnumTask());
        return null;
    }

    // Group all constructors with equal parameters into one task, return the list of tasks
    private List<AddConstructorsTask> createTasks(boolean isSingleton, List<MethodBuilder> constructors) {
        List<AddConstructorsTask> tasks = new ArrayList<>();
        for (MethodBuilder constructor : constructors) {
            boolean matchFound = false;
            for (AddConstructorsTask task : tasks) {
                if (task.parametersEquals(constructor)) {
                    task.addConstructor(constructor);
                    matchFound = true;
                    break;
                }
            }
            if (!matchFound)
                tasks.add(new AddConstructorsTask(constructor, isSingleton));
        }
        return tasks;
    }

    private MethodBuilder makeDefaultConstructor(List<Parameter> components) {
        MethodBuilder method = new MethodBuilder().setIdentifier(Environment.escapeJavaKeyword("new"));
        components.forEach(method::addParameter);
        components.forEach(p -> method.addStatement(new CodeBuilder()
                .append("this.")
                .append(p.getArgId())
                .append(" = ")
                .append(p.getArgId())
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
            constructor.addParameter(ctx.type().accept(defaultTranspiler), ctx.variableId().Identifier().toString());
            return null;
        }
        @Override
        public Void visitStatement(ConfluxParser.StatementContext ctx) {
            constructor.addStatement(ctx.accept(statementTranspiler));
            return null;
        }
    }

    //TODO: the parser needs to be updated before this is implemented
    private static class UninitializedComponentsVisitor extends ConfluxParserBaseVisitor<List<Parameter>> {
        @Override
        public List<Parameter> visitContainsBlock(ConfluxParser.ContainsBlockContext ctx) {
            return List.of();
        }
    }

    private class AddConstructorsTask implements TranspilerTask {
        private final String FACTORY_ID_PARAM = "factoryId";

        private final List<Parameter> parameters;
        private final Map<String, List<Code>> constructors;
        private final boolean isSingleton;

        AddConstructorsTask(MethodBuilder constructor, boolean isSingleton) {
            this.parameters = new ArrayList<>(constructor.getParameters());
            if (!parameters.isEmpty() && isSingleton)
                throw new TranspilerException("Singleton constructor cannot have parameters");
            this.constructors = new TreeMap<>();
            this.isSingleton = isSingleton;
            addConstructor(constructor);
        }

        void addConstructor(MethodBuilder constructor) {
            constructors.put(constructor.getIdentifier().toCode(), constructor.getStatements());
        }

        boolean parametersEquals(MethodBuilder constructor) {
            return constructor.getParameters().equals(this.parameters);
        }

        @Override
        public void run(TranspilerState state) {
            InterfaceBuilder interfaceBuilder = state.lookupInterface(typeId);
            ClassBuilder classBuilder = state.lookupClass(classId);

            MethodBuilder classConstructor = new MethodBuilder().setIdentifier(classId);
            if (constructors.size() > 1) {//add parameter for the factory, if there are more than one for this constructor
                classConstructor.addParameter(CONSTRUCTOR_ENUM_ID, FACTORY_ID_PARAM);
            }
            parameters.forEach(classConstructor::addParameter);

            CodeBuilder constructorStatement = new CodeBuilder();
            constructors.forEach((factoryId, statements) -> {
                String singletonId = Environment.reservedId(factoryId + "Singleton");
                factoryId = Environment.escapeJavaKeyword(factoryId);
                if (isSingleton) {
                    interfaceBuilder.addMethod(makeInterfaceSingletonFactoryMethod(factoryId));
                    classBuilder.addMethod(makeClassSingletonFactoryMethod(factoryId, singletonId));
                    classBuilder.addField(makeSingletonVar(singletonId));
                } else {
                    interfaceBuilder.addMethod(makeFactoryMethod(factoryId));
                }
                if (constructors.size() > 1) {
                    addConstructorIfBranch(factoryId, statements, constructorStatement);
                    constructorIdClashes.add(factoryId);
                } else {
                    constructorStatement.appendLine(0, statements);
                }
            });
            if (constructors.size() > 1) {
                String e = "throw new RuntimeException(\"Unhandled factory method: \" + %s);".formatted(FACTORY_ID_PARAM);
                constructorStatement.newLine().append(e);
            }
            classConstructor.addStatement(constructorStatement);
            classBuilder.addConstructor(classConstructor);
        }

        private Code makeSingletonVar(String singletonId) {
            return new CodeBuilder()
                    .append("private static ")
                    .append(classId)
                    .append(" ")
                    .append(singletonId)
                    .append(";");
        }

        private MethodBuilder makeFactoryMethod(String factoryId) {
            MethodBuilder factoryMethod = new MethodBuilder()
                    .addModifier("static")
                    .setReturnType(typeId)
                    .setIdentifier(factoryId);
            parameters.forEach(factoryMethod::addParameter);

            CodeBuilder factoryMethodReturnStm = new CodeBuilder()
                    .append("return new ")
                    .append(classId)
                    .append("(")
                    .beginDelimiter(", ");
            if (constructors.size() > 1) //pass the name of the factory, if there are more than one for this constructor
                factoryMethodReturnStm.append(classId + "." + CONSTRUCTOR_ENUM_ID + "." + factoryId);
            parameters.forEach(p -> factoryMethodReturnStm.append(p.getArgId()));
            factoryMethodReturnStm.endDelimiter().append(");");

            factoryMethod.addStatement(factoryMethodReturnStm);
            return factoryMethod;
        }

        private MethodBuilder makeClassSingletonFactoryMethod(String factoryId, String singletonId) {
            return new MethodBuilder()
                    .addModifier("static")
                    .setReturnType(typeId)
                    .setIdentifier(factoryId)
                    .addStatement(new CodeBuilder()
                            .append("return ")
                            .append(singletonId)
                            .append(" == null ? ")
                            .append(singletonId)
                            .append(" = new ")
                            .append(classId)
                            .append("(")
                            .beginConditional(constructors.size() > 1)
                            .append(CONSTRUCTOR_ENUM_ID).append(".").append(factoryId)
                            .endConditional()
                            .append(") : ")
                            .append(singletonId)
                            .append(";")
                    );
        }

        private MethodBuilder makeInterfaceSingletonFactoryMethod(String factoryId) {
            return new MethodBuilder()
                    .addModifier("static")
                    .setReturnType(typeId)
                    .setIdentifier(factoryId)
                    .addStatement(new CodeBuilder()
                            .append("return ")
                            .append(classId)
                            .append(".")
                            .append(factoryId)
                            .append("();"));
        }

        private void addConstructorIfBranch(String factoryId, List<Code> statements, CodeBuilder ifStatement) {
            if (!ifStatement.isEmpty()) {
                ifStatement.append(" else ");
            }
            ifStatement.append("if (")
                       .append(FACTORY_ID_PARAM)
                       .append(" == ")
                       .append(CONSTRUCTOR_ENUM_ID)
                       .append(".")
                       .append(factoryId)
                       .append(") {")
                       .appendLine(1, statements)
                       .newLine().append("}");
        }
    }

    private class ConstructorEnumTask implements TranspilerTask {
        @Override
        public void run(TranspilerState state) {
            if (constructorIdClashes.isEmpty()) return;
            CodeBuilder enumBuilder = new CodeBuilder()
                    .append("enum ")
                    .append(CONSTRUCTOR_ENUM_ID)
                    .append(" { ")
                    .beginDelimiter(", ");
            constructorIdClashes.forEach(enumBuilder::append);
            enumBuilder.append(" }");
            state.lookupClass(classId).addField(enumBuilder);
        }
    }
}
