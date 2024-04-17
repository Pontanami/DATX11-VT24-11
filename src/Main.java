import grammar.gen.ConfluxLexer;
import grammar.gen.ConfluxParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.tree.ParseTree;
import transpiler.tasks.TaskQueue;
import transpiler.visitors.*;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Error: missing input file");
            return;
        }
        try {
            CharStream stream = CharStreams.fromFileName(args[0]);
            ConfluxLexer lexer = new ConfluxLexer(stream);
            TokenStream tokenStream = new CommonTokenStream(lexer);
            ConfluxParser parser = new ConfluxParser(tokenStream);
            // Ensures that the parser throws a ParseCancellationException if it can't parse
            parser.setErrorHandler(new BailErrorStrategy());

            ParseTree tree = parser.attributesBlock();
            AttributeTranspiler attributeTranspiler = new AttributeTranspiler();
            System.out.println(tree.accept(attributeTranspiler));

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (ParseCancellationException e) {
            System.out.println("PARSER ERROR");
            e.printStackTrace();
            System.exit(1);
        }
    }
}