package transpiler.visitors;

import grammar.gen.ConfluxParser;
import grammar.gen.ConfluxParserBaseVisitor;
import grammar.gen.ConfluxParserVisitor;
import java_builder.Code;
import java_builder.MethodBuilder;
import transpiler.Environment;
import transpiler.TranspilerException;
import transpiler.TranspilerState;
import transpiler.tasks.TaskQueue;
import transpiler.tasks.TranspilerTask;

import static grammar.gen.ConfluxParser.*;

public class StartVisitor extends ConfluxParserBaseVisitor<Void> {
    private final TaskQueue taskQ;
    private final ClassTranspiler classTranspiler;
    private final InterfaceTranspiler interfaceTranspiler;
    private final ConstructorTranspiler constructorTranspiler;
    private final ObserverTranspiler observerTranspiler;
    private final DecoratorTranspiler decoratorTranspiler;
    private final StatementTranspiler statementTranspiler;

    private String typeFileName;
    private boolean generateClass;

    public StartVisitor(TaskQueue taskQ) {
        this.taskQ = taskQ;
        ExpressionTranspiler expTranspiler = new ExpressionTranspiler();

        observerTranspiler = new ObserverTranspiler(taskQ);
        statementTranspiler = new StatementTranspiler(observerTranspiler, expTranspiler);
        decoratorTranspiler = new DecoratorTranspiler(taskQ, statementTranspiler);
        constructorTranspiler = new ConstructorTranspiler(taskQ, statementTranspiler);
        classTranspiler = new ClassTranspiler(taskQ, statementTranspiler);
        interfaceTranspiler = new InterfaceTranspiler(taskQ, statementTranspiler);

        expTranspiler.setDecoratorTranspiler(decoratorTranspiler);
        expTranspiler.setObserverTranspiler(observerTranspiler);
        observerTranspiler.setExpressionTranspiler(expTranspiler);
        decoratorTranspiler.setExpressionTranspiler(expTranspiler);
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
        String typeId = ctx.Identifier().getText();
        if (!typeId.equals(typeFileName)) {
            throw new TranspilerException("Type identifier must be the same as the file name");
        }

        ctx.accept(interfaceTranspiler);
        ctx.accept(observerTranspiler);

        generateClass = generateClass(ctx.typeBody());
        if (generateClass) {
            ctx.accept(classTranspiler);
            ctx.accept(constructorTranspiler);
            ctx.accept(decoratorTranspiler); // Generate decorator wrapper class
        }
        if (ctx.typeBody().mainBlock() != null) {
            visitMainBlock(ctx.typeBody().mainBlock());
        }
        return null;
    }

    @Override
    public Void visitDecoratorDeclaration(DecoratorDeclarationContext ctx) {
        constructorTranspiler.visitDecoratorDeclaration(ctx);
        decoratorTranspiler.visitDecoratorDeclaration(ctx);
        return null;
    }

    @Override
    public Void visitMainBlock(ConfluxParser.MainBlockContext ctx) {
        MethodBuilder main = new MethodBuilder()
                .addModifier("public").addModifier("static").setReturnType("void").setIdentifier("main");
        if (!ctx.type().getText().equals("String[]")) {
            throw new TranspilerException("Parameter to main must have type String[]");
        }
        main.addParameter("String[]", ctx.Identifier().getText());
        ctx.statement().forEach(stm -> main.addStatement(statementTranspiler.visitStatement(stm)));

        String mainId = generateClass ? Environment.classId(typeFileName) : typeFileName;

        taskQ.addTask(TaskQueue.Priority.ADD_MAIN_CLASS, new AddMainTask(mainId, generateClass, main));
        return null;
    }

    private static class AddMainTask implements TranspilerTask {
        private final String mainId;
        private final boolean mainIsInClass;
        private final MethodBuilder mainMethod;

        private AddMainTask(String mainId, boolean mainIsInClass, MethodBuilder mainMethod) {
            this.mainId = mainId;
            this.mainIsInClass = mainIsInClass;
            this.mainMethod = mainMethod;
        }
        @Override
        public void run(TranspilerState state) {
            state.setMainClassId(mainId);
            if (mainIsInClass) {
                state.lookupClass(mainId).addMethod(mainMethod);
            } else {
                state.lookupInterface(mainId).addMethod(mainMethod);
            }
        }
    }

    // return true if a class should be generated
    private boolean generateClass(TypeBodyContext ctx) {
        return ctx.constructorsBlock() != null || ctx.componentsBlock() != null
               || ctx.attributesBlock() != null || ctx.methodBlock() != null;
    }

    public void setTypeFileName(String typeFileName) {
        this.typeFileName = typeFileName;
    }
}
