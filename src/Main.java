import grammar.gen.TheLexer;
import grammar.gen.TheParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.BailErrorStrategy;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Error: missing input file");
            return;
        }
        try {
            CharStream stream = CharStreams.fromFileName(args[0]);
            TheLexer lexer = new TheLexer(stream);
            TokenStream tokenStream = new CommonTokenStream(lexer);
            TheParser parser = new TheParser(tokenStream);
            // Ensures that the parser throws a ParseCancellationException if it can't parse
            parser.setErrorHandler(new BailErrorStrategy());
            TheParser.ProgramContext prog = parser.program();
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