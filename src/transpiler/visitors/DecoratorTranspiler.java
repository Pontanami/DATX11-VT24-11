package transpiler.visitors;

//TODO: create decorator class

import grammar.gen.ConfluxParser.*;
import grammar.gen.ConfluxParserBaseVisitor;
import grammar.gen.ConfluxParserVisitor;
import java_builder.*;
import java_builder.MethodBuilder.Parameter;
import org.antlr.v4.runtime.ParserRuleContext;
import transpiler.Environment;
import transpiler.TranspilerException;
import transpiler.TranspilerState;
import transpiler.tasks.TaskQueue;
import transpiler.tasks.TranspilerTask;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static transpiler.tasks.TaskQueue.Priority;

public class DecoratorTranspiler extends ConfluxParserBaseVisitor<Void> {
    private static final String DECORATOR_TYPE_ID = Environment.reservedId("Decorator");
    private static final String DECORATOR_TAG_TYPE_ID = "DecoratorTag";
    private static final String ADD_DECORATOR_METHOD_ID = Environment.reservedId("addDecorator");
    private static final String GET_INSTANCE_METHOD_ID = Environment.reservedId("getDecoratedInstance");
    private static final String DECORATOR_HANDLER_TYPE_ID = Environment.reservedId("DecoratorHandler");
    private static final String DECORATOR_HANDLER_VAR_ID = Environment.reservedId("decoratorHandler");
    private static final String HANDLER_ADD_DECORATOR = "addDecorator";
    private static final String HANDLER_GET_DECORATED = "getTopLevelDecorator";

    private final TaskQueue taskQueue;
    private final ConfluxParserVisitor<Void> conTranspiler;
    private ClassBuilder decoratorClass;

    public DecoratorTranspiler(TaskQueue taskQueue, ConfluxParserVisitor<Void> conTranspiler) {
        this.taskQueue = taskQueue;
        this.conTranspiler = conTranspiler;
    }

    @Override
    public Void visitProgram(ProgramContext ctx) {
        if (ctx.typeDeclaration() != null)
            visitTypeDeclaration(ctx.typeDeclaration());
        else if (ctx.decoratorDeclaration() != null)
            visitDecoratorDeclaration(ctx.decoratorDeclaration());
        return null;
    }

    @Override
    public Void visitTypeDeclaration(TypeDeclarationContext ctx) {
        if (canTypeBeDecorated(ctx)) {
            // add wrapper classes for types that can be decorated
            taskQueue.addTask(Priority.MAKE_DECORATORS, new CreateDecoratorWrapperTask(ctx.Identifier().getText()));
        }
        return null;
    }

    @Override
    public Void visitDecoratorDeclaration(DecoratorDeclarationContext ctx) {
        decoratorClass = new ClassBuilder();
        String decoratorId = ctx.decoratorId().getText();
        decoratorClass.setIdentifier(Environment.classId(decoratorId));

        conTranspiler.visitDecoratorDeclaration(ctx);
        visitDecoratorBody(ctx.decoratorBody());

        taskQueue.addTask(Priority.ADD_CLASS, new AddClassTask(decoratorClass));
        return null;
    }

    @Override
    public Void visitDecoratorBody(DecoratorBodyContext ctx) {
        //TODO: add methods to class builder
        //TODO: add attributes to class builder
        return null;
    }

    private static class CreateDecoratorTask implements TranspilerTask {
        private final String baseInterfaceId;
        private final String declaredDecoratorId;

        private CreateDecoratorTask(String baseInterfaceId, String declaredDecoratorId) {
            this.baseInterfaceId = baseInterfaceId;
            this.declaredDecoratorId = declaredDecoratorId;
        }

        @Override
        public void run(TranspilerState state) {
            InterfaceBuilder baseInterface = state.lookupInterface(baseInterfaceId);
            String classId = Environment.classId(declaredDecoratorId);
            ClassBuilder decoratorClass = state.lookupClass(classId);
            if (decoratorClass == null) {
                throw new IllegalStateException("CreateDecoratorTask: Decorator class '" + classId +
                                                "' wasn't added to the environment");
            }
            if (baseInterface == null) {
                throw new TranspilerException("Cannot resolve decorated type identifier '" + baseInterfaceId +
                                              "' in decorator declaration '" + declaredDecoratorId + "'");
            }

            String superClass = Environment.reservedId("AbstractDecorator") + "<" + baseInterfaceId + ">";
            decoratorClass.addModifier("public");
            decoratorClass.addExtendedClass(superClass);
            decoratorClass.addImplementedInterface(baseInterfaceId);
            decoratorClass.addMethod(makeGetInstanceMethod());
            implementMethods(baseInterface, decoratorClass);
        }

        private MethodBuilder makeGetInstanceMethod() {
            return new MethodBuilder()
                    .addModifier("public")
                    .setReturnType(baseInterfaceId)
                    .setIdentifier(GET_INSTANCE_METHOD_ID)
                    .addStatement("return this;");
        }

