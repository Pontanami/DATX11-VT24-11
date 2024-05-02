package transpiler.visitors;

import grammar.gen.ConfluxParser.*;
import grammar.gen.ConfluxParserBaseVisitor;
import grammar.gen.ConfluxParserVisitor;
import java_builder.*;
import org.antlr.v4.runtime.ParserRuleContext;
import transpiler.Environment;
import transpiler.TranspilerException;
import transpiler.TranspilerState;
import transpiler.tasks.AssertDecorableTask;
import transpiler.tasks.TaskQueue;
import transpiler.tasks.TranspilerTask;

import java.util.List;

import static transpiler.tasks.TaskQueue.Priority;

public class DecoratorTranspiler extends ConfluxParserBaseVisitor<String> {
    private static final String ABSTRACT_DECORATOR_TYPE_ID = Environment.reservedId("AbstractDecorator");
    private static final String DECORATOR_REF_TYPE_ID = "DecoratorRef";
    private static final String ADD_DECORATOR_METHOD_ID = Environment.reservedId("addDecorator");
    private static final String REMOVE_DECORATOR_METHOD_ID = Environment.reservedId("removeDecorator");
    private static final String DECORATOR_HANDLER_TYPE_ID = Environment.reservedId("DecoratorHandler");
    private static final String DECORATOR_HANDLER_VAR_ID = Environment.reservedId("decoratorHandler");
    private static final String HANDLER_ADD_DECORATOR = "addDecorator";
    private static final String HANDLER_REMOVE_DECORATOR = "removeDecorator";

    private static final String CALL_BASE = Environment.reservedId("getPrevious()") + "." +
                                            Environment.reservedId("invoke");

    public static final String CALL_TOP_DECORATOR = Environment.reservedId("decoratorHandler") +
                                                    ".callTopDecorator";

    private final TaskQueue taskQueue;
    private final ConfluxParserVisitor<Code> stmTranspiler;
    private ConfluxParserVisitor<String> expressionTranspiler;

    private boolean generateClass;
    private String decoratorId;
    private ClassBuilder decoratorClass;

    public DecoratorTranspiler(TaskQueue taskQueue, ConfluxParserVisitor<Code> stmTranspiler) {
        this.taskQueue = taskQueue;
        this.stmTranspiler = stmTranspiler;
    }

    public void setExpressionTranspiler(ConfluxParserVisitor<String> expressionTranspiler) {
        this.expressionTranspiler = expressionTranspiler;
    }

    public void setGenerateClass(boolean generateClass) {
        this.generateClass = generateClass;
    }

    @Override
    public String visitProgram(ProgramContext ctx) {
        if (ctx.typeDeclaration() != null)
            visitTypeDeclaration(ctx.typeDeclaration());
        else if (ctx.decoratorDeclaration() != null)
            visitDecoratorDeclaration(ctx.decoratorDeclaration());
        return defaultResult();
    }

    @Override
    public String visitTypeDeclaration(TypeDeclarationContext ctx) {
        boolean canBeDecorated = ctx.typeModifier().stream().anyMatch(c -> c.DECORABLE() != null);
        if (canBeDecorated) {
            String typeId = ctx.Identifier().getText();

            // add decorator methods to the type interface
            taskQueue.addTask(Priority.ENABLE_TYPE_DECORATION, new EnableTypeDecorationTask(typeId));

            // create the common super type for decorators if the given type
            taskQueue.addTask(Priority.MAKE_DECORATOR_CLASSES, new CreateDecoratorSuperClassTask(typeId));
            if (generateClass) {
                // add wrapper classes for types that can be decorated
                taskQueue.addTask(Priority.MAKE_DECORATOR_CLASSES, new CreateDecoratorWrapperTask(typeId));
            }
        }
        return defaultResult();
    }

    @Override
    public String visitDecoratorDeclaration(DecoratorDeclarationContext ctx) {
        String decoratedType = ctx.typeId().getText();
        decoratorId = ctx.decoratorId().getText();

        decoratorClass = new ClassBuilder()
                .addModifier("public")
                .setIdentifier(Environment.classId(decoratorId))
                .addExtendedClass(decoratorSuperClassId(decoratedType))
                .addImplementedInterface(decoratedType);

        // Make sure type can be decorated
        taskQueue.addTask(Priority.CHECK_DECORABLE, new AssertTypeCanBeDecorated(decoratorId, decoratedType));
        //Add decorator class to transpiler state
        taskQueue.addTask(Priority.ADD_CLASS, new AddClassTask(decoratorClass));
        return visitDecoratorBody(ctx.decoratorBody()); // add methods and fields to decoratorClass
    }

