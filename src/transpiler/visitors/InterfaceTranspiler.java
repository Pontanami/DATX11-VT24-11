package transpiler.visitors;

import grammar.gen.ConfluxParser.ProgramContext;
import grammar.gen.ConfluxParser.TypeDeclarationContext;
import grammar.gen.ConfluxParserBaseVisitor;
import grammar.gen.ConfluxParserVisitor;
import java_builder.Code;
import java_builder.InterfaceBuilder;
import java_builder.MethodBuilder;
import transpiler.TranspilerException;
import transpiler.TranspilerState;
import transpiler.tasks.PopulateExtendedInterfacesTask;
import transpiler.tasks.TaskQueue;
import transpiler.tasks.TaskQueue.Priority;
import transpiler.tasks.TranspilerTask;

public class InterfaceTranspiler extends ConfluxParserBaseVisitor<Void> {
    private final TaskQueue taskQueue;
    private final PopulateExtendedInterfacesTask populateInterfaces;
    private final ConfluxParserVisitor<Code> statementTranspiler;

    public InterfaceTranspiler(TaskQueue taskQueue, ConfluxParserVisitor<Code> stmTranspiler) {
        this.taskQueue = taskQueue;
        this.populateInterfaces = new PopulateExtendedInterfacesTask();
        this.statementTranspiler = stmTranspiler;
        taskQueue.addTask(Priority.POPULATE_INTERFACES, populateInterfaces);
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
        InterfaceBuilder interfaceBuilder = new InterfaceBuilder().addModifier("public").setIdentifier(typeId);
        if (ctx.typeExtend() != null) { // if there is an extends clause
            ctx.typeExtend().Identifier().forEach(t -> interfaceBuilder.addExtendedInterface(t.toString()));
        }
        ctx.typeBody().interfaceBlock().methodSignature().forEach(mCtx -> {
                    MethodBuilder method = new MethodBuilder(false).addModifier("public");
                    mCtx.accept(new MethodTranspiler(method, statementTranspiler));
                    interfaceBuilder.addMethod(method);
                }
        );
        populateInterfaces.addInterface(interfaceBuilder);
        taskQueue.addTask(Priority.ADD_INTERFACE, new AddInterfaceTask(interfaceBuilder));
        return null;
    }

    private record AddInterfaceTask(InterfaceBuilder genInterface) implements TranspilerTask {
        @Override
        public void run(TranspilerState state) {
            String typeId = genInterface.getIdentifier().toCode();
            if (state.doesJavaIdExist(typeId)) {
                throw new TranspilerException("Duplicate type id: " + typeId);
            }
            state.addInterface(genInterface);
        }
    }
}
