package transpiler;

import java.util.Set;

// Global constants for the transpiler
public class Environment {
    public static final String RUNTIME_PACKAGE = "transpiler.runtime";

    // Create an identifier that can't be defined in the source language to avoid name conflicts
    public static String reservedId(String source) {
        return "_" + source;
    }

    // If the source is a java keyword that isn't a keyword in the project language,
    // then the returned string should be converted into a valid java identifier
    public static String escapeJavaKeyword(String source) {
        return UNUSED_JAVA_KEYWORDS.contains(source) ? reservedId(source) : source;
    }

    // TODO: decide if this includes: assert, continue, throw, throws, try, catch, finally, short, long, byte, null
    public static final Set<String> UNUSED_JAVA_KEYWORDS = Set.of(
            "abstract", "new", "default", "package", "synchronized", "do", "goto", "implements", "protected",
            "public", "private", "enum", "instanceof", "transient", "interface", "static", "class", "strictfp",
            "volatile", "native", "super", "exports", "opens", "requires", "yield", "permits", "sealed", "non-sealed",
            "provides", "to", "when", "open", "record", "transitive"
    );
}