    @Override
    public String visitDecoratorBody(DecoratorBodyContext ctx) {
        if (ctx.attributesBlock() != null) {
            visitAttributesBlock(ctx.attributesBlock());
        }

        if (ctx.methodBlock() != null) {
            visitMethodBlock(ctx.methodBlock());
        }
        return defaultResult();
    }

    @Override
    public String visitAttributesBlock(AttributesBlockContext ctx) {
        ctx.attributeDeclaration().forEach(this::visitAttributeDeclaration);
        return defaultResult();
    }

    @Override
    public String visitAttributeDeclaration(AttributeDeclarationContext ctx) {
        if (ctx.AS() != null) { //TODO: maybe allow auto-generated getters for decorators?
            throw new TranspilerException("Illegal getter generation for decorator '" + decoratorId + "'");
        }
        String field = stmTranspiler.visitDeclaration(ctx.declaration()).toCode();
        decoratorClass.addField("private " + field + ";");
        return defaultResult();
    }

    @Override
    public String visitMethodBlock(MethodBlockContext ctx) {
        ctx.methodDeclaration().forEach(this::visitMethodDeclaration);
        return defaultResult();
    }

    @Override
    public String visitMethodDeclaration(MethodDeclarationContext ctx) {
        //TODO: maybe make method public only if it's from the interface
        MethodBuilder method = new MethodBuilder().addModifier("public");
        MethodTranspiler methodTranspiler = new MethodTranspiler(method, stmTranspiler);
        methodTranspiler.visitMethodDeclaration(ctx);
        decoratorClass.addMethod(method);
        return defaultResult();
    }

    @Override
    public String visitAddDecoratorExpression(AddDecoratorExpressionContext ctx) {
        return "%s.%s(%s.%s(%s))".formatted(
                expressionTranspiler.visitDecoratedObject(ctx.decoratedObject()),
                ADD_DECORATOR_METHOD_ID,
                visitDecoratorId(ctx.decoratorId()),
                expressionTranspiler.visitMethodId(ctx.methodId()),
                ctx.parameterList() == null ? "" : expressionTranspiler.visitParameterList(ctx.parameterList())
        );
    }

    @Override
    public String visitRemoveDecoratorStatement(RemoveDecoratorStatementContext ctx) {
        return "%s.%s(%s);".formatted(
                expressionTranspiler.visitDecoratedObject(ctx.decoratedObject()),
                REMOVE_DECORATOR_METHOD_ID,
                expressionTranspiler.visitDecoratorRef(ctx.decoratorRef())
        );
    }

    @Override
    public String visitDecoratorId(DecoratorIdContext ctx) {
        return Environment.classId(ctx.getText()); //Make the constructor 'visible' from the Conflux source
    }

    @Override
    public String visitBaseCall(BaseCallContext ctx) {
        validateBaseCall(ctx);
        CodeBuilder result = new CodeBuilder().append("super.").append(ctx.Identifier().getText())
                                              .append("(").beginDelimiter(", ");
        if (ctx.parameterList() != null) {
            for (ExpressionContext eCtx : ctx.parameterList().expression()) {
                result.append(expressionTranspiler.visitExpression(eCtx));
            }
        }
        return result.endDelimiter().append(")").toCode();
    }

    private void validateBaseCall(BaseCallContext ctx) {
        ParserRuleContext parent = ctx.getParent();
        while (parent != null) {
            if (parent instanceof DecoratorDeclarationContext) {
                return;
            }
            parent = parent.getParent();
        }
        throw new TranspilerException("Illegal reference to base: keyword 'base' can only be " +
                                      "used inside a decorator declaration");
    }

    @Override
    protected String defaultResult() {
        return "";
    }

    private record CreateDecoratorSuperClassTask(String decoratedTypeId) implements TranspilerTask {
        @Override
        public void run(TranspilerState state) {
            InterfaceBuilder decoratedInterface = state.lookupInterface(decoratedTypeId);
            if (decoratedInterface == null) {
                throw new IllegalStateException("Cannot create wrapper class for interface '" + decoratedTypeId +
                                                "', it's missing in the transpiler state");
            }
            ClassBuilder decorator = new ClassBuilder()
                    .addModifier("public").addModifier("abstract")
                    .setIdentifier(decoratorSuperClassId(decoratedTypeId))
                    .addExtendedClass(ABSTRACT_DECORATOR_TYPE_ID)
                    .addImplementedInterface(decoratedTypeId);
            implementMethods(decoratedInterface, decorator);
            state.addClass(decorator);
        }

