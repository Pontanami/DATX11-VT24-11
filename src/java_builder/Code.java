package java_builder;

// Interface for things that can be turned into code
public interface Code {
    // Generate code, from this object
    String toCode(Indentation indentation);

    // Generate code without indentation
    default String toCode() { return toCode(Indentation.noIndent()); }

    // Returns a Code instance identical to this instance but with adjusted indentation level
    default Code withIndentationDelta(int delta) {
        final Code base = this;
        return indentation -> base.toCode(indentation.adjustLevel(delta));
    }

    // Returns a Code instance identical to this instance but without indentation
    default Code withoutIndentation() {
        final Code base = this;
        return i -> base.toCode(Indentation.noIndent());
    }

    // Create a code object from a string, rejects null
    static Code fromString(String source) {
        if (source == null) throw new IllegalArgumentException("Code.fromString: null argument");
        return new Code() {
            public String toCode(Indentation indentation) { return indentation.string() + source; }
            public String toString() { return "Code.fromString{source='" + source + "'}"; }
        };
    }
}
