package transpiler.visitors;

import grammar.gen.ConfluxParser.ProgramContext;
import grammar.gen.ConfluxParser.TypeDeclarationContext;
import grammar.gen.ConfluxParserBaseVisitor;
import grammar.gen.ConfluxParserVisitor;
import java_builder.Code;
import java_builder.InterfaceBuilder;
import java_builder.MethodBuilder;
import transpiler.TranspilerException;
import transpiler.tasks.TaskQueue;
import transpiler.tasks.TaskQueue.Priority;

public class InterfaceTranspiler extends ConfluxParserBaseVisitor<Void> {
    private final TaskQueue taskQueue;
    private ConfluxParserVisitor<Code> statementTranspiler;

    public InterfaceTranspiler(TaskQueue taskQueue, ConfluxParserVisitor<Code> stmTranspiler) {
        this.taskQueue = taskQueue;
        this.statementTranspiler = stmTranspiler;
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
        ctx.typeBody().interfaceBlock().methodSignature().forEach(m -> {
                    MethodBuilder mB = new MethodBuilder(false);
                    m.accept(new MethodTranspiler(mB, statementTranspiler));
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
