package transpiler.visitors;

import grammar.gen.ConfluxLexer;
import grammar.gen.ConfluxParserBaseVisitor;
import grammar.gen.ConfluxParserVisitor;
import java_builder.Code;
import java_builder.CodeBuilder;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import transpiler.Environment;

import java.util.List;

import static grammar.gen.ConfluxParser.*;

public class StatementTranspiler extends ConfluxParserBaseVisitor<Code> {
    private final ConfluxParserVisitor<String> obsVisitor;
    private final ConfluxParserVisitor<String> expVisitor;

    public StatementTranspiler(ConfluxParserVisitor<String> obsVisitor, ConfluxParserVisitor<String> expVisitor) {
        this.obsVisitor = obsVisitor;
        this.expVisitor = expVisitor;
    }

    @Override
    public Code visitStatement(StatementContext ctx) {
        return visitFirst(ctx);
    }

    @Override
    public Code visitJavaStatement(JavaStatementContext ctx) {
        if (ctx.SEMI() != null) {
            return new CodeBuilder().append(visitFirst(ctx)).append(";");
        }
        return visitFirst(ctx);
    }

    @Override
    public Code visitObserverStatement(ObserverStatementContext ctx) {
        return visitFirst(ctx);
    }

    private Code visitFirst(ParserRuleContext ctx) {
        return ctx.getChild(0).accept(this);
    }

    @Override
    public Code visitPublishStatement(PublishStatementContext ctx) {
        return Code.fromString(obsVisitor.visitPublishStatement(ctx));
    }

    @Override
    public Code visitAddSubscriberStatement(AddSubscriberStatementContext ctx) {
        return Code.fromString(obsVisitor.visitAddSubscriberStatement(ctx));
    }

    @Override
    public Code visitRemoveSubscriberStatement(RemoveSubscriberStatementContext ctx) {
        return Code.fromString(obsVisitor.visitRemoveSubscriberStatement(ctx));
    }

    @Override
    public Code visitDeclaration(DeclarationContext ctx) {
        return new CodeBuilder()
                .beginConditional(ctx.VAR() == null).append("final ").endConditional()
                .append(visitType(ctx.type())).append(" ")
                .beginDelimiter(", ")
                .append(ctx.declarationPart().stream().map(this::visitDeclarationPart).toList())
                .endDelimiter();
    }

    @Override
    public Code visitAssignment(AssignmentContext ctx) {
        return new CodeBuilder()
                .append(ctx.assignmentLeftHandSide().accept(expVisitor))
                .append(" = ")
                .append(visitExpression(ctx.expression()));
    }

    @Override
    public Code visitReturnStatement(ReturnStatementContext ctx) {
        return new CodeBuilder().append("return ").append(visitExpression(ctx.expression())).append(";");
    }

    @Override
    public Code visitIfStatement(IfStatementContext ctx) {
        CodeBuilder ifStm = buildIf(new CodeBuilder(), ctx.expression(), ctx.statement());
        ctx.elseIfStatememt().forEach(stm ->
                buildIf(ifStm.appendLine(0, "else "), stm.expression(), stm.statement())
        );
        if (ctx.elseStatement() != null) {
            addNestedStatements(ifStm.appendLine(0, "else"), ctx.elseStatement().statement());
        }
        return ifStm;
    }

    private CodeBuilder buildIf(CodeBuilder builder, ExpressionContext exp, StatementContext stm) {
        return addNestedStatements(builder.append("if (").append(visitExpression(exp)).append(")"), stm);
    }

    @Override
    public Code visitForStatement(ForStatementContext ctx) {
        CodeBuilder forStm = new CodeBuilder()
                .append("for (").append(visitDeclaration(ctx.declaration())).append("; ")
                .append(visitExpression(ctx.expression(0))).append("; ")
                .append(visitExpression(ctx.expression(1))).append(")");
        return addNestedStatements(forStm, ctx.statement());
    }
    @Override
    public Code visitWhileStatement(WhileStatementContext ctx) {
        return addNestedStatements(
                new CodeBuilder().append("while (").append(visitExpression(ctx.expression())).append(")"),
                ctx.statement()
        );
    }

    private CodeBuilder addNestedStatements(CodeBuilder builder, StatementContext stm) {
        if (stm.javaStatement() != null && stm.javaStatement().block() != null) {
            List<Code> stms = stm.javaStatement().block().statement().stream().map(this::visitStatement).toList();
            builder.append(" {").appendLine(1, stms).appendLine(0, "}");
        } else {
            builder.appendLine(1, visitStatement(stm));
        }
        return builder;
    }

    @Override
    public Code visitSwitchStatement(SwitchStatementContext ctx) {
        CodeBuilder switchStm = new CodeBuilder()
                .append("switch (")
                .append(visitExpression(ctx.expression()))
                .append(") {");

        ctx.case_().forEach(caseCtx -> addSwitchCase(switchStm, caseCtx));

        if (ctx.default_() != null) {
            switchStm.newLine(1).append("default:");
            ctx.default_().statement().forEach(stm -> switchStm.appendLine(2, visitStatement(stm)));
        }

        return switchStm.newLine().append("}");
    }

    private void addSwitchCase(CodeBuilder switchStm, CaseContext caseCtx) {
        switchStm.newLine(1)
                 .append("case ")
                 .append(visitExpression(caseCtx.expression()))
                 .append(":");
        caseCtx.statement().forEach(stm -> switchStm.appendLine(2, visitStatement(stm)));
    }

    @Override
    public Code visitBlock(BlockContext ctx) {
        List<Code> stms = ctx.statement().stream().map(this::visitStatement).toList();
        return new CodeBuilder().append("{").appendLine(1, stms).appendLine(0, "}");
    }

    @Override
    public Code visitExpression(ExpressionContext ctx) {
        return Code.fromString(expVisitor.visitExpression(ctx));
    }

    @Override
    public Code visitChildren(RuleNode node) {
        CodeBuilder result = new CodeBuilder().beginDelimiter(" ");
        for (int i = 0; i < node.getChildCount(); i++) {
            result.append(node.getChild(i).accept(this));
        }
        return result;
    }

    @Override
    public Code visitTerminal(TerminalNode node) {
        String source;
        if (node.getSymbol().getType() == ConfluxLexer.Identifier)
            source = Environment.escapeJavaKeyword(node.toString());
        else if (node.getSymbol().getType() == ConfluxLexer.BASE)
            source = Environment.reservedId("getBase") + "()";
        else
            source = node.getText();
        return Code.fromString(source);
    }
}
