package transpiler;

import java.util.Set;

// Global constants for the transpiler
public class Environment {

    // Create an identifier that can't be defined in the source language to avoid name conflicts
    public static String reservedId(String source) {
        return "_" + source;
    }

    public static String classId(String source) {
        return "_Class" + source;
    }

    public static String decoratorWrapperId(String source) {
        return "_Decorated" + source;
    }

    // name for generated identifiers that will not be referred to directly (e.g. when needed just for type checking)
    public static String unusedIdentifier() {
        return "__";
    }

    // If the source is a java keyword that isn't a keyword in the project language,
    // then the returned string will be converted into a valid java identifier
    public static String escapeJavaKeyword(String source) {
        return UNUSED_JAVA_KEYWORDS.contains(source) ? reservedId(source) : source;
    }

    // TODO: decide if this includes: assert, throw, throws, try, catch, finally, null
    public static final Set<String> UNUSED_JAVA_KEYWORDS = Set.of(
            "abstract", "new", "default", "package", "synchronized", "do", "goto", "implements", "protected",
            "public", "private", "enum", "instanceof", "transient", "interface", "static", "class", "strictfp",
            "volatile", "native", "super", "exports", "opens", "requires", "yield", "permits", "sealed", "non-sealed",
            "provides", "to", "when", "open", "record", "transitive"
    );
}
