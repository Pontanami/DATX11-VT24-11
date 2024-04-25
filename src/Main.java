import grammar.gen.ConfluxLexer;
import grammar.gen.ConfluxParser;
import grammar.gen.ConfluxParser.ProgramContext;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.BailErrorStrategy;
import transpiler.Transpiler;
import transpiler.TranspilerException;
import transpiler.TranspilerOutput;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            Options options = new Options(args);
            Transpiler transpiler = new Transpiler();
            for (String file : options.getSourceFiles()) {
                transpiler.addSource(getNameWithoutExtension(file), parse(file));
            }
            TranspilerOutput output = transpiler.transpile();
            String outDir = options.getOutputDir();
            writeJavaFiles(outDir, output);

            String javaCompiler = options.getJavaCompiler();
            if (javaCompiler != null) {
                runJavaCompiler(javaCompiler, outDir, output.allFileNames());
            }
            String javaInterpreter = options.getJavaInterpreter();
            String mainFile = output.lookupMainFileName();
            if (javaInterpreter != null && mainFile != null) {
                runJavaMain(javaInterpreter, outDir, output.getPackageName(), mainFile);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace(System.err);
            System.exit(1);
        } catch (ParseCancellationException e) {
            System.err.println("PARSER ERROR");
            e.printStackTrace(System.err);
            System.exit(1);
        } catch (TranspilerException e) {
            System.err.println("TRANSPILER ERROR");
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    private static ProgramContext parse(String file) throws IOException, ParseCancellationException {
        CharStream stream = CharStreams.fromFileName(file);
        ConfluxLexer lexer = new ConfluxLexer(stream);
        TokenStream tokenStream = new CommonTokenStream(lexer);
        ConfluxParser parser = new ConfluxParser(tokenStream);
        // Ensures that the parser throws a ParseCancellationException if it can't parse
        parser.setErrorHandler(new BailErrorStrategy());
        return parser.program();
    }

    private static void createOutputDirectory(String outDir) throws IOException {
        try {
            Files.createDirectories(Path.of(outDir));
        } catch (IOException e) {
            System.err.println("Couldn't create output directory: " + outDir);
            throw new IOException(e);
        }
    }

    private static String getNameWithoutExtension(String path) {
        return Path.of(path).getFileName().toString().replaceAll("\\..*$", "");
    }

    // Go through the transpiler output and write all the java files into the given directory
    private static void writeJavaFiles(String outDir, TranspilerOutput output) throws IOException {
        createOutputDirectory(outDir);
        for (String fileName : output.allFileNames()) {
            File javaFile = new File(Path.of(outDir, fileName).toString());
            javaFile.createNewFile();
            try (FileWriter writer = new FileWriter(javaFile)) {
                writer.write(output.getTranspiledCode(fileName));
            }
        }
    }

    // Run the java compiler on the given files in the given directory
    private static void runJavaCompiler(String compiler, String outDir, List<String> files)
            throws IOException, InterruptedException {
        List<String> compileJavaCmd = new ArrayList<>();
        compileJavaCmd.add(compiler);
        compileJavaCmd.add("-cp"); // include the runtime classes
        compileJavaCmd.add('"' + getConfluxJar() + '"');

        compileJavaCmd.add("-d"); // set the output directory
        compileJavaCmd.add(Path.of(outDir).toString());
        files.forEach(f -> compileJavaCmd.add('"' + Path.of(outDir, f).toString() + '"'));

        runCommandWait(compileJavaCmd);
    }

    // Run the java main file
    private static void runJavaMain(String javaInterpreter, String outDir, String pkgId, String mainFile)
            throws IOException, InterruptedException {
        String mainId = getNameWithoutExtension(mainFile);
        String sep = System.getProperty("path.separator");
        List<String> runCmd = List.of(
                javaInterpreter,
                "-cp",
                '"' + getConfluxJar() + sep + outDir + '"',
                '"' + pkgId + '.' + mainId + '"'
        );
        runCommandWait(runCmd);
    }

    // Run a command with args represented by a list of strings, and wait for it to terminate
    private static void runCommandWait(List<String> call) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(call).inheritIO().start();
        process.waitFor();
    }

    private static String getConfluxJar() {
        try {
            return new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
        } catch (URISyntaxException e) {
            System.err.println("Couldn't find Conflux jar file, caused by:");
            e.printStackTrace(System.err);
            System.exit(1);
            throw new AssertionError("Unreachable code");
        }
    }
}