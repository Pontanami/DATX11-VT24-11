package java_builder;

// Code indentation
public interface Indentation {
    // Return an indentation with a level adjusted by the given delta, IllegalArgumentException if it goes below 0
    Indentation adjustLevel(int delta);

    int getLevel();

    // Return a string that contains just the indentation
    String string();

    // Indentation that's always on level 0, useful for code that shouldn't be indented
    static Indentation noIndent() {
        return new Indentation() {
            public Indentation adjustLevel(int delta) { return this; }
            public int getLevel() { return 0; }
            public String string() { return ""; }
        };
    }
}
