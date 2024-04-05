package transpiler.visitors;

import grammar.gen.TheParser;
import grammar.gen.TheParserBaseVisitor;
import java_builder.Code;
import java_builder.CodeBuilder;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

// Approximately recreates the source code, ignoring formatting
// useful when java syntax and [language name] syntax are the same
public class OneToOneTranspiler extends TheParserBaseVisitor<String> {

    @Override
    public String visitChildren(RuleNode node) {
        return concatChildren(node, " "); // default behavior is to put spaces between tokens
    }

    @Override
    public String visitQualifiedIdentifier(TheParser.QualifiedIdentifierContext ctx) {
        return concatChildren(ctx, ""); // don't put spaces around tokens in qualified ids like 'obj.x.y'
    }

    // TODO: (probably not very important) override default delimiter for some cases

    @Override
    public String visitTerminal(TerminalNode node) {
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
