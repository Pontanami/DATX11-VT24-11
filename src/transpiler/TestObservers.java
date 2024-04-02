package transpiler;

import grammar.gen.TheLexer;
import grammar.gen.TheParser;
import java_builder.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class TestObservers {
    static String testFolder = "C:\\Users\\omar-\\Documents\\DV\\DIT561 Thesis\\observer branch\\DATX11-VT24-11\\src\\observer_test\\";
    static String testFile = testFolder + "Test.txt";
    static String tcFile = testFolder + "TrafficController.txt";

    public static void main(String[] args) {
        testObservers();
    }

    static void testObservers() {
        Environment env = new Environment();
        env.createClass("Test", parse(testFile));
        env.createClass("TrafficController", parse(tcFile));

        new ObserverTranspiler().transpile(env);

        for (String outName : env.getOutputIds()) {
            String outPath = testFolder + outName + ".java";
            Code topLevelDef = new CodeBuilder()
                    .append("package observer_test;\n\n")
                    .beginIndentItems(0)
                    .append(env.lookupCode(outName));
            writeFile(outPath, topLevelDef);
        }
    }

    static void writeFile(String outName, Code code) {
        try {
            File outClass = new File(outName);
            outClass.createNewFile();
            FileWriter writer = new FileWriter(outName);
            writer.write(code.toCode(new SpaceIndentation(3)));
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static ParseTree parse(String fileName) {
        try {
            CharStream stream = CharStreams.fromFileName(fileName);
            TheLexer lexer = new TheLexer(stream);
            TokenStream tokenStream = new CommonTokenStream(lexer);
            TheParser parser = new TheParser(tokenStream);
            // Ensures that the parser throws a ParseCancellationException if it can't parse
            parser.setErrorHandler(new BailErrorStrategy());
            return parser.program();
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
