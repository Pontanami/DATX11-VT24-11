package old_grammar;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.io.IOException;

public class TestVisitor extends ExprBaseVisitor<String> {
    @Override
    public String visitChildren(RuleNode node) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < node.getChildCount(); i++) {
            result.append(node.getChild(i).accept(this));
        }
        return result.toString();
    }

    @Override
    public String visitTerminal(TerminalNode node) {
        return node.toString();
    }

    public static void main(String[] args) {
        ParseTree tree = parse("C:\\Repositories\\DATX11-VT24-11\\src\\test.txt");
        System.out.println(tree.accept(new TestVisitor()));
    }

    static ParseTree parse(String fileName) {
        try {
            CharStream stream = CharStreams.fromFileName(fileName);
            ExprLexer lexer = new ExprLexer(stream);
            TokenStream tokenStream = new CommonTokenStream(lexer);
            ExprParser parser = new ExprParser(tokenStream);
            // Ensures that the parser throws a ParseCancellationException if it can't parse
            parser.setErrorHandler(new BailErrorStrategy());
            return parser.prog();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (ParseCancellationException e) {
            System.out.println("PARSER ERROR");
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }
}
