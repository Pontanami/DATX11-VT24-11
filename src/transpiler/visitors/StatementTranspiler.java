package transpiler.visitors;

import grammar.gen.ConfluxParser;
import grammar.gen.ConfluxParserBaseVisitor;
import grammar.gen.ConfluxParserVisitor;
import java_builder.Code;

public class StatementTranspiler extends DefaultTranspiler {
    private final ConfluxParserVisitor<String> observerTranspiler;
    private final ConfluxParserVisitor<String> defaultTranspiler = new DefaultTranspiler();

    public StatementTranspiler(ConfluxParserVisitor<String> observerTranspiler) {
        this.observerTranspiler = observerTranspiler;
    }
    @Override
    public String visitPublishStatement(ConfluxParser.PublishStatementContext ctx) {
        return observerTranspiler.visitPublishStatement(ctx);
    }
    @Override
    public String visitAddSubscriberStatement(ConfluxParser.AddSubscriberStatementContext ctx) {
        return observerTranspiler.visitAddSubscriberStatement(ctx);
    }
    @Override
    public String visitRemoveSubscriberStatement(ConfluxParser.RemoveSubscriberStatementContext ctx) {
        return observerTranspiler.visitRemoveSubscriberStatement(ctx);
    }

    /*    @Override
    public String visitStatement(ConfluxParser.StatementContext ctx) {
        if (ctx.javaStatement() != null) {
            return defaultTranspiler.visitJavaStatement(ctx.javaStatement());
        } else if (ctx.observerStatement() != null) {
            return ctx.accept(observerTranspiler);
        } else {
            throw new RuntimeException("Statement not implemented in visitor");
        }
    }*/
/*

    @Override
    public Code visitAssignment(ConfluxParser.AssignmentContext ctx)                   {
        return Code.fromString(defaultTranspiler.visitAssignment(ctx));
    }

    @Override
    public Code visitArrayAssignement(ConfluxParser.ArrayAssignementContext ctx) {
        return Code.fromString(defaultTranspiler.visitArrayAssignement(ctx));
    }

    @Override
    public String visitArrayInitWithValues(ConfluxParser.ArrayInitWithValuesContext ctx) {
        return defaultTranspiler.visitArrayInitWithValues(ctx);
    }
    @Override
    public String visitArrayInitWithLength(ConfluxParser.ArrayInitWithLengthContext ctx) { return defaultTranspiler.visitArrayInitWithLength(ctx); }
    @Override
    public String visitReturnStatement(ConfluxParser.ReturnStatementContext ctx)         { return defaultTranspiler.visitReturnStatement(ctx); }
*/

}
