package transpiler;

// Global constants for the transpiler
public class Environment {
    public static final String RUNTIME_PACKAGE = "transpiler.runtime";

    // Create an identifier that can't be defined in the source language to avoid name conflicts
    public static String reservedId(String source) {
        return "_" + source;
    }

    // If the source is a java keyword that isn't a keyword in the project language,
    // then the returned string should be an converted into a valid java identifier
    public static String escapeJavaKeyword(String source) {
        throw new RuntimeException("Not implemented"); //TODO: implementera om det behövs
    }
}
