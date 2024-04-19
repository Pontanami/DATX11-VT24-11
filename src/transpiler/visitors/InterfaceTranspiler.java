package transpiler.visitors;

import grammar.gen.ConfluxParser.ProgramContext;
import grammar.gen.ConfluxParser.TypeDeclarationContext;
import grammar.gen.ConfluxParserBaseVisitor;
import grammar.gen.ConfluxParserVisitor;
import java_builder.InterfaceBuilder;
import java_builder.MethodBuilder;
import transpiler.TranspilerException;
import transpiler.tasks.TaskQueue;
import transpiler.tasks.TaskQueue.Priority;

public class InterfaceTranspiler extends ConfluxParserBaseVisitor<Void> {
    private final ConfluxParserVisitor<String> observerTranspiler;
    private final TaskQueue taskQueue;

    public InterfaceTranspiler(TaskQueue taskQueue) {
        this.observerTranspiler = new ObserverTranspiler(taskQueue);
        this.taskQueue = taskQueue;
    }

    @Override
    public Void visitProgram(ProgramContext ctx) {
        if (ctx.typeDeclaration() != null) {
            return visitTypeDeclaration(ctx.typeDeclaration());
        }
        return null;
    }

    @Override
    public Void visitTypeDeclaration(TypeDeclarationContext ctx) {
        String typeId = ctx.Identifier().toString();
        InterfaceBuilder interfaceBuilder = new InterfaceBuilder();
        interfaceBuilder.setIdentifier(typeId);
        if (ctx.typeExtend() != null) { // if there is an extends clause
            ctx.typeExtend().Identifier().forEach(t -> interfaceBuilder.addExtendedInterface(t.toString()));
        }
        if (ctx.typePublishes() != null) { // if there is a publishes clause
            observerTranspiler.visitTypePublishes(ctx.typePublishes());
        }
        ctx.typeBody().interfaceBlock().methodSignature().forEach(m -> {
                    MethodBuilder mB = new MethodBuilder();
                    m.accept(new MethodTranspiler(mB, new StatementTranspiler(observerTranspiler, new ExpressionTranspiler())));
                    interfaceBuilder.addMethod(mB);
                }
        );
        taskQueue.addTask(Priority.ADD_INTERFACE, state -> {
            if (state.doesJavaIdExist(typeId)) {
                throw new TranspilerException("Duplicate type id: " + typeId);
            }
            state.addInterface(interfaceBuilder);
        });
        return null;
    }
}
