package transpiler;

// Global constants for the transpiler
public class Environment {
    public static final String RUNTIME_PACKAGE = "transpiler.runtime";

    // Create an identifier that can't be defined in the source language to void name conflicts
    public static String reservedId(String source) {
        return "_" + source;
    }

    // Return a valid java identifier, if the source is a java keyword that isn't a key word in the project language,
    // then the returned string should be an equivalent
    public static String escapeJavaKeyword(String source) {
        throw new RuntimeException("Not implemented");
    }
}
