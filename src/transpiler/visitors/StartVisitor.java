package transpiler.visitors;

import grammar.gen.ConfluxParser;
import grammar.gen.ConfluxParserBaseVisitor;
import transpiler.tasks.TaskQueue;

public class StartVisitor extends ConfluxParserBaseVisitor<Void> {
    private TaskQueue taskQueue;
    public StartVisitor(TaskQueue taskQ) {
     this.taskQueue = taskQ;
    }

    @Override
    public Void visitProgram(ConfluxParser.ProgramContext ctx) {
        if (ctx.typeDeclaration() == null) {
            return null;
        }

        ObserverTranspiler observerTranspiler = new ObserverTranspiler(taskQueue);
        //Need to generate a class since only the interface block is mandatory
        if (ctx.typeDeclaration().typeBody().getChildCount() > 1){
            ctx.typeDeclaration().accept(new ClassTranspiler(taskQueue,
                     new StatementTranspiler(observerTranspiler, new ExpressionTranspiler())));
            ctx.accept(new ConstructorTranspiler(taskQueue));
        }
        ctx.accept(observerTranspiler);
        ctx.accept(new InterfaceTranspiler(taskQueue));




        return null;
    }
}
