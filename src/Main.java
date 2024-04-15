import grammar.gen.ConfluxLexer;
import grammar.gen.ConfluxParser;
import java_builder.MethodBuilder;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.tree.ParseTree;
import transpiler.tasks.TaskQueue;
import transpiler.visitors.*;

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
            ConfluxLexer lexer = new ConfluxLexer(stream);
            TokenStream tokenStream = new CommonTokenStream(lexer);
            ConfluxParser parser = new ConfluxParser(tokenStream);
            // Ensures that the parser throws a ParseCancellationException if it can't parse
            parser.setErrorHandler(new BailErrorStrategy());

            System.out.println("Testar METHOD-TRANSPILERN::::\n");

            MethodBuilder mb = new MethodBuilder();
            StatementTranspiler st = new StatementTranspiler(new ObserverTranspiler(new TaskQueue()));

            ParseTree prog = parser.methodDeclaration();
            MethodTranspiler methodTranspiler = new MethodTranspiler(mb, st);

            prog.accept(methodTranspiler);
          //  System.out.println(methodTranspiler.methodSignatureToString());
            System.out.println("Method declaration:");
            System.out.println(methodTranspiler.methodDeclarationToString());

            System.out.println("\nMethod signature:");
            System.out.println(methodTranspiler.methodSignatureToString());


            System.out.println("\nMethod body:");
            System.out.println(methodTranspiler.methodBodyToString());


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