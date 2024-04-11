import grammar.gen.ConfluxLexer;
import grammar.gen.ConfluxParser;
import java_builder.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.tree.ParseTree;
import transpiler.Transpiler;
import transpiler.TranspilerState;
import transpiler.tasks.TaskQueue;
import transpiler.visitors.ConstructorTranspiler;
import transpiler.visitors.ObserverTranspiler;
import transpiler.visitors.StatementTranspiler;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

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
            ParseTree prog = parser.program();

            TranspilerState state = null; // initialState();
            TaskQueue taskQueue = new TaskQueue();
            prog.accept(new ConstructorTranspiler(taskQueue));
            taskQueue.runTasks(state);

            System.out.println(state.lookupInterface("Car").toCode(new SpaceIndentation(3)));
            System.out.println();
            System.out.println(state.lookupClass("_ClassCar").toCode(new SpaceIndentation(3)));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (ParseCancellationException e) {
            System.out.println("PARSER ERROR");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /*static TranspilerState initialState() {
        TranspilerState state = new Transpiler.State();
        state.addInterface(new InterfaceBuilder().setIdentifier("Car").addModifier("public"));
        state.addClass(new ClassBuilder().setIdentifier("_ClassCar").addImplementedInterface("Car"));
        return state;
    }*/
}