        private void implementMethods(InterfaceBuilder baseInterface, ClassBuilder decoratorClass) {
            for (MethodBuilder method : baseInterface.getMethods()) {
                boolean isStatic = method.getModifiers().stream().anyMatch(c -> c.toCode().equals("static"));
                boolean isAddDecoratorMethod = method.getIdentifier().toCode().equals(ADD_DECORATOR_METHOD_ID);
                boolean isRemoveDecoratorMethod = method.getIdentifier().toCode().equals(REMOVE_DECORATOR_METHOD_ID);

                if (!isStatic && !isAddDecoratorMethod && !isRemoveDecoratorMethod) {
                    decoratorClass.addMethod(makeReflectedDelegate(method, CALL_BASE));
                }
            }
        }
    }

    private record EnableTypeDecorationTask(String decoratedTypeId) implements TranspilerTask {
        @Override
        public void run(TranspilerState state) {
            InterfaceBuilder decoratedInterface = state.lookupInterface(decoratedTypeId);
            if (decoratedInterface == null) {
                throw new IllegalStateException("Cannot add decorator method for interface '" + decoratedTypeId +
                                                "', it's missing in the transpiler state");
            }
            decoratedInterface.addMethod(makeAddDecoratorMethod(decoratedTypeId));
            boolean canSuperTypeBeDecorated = false;
            for (Code superId : decoratedInterface.getExtendedInterfaces()) {
                canSuperTypeBeDecorated = canSuperTypeBeDecorated ||
                                          state.lookupSource(superId.toCode()).accept(new CheckDecorable());
            }
            if (!canSuperTypeBeDecorated)
                decoratedInterface.addMethod(makeRemoveDecoratorMethod());
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

            ClassBuilder wrapperClass = new ClassBuilder()
                    .setIdentifier(Environment.decoratorWrapperId(wrappedTypeId))
                    .addImplementedInterface(wrappedTypeId)
                    .addField(makeHandlerField())
                    .addConstructor(makeConstructor());

            wrappedInterface.getMethods().forEach(method -> {
                boolean isAddDecoratorMethod = method.getIdentifier().toCode().equals(ADD_DECORATOR_METHOD_ID);
                boolean isRemoveDecoratorMethod = method.getIdentifier().toCode().equals(REMOVE_DECORATOR_METHOD_ID);
                boolean isStatic = method.getModifiers().stream().anyMatch(c -> c.toCode().equals("static"));

                if (!isStatic && !isAddDecoratorMethod && !isRemoveDecoratorMethod) {
                    wrapperClass.addMethod(makeReflectedDelegate(method, CALL_TOP_DECORATOR));
                }
                if (isAddDecoratorMethod) {
                    MethodBuilder implementation = method.copySignature(false).addModifier("public");
                    implementation.addStatement("return %s.%s(this, %s);".formatted(
                            DECORATOR_HANDLER_VAR_ID,
                            HANDLER_ADD_DECORATOR,
                            implementation.getParameters().get(0).argId()
                    ));
                    wrapperClass.addMethod(implementation);
                }
                if (isRemoveDecoratorMethod) {
                    MethodBuilder implementation = method.copySignature(false).addModifier("public");
                    implementation.addStatement("%s.%s(this, %s);".formatted(
                            DECORATOR_HANDLER_VAR_ID,
                            HANDLER_REMOVE_DECORATOR,
                            implementation.getParameters().get(0).argId()
                    ));
                    wrapperClass.addMethod(implementation);
                }
            });
            state.addClass(wrapperClass);
        }

        private Code makeHandlerField() {
            return new CodeBuilder()
                    .append("private final ")
                    .append(DECORATOR_HANDLER_TYPE_ID).append(" ")
                    .append(DECORATOR_HANDLER_VAR_ID).append(";");
        }

