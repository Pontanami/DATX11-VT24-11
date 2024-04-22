package java_builder;

/**
 * Interface for objects representing code indentation.
 */
public interface Indentation {

    /**
     * Return an indentation object where the indentation level is adjusted by the given delta. This method should throw
     * an {@link IllegalArgumentException} if the resulting indentation level would be less than zero.
     * @param delta the change in indentation
     * @return the new indentation object
     */
    Indentation adjustLevel(int delta);

    /**
     * Return the current indentation level
     * @return the indentation level
     */
    int getLevel();

    /**
     * Return this indentation as a string.
     * @return the indentation string
     */
    String string();

    /**
     * An indentation object that's always on level 0, useful for code that shouldn't be indented
     */
    Indentation NONE = new Indentation() {
        public Indentation adjustLevel(int delta) { return this; }
        public int getLevel() { return 0; }
        public String string() { return ""; }
    };
}
