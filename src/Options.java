import java.io.File;
import java.nio.file.Path;
import java.util.*;

// Container for command line options for the transpiler, the constructor parses the args.
final class Options {
    private static final String COMPILE_FLAG = "compile";
    private static final String DEFAULT_COMPILER = "javac";

    private static final String RUN_FLAG = "run";
    private static final String DEFAULT_INTERPRETER = "java";

    private static final String OUTPUT_FLAG = "output";
    private static final String INPUT_EXTENSION = "flux";
    private static final String USAGE = """
            Usage:
               [-c|--compile [JAVA_COMPILER]]
               [-r|--run     [JAVA_INTERPRETER]]
               [-o|--output  OUTPUT_DIR]
               INPUT_FILES...""";

    private final String javaCompiler;
    private final String javaInterpreter;
    private final String outputDir;
    private final List<String> sourceFiles;

    Options(String[] args) {
        List<String> argList = new ArrayList<>(List.of(args));
        List<String> sourceFiles = new ArrayList<>();

        String javaCompiler = parseFlag(COMPILE_FLAG, DEFAULT_COMPILER, argList);
        String javaInterpreter = parseFlag(RUN_FLAG, DEFAULT_INTERPRETER, argList);
        String outputDir = parseFlag(OUTPUT_FLAG, null, argList);


        argList.forEach(f -> addInputFileToList(f, sourceFiles));// the remaining args must be Conflux files
        if (argList.isEmpty()) {
            reportAndExit("No input files");
        }
        if (javaCompiler == null && javaInterpreter != null) {
            javaCompiler = DEFAULT_COMPILER; // must compile if we're running, so set to default
        }
        if (outputDir == null) {
            outputDir = getDefaultOutputDir();
        }
        this.javaCompiler = javaCompiler;
        this.javaInterpreter = javaInterpreter;
        this.outputDir = outputDir;
        this.sourceFiles = List.copyOf(sourceFiles);
    }

    // Parse a flag and its argument. If the flag isn't present return null. If flag is present, remove the flag
    // (and argument if present) from the input, and return the argument. If defaultValue is null, the flag must
    // have an argument, otherwise exit with error.
    private String parseFlag(String flag, String defaultValue, List<String> input) {
        final String fullFlag = "--" + flag;
        final String shortFlag = "-" + flag.charAt(0);
        String flagValue = null;

        for (Iterator<String> it = input.listIterator(); it.hasNext(); ) {
            String arg = it.next();
            if (arg.equals(fullFlag) || arg.equals(shortFlag)) {
                if (flagValue != null) {
                    reportAndExit("Duplicate flag " + flag);
                }
                it.remove();
                String value = defaultValue;
                if (it.hasNext()) {
                    arg = it.next();
                    if (!isInputFileValid(arg) && arg.charAt(0) != '-') {//the next arg is not a source file or a flag
                        value = arg;
                        it.remove();
                    }
                }
                if (value == null) {
                    reportAndExit("Missing argument to flag " + flag);
                } else {
                    flagValue = value;
                }
            }
        }
        return flagValue;
    }

    private void addInputFileToList(String fileName, List<String> list) {
        File file = new File(fileName);
        if (file.isDirectory()) {
            for (File fileInDir : file.listFiles()) {
                String path = fileInDir.getAbsolutePath();
                if (isInputFileValid(path)) {
                    list.add(path);
                }
            }
        } else if (isInputFileValid(fileName)) {
            list.add(fileName);
        } else {
            reportAndExit("Invalid extension for file '%s', must be '.%s".formatted(file, INPUT_EXTENSION));
        }
    }

    private boolean isInputFileValid(String file) {
        String validPattern = "^.*\\." + INPUT_EXTENSION + "$";
        return Path.of(file).getFileName().toString().matches(validPattern);
    }

    private String getDefaultOutputDir() {
        return Path.of(System.getProperty("user.dir"), "transpiler-output").toString();
    }

    private void reportAndExit(String errMsg) {
        System.err.println(errMsg);
        System.err.println(USAGE);
        System.exit(1);
    }

    @Override
    public String toString() {
        return ("""
                Options {
                   javaCompiler:    "%s"
                   javaInterpreter: "%s"
                   outputDir:       "%s"
                   sourceFiles:     %s
                }""")
                .formatted(javaCompiler, javaInterpreter, outputDir, sourceFiles);
    }

    String getJavaCompiler() { return javaCompiler; }
    String getJavaInterpreter() { return javaInterpreter; }
    String getOutputDir() { return outputDir; }
    List<String> getSourceFiles() { return sourceFiles; } // this list is read-only
}
