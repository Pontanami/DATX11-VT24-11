package transpiler.visitors;

import grammar.gen.TheLexer;
import grammar.gen.TheParser;
import grammar.gen.TheParserBaseVisitor;
import java_builder.CodeBuilder;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import transpiler.Environment;

// Approximately recreates the source code, ignoring formatting,
// useful when java syntax and [language name] syntax are the same
public class DefaultTranspiler extends TheParserBaseVisitor<String> {

    @Override
    public String visitChildren(RuleNode node) {
        return concatChildren(node, " "); // default behavior is to put spaces between tokens
    }

    @Override
    public String visitQualifiedIdentifier(TheParser.QualifiedIdentifierContext ctx) {
        return concatChildren(ctx, ""); // don't put spaces around tokens in qualified ids like 'obj.x.y'
    }

    @Override
    public String visitTerminal(TerminalNode node) {
        if (node.getSymbol().getType() == TheLexer.Identifier)
            return Environment.escapeJavaKeyword(node.toString());
        else
            return node.toString();
    }

    private String concatChildren(RuleNode node, String delimiter) {
        CodeBuilder result = new CodeBuilder().beginDelimiter(delimiter);
        for (int i = 0; i < node.getChildCount(); i++) {
            result.append(node.getChild(i).accept(this));
        }
        return result.toCode();
    }
}
