package java_builder;

import java.util.Objects;

// Indentation with spaces
public final class SpaceIndentation implements Indentation {
    private final int tabSize;
    private final int tabLevel;

    public SpaceIndentation(int tabSize) {
        this(tabSize, 0);
    }

    public SpaceIndentation(int tabSize, int tabLevel) {
        this.tabSize = requireNonNegative(tabSize);
        this.tabLevel = requireNonNegative(tabLevel);
    }

    private int requireNonNegative(int n) {
        if (n < 0) throw new IllegalArgumentException("tab size and tab level must be >= 0");
        return n;
    }

    @Override
    public Indentation adjustLevel(int delta) { return new SpaceIndentation(tabSize, tabLevel + delta); }

    @Override
    public int getLevel() { return tabLevel; }

    @Override // OBS! Annan version än Omars som inte funkade på min dator...
    public String string() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < tabSize * tabLevel; i++) {
            stringBuilder.append(" ");
        }
        return stringBuilder.toString();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpaceIndentation that = (SpaceIndentation) o;
        return tabSize == that.tabSize && tabLevel == that.tabLevel;
    }

    @Override
    public int hashCode() { return Objects.hash(tabSize, tabLevel); }

    @Override
    public String toString() { return "Indentation{tabSize=" + tabSize + ", tabLevel=" + tabLevel + '}'; }
}