        private void implementMethods(InterfaceBuilder baseInterface, ClassBuilder decoratorClass) {
            Set<MethodSignature> implementedMethods = decoratorClass
                    .getMethods().stream().map(MethodSignature::new).collect(Collectors.toSet());

            for (MethodBuilder method : baseInterface.getMethods()) {
                boolean isStatic = method.getModifiers().stream().anyMatch(c -> c.toCode().equals("static"));
                boolean isImplemented = implementedMethods.contains(new MethodSignature(method));

                if (!isStatic && !isImplemented) {
                    decoratorClass.addMethod(makeDelegateMethod(method, Environment.reservedId("getBase()")));
                }
            }
        }
    }

    private record CreateDecoratorWrapperTask(String wrappedTypeId) implements TranspilerTask {
        @Override
        public void run(TranspilerState state) {
            InterfaceBuilder wrappedInterface = state.lookupInterface(wrappedTypeId);
            if (wrappedInterface == null) {
                throw new IllegalStateException("Cannot create wrapper class for interface '" + wrappedTypeId +
                                                "', it's missing in the transpiler state");
            }
            wrappedInterface.addMethod(makeAddDecoratorMethod(true));

            String getDelegate = DECORATOR_HANDLER_VAR_ID + "." + HANDLER_GET_DECORATED + "()";
            ClassBuilder wrapperClass = new ClassBuilder()
                    .setIdentifier(Environment.decoratorWrapperId(wrappedTypeId))
                    .addImplementedInterface(wrappedTypeId)
                    .addField(makeHandlerField())
                    .addConstructor(makeConstructor())
                    .addMethod(makeAddDecoratorMethod(false));
            wrappedInterface.getMethods().forEach(method ->
                    wrapperClass.addMethod(makeDelegateMethod(method, getDelegate))
            );
        }

        private Code makeHandlerField() {
            return new CodeBuilder()
                    .append("private final ")
                    .append(DECORATOR_HANDLER_TYPE_ID).append("<").append(wrappedTypeId).append("> ")
                    .append(DECORATOR_HANDLER_VAR_ID).append(";");
        }

        private MethodBuilder makeConstructor() {
            CodeBuilder stm = new CodeBuilder()
                    .append(DECORATOR_HANDLER_VAR_ID).append(" = new ")
                    .append(DECORATOR_HANDLER_TYPE_ID).append("<>(base);");
            return new MethodBuilder()
                    .setIdentifier(Environment.decoratorWrapperId(wrappedTypeId))
                    .addParameter(wrappedTypeId, "base")
                    .addStatement(stm);
        }

        private MethodBuilder makeAddDecoratorMethod(boolean forInterface) {
            CodeBuilder stm = new CodeBuilder();
            if (forInterface) {
                // should be impossible to trigger if type checking is implemented correctly
                stm.append("throw new UnsupportedOperationException(\"This object cannot be decorated\");");
            } else {
                stm.append("return ").append(DECORATOR_HANDLER_VAR_ID).append(".")
                   .append(HANDLER_ADD_DECORATOR).append("(decorator);");
            }
            MethodBuilder method = new MethodBuilder()
                    .addModifier("public").setReturnType(DECORATOR_TAG_TYPE_ID).setIdentifier(ADD_DECORATOR_METHOD_ID)
                    .addParameter(DECORATOR_TYPE_ID + "<" + wrappedTypeId + ">", "decorator")
                    .addStatement(stm);
            if (forInterface)
                method.addModifier("default");
            return method;

        }
    }

    private static MethodBuilder makeDelegateMethod(MethodBuilder method, String delegateId) {
        String returnType = method.getReturnType().toCode();
        MethodBuilder implementation = new MethodBuilder()
                .addModifier("public")
                .setReturnType(returnType)
                .setIdentifier(method.getIdentifier());
        CodeBuilder delegateCall = new CodeBuilder()
                .beginConditional(!returnType.equals("void")).append("return ").endConditional()
                .append(delegateId)
                .append(".")
                .append(method.getIdentifier())
                .append("(")
                .beginDelimiter(", ");
        method.getParameters().forEach(param -> {
            implementation.addParameter(param);
            delegateCall.append(param.getArgId());
        });

        delegateCall.endDelimiter().append(");");
        return implementation.addStatement(delegateCall);
    }

    private static class MethodSignature {
        private final String identifier;
        private final List<Parameter> parameters;

        MethodSignature(MethodBuilder source) {
            identifier = source.getIdentifier().toCode();
            parameters = List.copyOf(source.getParameters());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MethodSignature that = (MethodSignature) o;
            return Objects.equals(identifier, that.identifier) && Objects.equals(parameters, that.parameters);
        }
        @Override
        public int hashCode() {
            return Objects.hash(identifier, parameters);
        }
    }

    private static boolean canTypeBeDecorated(TypeDeclarationContext ctx) {
        return ctx.typeModifier() == null || ctx.typeModifier().IMMUTABLE() == null;
    }

    private record AddClassTask(ClassBuilder toAdd) implements TranspilerTask {
        @Override
        public void run(TranspilerState state) {
            state.addClass(toAdd);
        }
    }

    // en lösning på att vi har en massa olika visitors som implicit antar att man börjar visita från en viss nod

    static abstract class PartialVisitor<T, C extends ParserRuleContext> extends ConfluxParserBaseVisitor<T> {
        public abstract T visitStartNode(C ctx);
    }

    static class SomeVisitor extends PartialVisitor<Void, ExpressionContext> {
        @Override
        public Void visitStartNode(ExpressionContext ctx) {
            return visitExpression(ctx);
        }
    }
}