        private MethodBuilder makeConstructor() {
            CodeBuilder stm = new CodeBuilder()
                    .append(DECORATOR_HANDLER_VAR_ID).append(" = new ")
                    .append(DECORATOR_HANDLER_TYPE_ID).append("(base);");
            return new MethodBuilder()
                    .setIdentifier(Environment.decoratorWrapperId(wrappedTypeId))
                    .addParameter(wrappedTypeId, "base")
                    .addStatement(stm);
        }
    }

    private static MethodBuilder makeAddDecoratorMethod(String decoratedTypeId) {
        return new MethodBuilder()
                .addModifier("public").addModifier("default")
                .setReturnType(DECORATOR_REF_TYPE_ID).setIdentifier(ADD_DECORATOR_METHOD_ID)
                .addParameter(decoratorSuperClassId(decoratedTypeId), "decorator")
                .addStatement("throw new AssertionError(\"This should be unreachable\");");
    }

    private static MethodBuilder makeRemoveDecoratorMethod() {
        return new MethodBuilder()
                .addModifier("public").addModifier("default").setReturnType("void")
                .setIdentifier(REMOVE_DECORATOR_METHOD_ID)
                .addParameter(DECORATOR_REF_TYPE_ID, "decoratorRef");
    }

    // Implement the given method by providing reflective arguments to the delegateId
    private static MethodBuilder makeReflectedDelegate(MethodBuilder method, String delegateId) {
        MethodBuilder result = new MethodBuilder()
                .setIdentifier(method.getIdentifier())
                .setReturnType(method.getReturnType());
        method.getModifiers().forEach(result::addModifier);

        String returnType = Environment.boxedId(method.getReturnType().toCode()) + ".class";
        String methodId = '"' + method.getIdentifier().toCode() + '"';
        CodeBuilder paramArray = new CodeBuilder().append("new Class[]{").beginDelimiter(", ");
        CodeBuilder argArray = new CodeBuilder().append("new Object[]{").beginDelimiter(", ");
        method.getParameters().forEach(p -> {
            paramArray.append(p.argType() + ".class");
            argArray.append(p.argId());
            result.addParameter(p);
        });
        paramArray.endDelimiter().append("}");
        argArray.endDelimiter().append("}");

        CodeBuilder stm = new CodeBuilder();
        if (!"void".equals(method.getReturnType().toCode())) {
            stm.append("return ");
        }
        stm.append(delegateId)
           .append("(").beginDelimiter(", ")
           .append(returnType).append(methodId).append(paramArray).append(argArray)
           .endDelimiter().append(");");

        return result.addStatement(stm);
    }

    private static String decoratorSuperClassId(String decoratedType) {
        return Environment.reservedId(decoratedType + "Decorator");
    }

    private record AddClassTask(ClassBuilder toAdd) implements TranspilerTask {
        @Override
        public void run(TranspilerState state) {
            state.addClass(toAdd);
        }
    }

    private static class CheckDecorable extends ConfluxParserBaseVisitor<Boolean> {
        @Override
        public Boolean visitProgram(ProgramContext ctx) {
            return ctx.typeDeclaration() != null && visitTypeDeclaration(ctx.typeDeclaration());
        }
        @Override
        public Boolean visitTypeDeclaration(TypeDeclarationContext ctx) {
            return ctx.typeModifier().stream().anyMatch(c -> c.DECORABLE() != null);
        }
    }

    private record AssertTypeCanBeDecorated(String decoratorId, String typeId) implements TranspilerTask {
        @Override
        public void run(TranspilerState state) {
            ProgramContext ctx = state.lookupSource(typeId);
            if (ctx == null) {
                throw new TranspilerException("Cannot resolve decorated type '" + typeId +
                                              "' in decorator declaration for '" + decoratorId + "'");
            }
            if (ctx.typeDeclaration() == null) {
                throw new TranspilerException("Illegal decorated type '" + typeId +
                                              "' in decorator declaration for '" + decoratorId + "'");
            }
            List<TypeModifierContext> l = ctx.typeDeclaration().typeModifier();
            if (l.stream().anyMatch(c -> c.IMMUTABLE() != null)) {
                throw new TranspilerException("Illegal decorated type '" + typeId +
                                              "' in decorator declaration for '" + decoratorId +
                                              "' (immutable types cannot be decorated)");
            }
            if (l.stream().noneMatch(c -> c.DECORABLE() != null)) {
                throw new TranspilerException("Illegal decorated type '" + typeId +
                                              "' in decorator declaration for '" + decoratorId +
                                              "' (" + typeId + "' isn't decorable)");
            }
        }
    }
}
