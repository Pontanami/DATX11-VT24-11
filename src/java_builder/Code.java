package java_builder;

/**
 * Abstract representation of java code. This is the common interface for the builder classes in this package, where
 * the method toCode is the 'build' method.
 */
public interface Code {

    /**
     * Generate the java code represented by this object, as a string. Implementations may throw an
     * {@link IllegalStateException} valid java code cannot be generated from the code object in its current state.
     * @param indentation the current indentation.
     * @return java code as a string.
     */
    String toCode(Indentation indentation);

    /**
     * Generate the java code represented by this object, as a string without indentation. The default implementation
     * calls {@code toCode} with {@link Indentation}{@code .NONE}.
     * @return java code as a string.
     */
    default String toCode() { return toCode(Indentation.NONE); }

    /**
     * (Factory method) Create a code object from a string which will be returned by the toCode method. Indentation
     * will be added to the beginning of the string only and not on consecutive lines.
     * @param source the string that will be returned by toCode
     * @return a code object representing the given string
     * @throws IllegalArgumentException if source is null
     */
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

    /**
     * Return a code object that produces the same code as this object but with an adjusted indentation level.
     * @param delta the change in indentation level
     * @return the code object with changed indentation
     */
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

    /**
     * Return a code object that produces the same code as this object but without indentation.
     * @return the code object without indentation
     */
    default Code withoutIndentation() {
        final Code base = this;
        return new Code() {
            public String toCode(Indentation i) {
                return base.toCode(Indentation.NONE);
            }
            public String toString() {
                return "Code.withoutIndentation(base=" + base + ")";
            }
        };
    }
}
