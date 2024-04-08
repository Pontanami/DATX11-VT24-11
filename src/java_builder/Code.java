package java_builder;

// Interface for things that can be turned into code
public interface Code {
    // Generate code, from this object
    String toCode(Indentation indentation);

    // Generate code without indentation
    default String toCode() { return toCode(Indentation.noIndent()); }


    // (Factory method) Create a code object from a string, rejects null
    static Code fromString(String source) {
        if (source == null) throw new IllegalArgumentException("Code.fromString: null argument");
        String escaped = source.replace("\n", "\\n").replace("\r", "\\r");

        return new Code() {
            public String toCode(Indentation indentation) {
                return indentation.string() + source;
            }
            public String toString() {
                return "Code.fromString(source=\"" + escaped + "\")";
            }
        };
    }


    //////////////////// Creating Decorators ////////////////////

    // Returns a Code instance identical to this instance but with adjusted indentation level
    default Code withIndentationDelta(int delta) {
        if (delta == 0) return this;
        final Code base = this;
        return new Code() {
            @Override
            public String toCode(Indentation indentation) {
                return base.toCode(indentation.adjustLevel(delta));
            }
            @Override
            public String toString() {
                return "Code.withIndentationDelta(delta=" + delta + ", base="  + base + ")";
            }
        };
    }

    // Returns a Code instance identical to this instance but without indentation
    default Code withoutIndentation() {
        final Code base = this;
        return new Code() {
            public String toCode(Indentation i) {
                return base.toCode(Indentation.noIndent());
            }
            public String toString() {
                return "Code.withoutIndentation(base=" + base + ")";
            }
        };
    }
}
