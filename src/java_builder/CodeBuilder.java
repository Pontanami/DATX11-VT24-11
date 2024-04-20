package java_builder;

import java.util.*;
import java.util.stream.Collectors;

import static java_builder.Code.fromString;

/**
 * A general builder for putting together code fragments, similar to {@link StringBuilder} but with some different
 * specialized features such as delimiters, prefixes, suffixes, and conditional branches. When appending fragments,
 * indentation will be ignored unless the {@code CodeBuilder} was empty before the call, or if using the methods
 * {@code appendLine}, or if the call was preceded by a call to {@code newLine}. The order in which the various elements
 * are added is illustrated below. Elements enclosed in brackets denotes optional items.
 * <p>
 * [newLine] [indentation] [prefix] fragment [suffix] [delimiter] [newLine] [indentation] [prefix] fragment [suffix] ...
 * <p>
 * All methods in this class throw an {@link IllegalArgumentException} for null arguments unless the builder is in a
 * conditional branch that isn't executed. All the 'setter' methods in this class returns the instance for chaining.
 */
public class CodeBuilder implements Code {
    private static final Code NEW_LINE = new Code() {
        public String toCode() { return "\n"; }
        public String toCode(Indentation indentation) { return "\n"; }
        public String toString() { return "[NEW_LINE]"; }
    };
    private static final Code EMPTY = new Code() {
        public String toCode() { return ""; }
        public String toCode(Indentation indentation) { return indentation.string(); }
        public String toString() { return "[EMPTY]"; }
    };

    private final List<Code> fragments;
    private final Deque<Boolean> conditionalBlocks;
    private Code delimiter; // null means don't use delimiter
    private boolean firstDelimitedItem; // for remembering not to put a delimiter before the first item
    private Code prefix; // null means don't use prefix
    private Code suffix; // null means don't use suffix

    public CodeBuilder() {
        fragments = new ArrayList<>();
        conditionalBlocks = new ArrayDeque<>();
        delimiter = null;
        firstDelimitedItem = false;
        prefix = null;
        suffix = null;
    }

    /**
     * Return true if this {@code CodeBuilder} is empty i.e. nothing has been appended.
     * @return if this {@code CodeBuilder} is empty
     */
    public boolean isEmpty() { return fragments.isEmpty(); }

    /**
     * Append the given strings to this {@code CodeBuilder} unless this {@code CodeBuilder} is currently in a
     * conditional branch that will not be executed. If this {@code CodeBuilder} is currently using a prefix, delimiter,
     * or suffix, these will be appended before/between/after each string.
     * @param fragments zero or more strings to append.
     * @return this {@code CodeBuilder}
     */
    public CodeBuilder append(String... fragments) {
        return runBuilderAction(() -> {
            for (String s : throwOnNull(fragments))
                appendCode(fromString(s));
        });
    }

    /**
     * Append each of the given strings on a new line, unless this {@code CodeBuilder} is currently in a conditional
     * branch that will not be executed. If this {@code CodeBuilder} is currently using a prefix, delimiter, or suffix,
     * these will be appended before/between/after each string.
     * @param indentDelta the increase in indentation for the appended strings
     * @param fragments zero or more strings to append.
     * @return this {@code CodeBuilder}
     * @throws IllegalArgumentException if {@code indentDelta < 0}
     */
    public CodeBuilder appendLine(int indentDelta, String... fragments) {
        return runBuilderAction(() -> {
            for (String line : throwOnNull(fragments))
                appendCode(fromString(line), indentDelta);
        });
    }

    /**
     * Append the given code fragments to this {@code CodeBuilder} unless this {@code CodeBuilder} is currently in
     * conditional branch that will not be executed. If this {@code CodeBuilder} is currently using a prefix, delimiter,
     * or suffix, these will be appended before/between/after each fragment.
     * @param fragments zero or more code fragments to append.
     * @return this {@code CodeBuilder}
     */
    public CodeBuilder append(Code... fragments) {
        return runBuilderAction(() -> {
            for (Code code : throwOnNull(fragments))
                appendCode(code);
        });
    }

    /**
     * Append each of the given code fragments on a new line, unless this {@code CodeBuilder} is currently in a
     * conditional branch that will not be executed. If this {@code CodeBuilder} is currently using a prefix, delimiter,
     * or suffix, these will be appended before/between/after each fragment.
     * @param indentDelta the increase in indentation for the appended code fragments
     * @param fragments zero or more code fragments to append.
     * @return this {@code CodeBuilder}
     * @throws IllegalArgumentException if {@code indentDelta < 0}
     */
    public CodeBuilder appendLine(int indentDelta, Code... fragments) {
        return runBuilderAction(() -> {
            for (Code f : throwOnNull(fragments))
                appendCode(f, indentDelta);
        });
    }

    /**
     * Append the given collection of code fragments to this {@code CodeBuilder} unless this {@code CodeBuilder} is
     * currently in a conditional branch that will not be executed. If this {@code CodeBuilder} is currently using a
     * prefix, delimiter, or suffix, these will be appended before/between/after each fragment.
     * @param fragments a collection of code fragments to append.
     * @return this {@code CodeBuilder}
     */
    public CodeBuilder append(Collection<? extends Code> fragments) {
        return runBuilderAction(() -> throwOnNull(fragments).forEach(this::appendCode));
    }

    /**
     * Append each of the code fragments in the given collection on a new line, unless this {@code CodeBuilder} is
     * currently in a conditional branch that will not be executed. If this {@code CodeBuilder} is currently using a
     * prefix, delimiter, or suffix, these will be appended before/between/after each fragment.
     * @param indentDelta the increase in indentation for the appended code fragments
     * @param fragments a collection of code fragments to append.
     * @return this {@code CodeBuilder}
     * @throws IllegalArgumentException if {@code indentDelta < 0}
     */
    public CodeBuilder appendLine(int indentDelta, Collection<? extends Code> fragments) {
        return runBuilderAction(() -> throwOnNull(fragments).forEach(line -> appendCode(line, indentDelta)));
    }

    /**
     * Append zero or more {@code CodeBuilder}s as single units, unless this {@code CodeBuilder} is currently in a
     * conditional branch that will not be executed. If this {@code CodeBuilder} is currently using a prefix, delimiter,
     * or suffix, these will be appended before/between/after each builder.
     * @param ignoreEmpty if true, empty {@code CodeBuilder}s will not be appended
     * @param builders zero or more {@code CodeBuilder}s to append
     * @return this {@code CodeBuilder}
     */
    public CodeBuilder append(boolean ignoreEmpty, CodeBuilder... builders) {
        return runBuilderAction(() -> {
            for (CodeBuilder b : throwOnNull(builders))
                appendCode(ignoreEmpty, b);
        });
    }

    /**
     * Append zero or more {@code CodeBuilder}s on separate lines, unless this {@code CodeBuilder} is currently in a
     * conditional branch that will not be executed. If this {@code CodeBuilder} is currently using a prefix, delimiter,
     * or suffix, these will be appended before/between/after each builder.
     * @param ignoreEmpty if true, empty {@code CodeBuilder}s will not be appended
     * @param indentDelta the increase in indentation for the appended builders
     * @param builders zero or more {@code CodeBuilder}s to append
     * @return this {@code CodeBuilder}
     * @throws IllegalArgumentException if {@code indentDelta < 0}
     */
    public CodeBuilder appendLine(boolean ignoreEmpty, int indentDelta, CodeBuilder... builders) {
        return runBuilderAction(() -> {
            for (CodeBuilder b : throwOnNull(builders))
                appendCode(ignoreEmpty, indentDelta, b);
        });
    }

    /**
     * Append a new line to this {@code CodeBuilder}
     * @return this {@code CodeBuilder}
     */
    public CodeBuilder newLine() { return newLine(0); }

    /**
     * Append a new line to this {@code CodeBuilder} with the given increase in indentation
     * @param indentDelta the increase in indentation for the new line
     * @return this {@code CodeBuilder}
     */
    public CodeBuilder newLine(int indentDelta) {
        return runBuilderAction(() -> {
            fragments.add(NEW_LINE);
            fragments.add(EMPTY.withIndentationDelta(indentDelta));
        });
    }

    ////////////////////////// Helper methods for append //////////////////////////

    private void appendCode(boolean ignoreEmpty, CodeBuilder builder) {
        appendCode(ignoreEmpty, null, builder);
    }
    private void appendCode(boolean ignoreEmpty, Integer indentDelta, CodeBuilder builder) {
        throwOnNull(builder);
        if (ignoreEmpty && builder.isEmpty()) return;
        appendCode(builder, indentDelta);
    }
    private void appendCode(Code fragment) {
        appendCode(fragment, null);
    }
    private void appendCode(Code fragment, Integer indentDelta) {
        throwOnNull(fragment);
        boolean appendOnNewLine = indentDelta != null;
        if (appendOnNewLine && indentDelta < 0)
            throw new IllegalArgumentException("indentDelta < 0");
        if (delimiter != null) {
            if (firstDelimitedItem)
                firstDelimitedItem = false;
            else
                this.fragments.add(delimiter.withoutIndentation());
        }
        if (appendOnNewLine && !isEmpty()) {
            fragments.add(NEW_LINE);
        }
        if (prefix != null) {
            fragments.add(appendOnNewLine || isEmpty()
                    ? prefix.withIndentationDelta(indentDelta != null ? indentDelta : 0)
                    : prefix.withoutIndentation());
        }
        fragments.add(prefix == null && (appendOnNewLine || isEmpty())
                ? fragment.withIndentationDelta(indentDelta != null ? indentDelta : 0)
                : fragment.withoutIndentation()
        );
        if (suffix != null)
            fragments.add(suffix);
    }

    /**
     * Start using a delimiter for following appends. The delimiter will not be added <em>before</em> the next item.
     * {@code "a,b,c,d" == new CodeBuilder().beginDelimiter(",").append("a").append("b").append("c", "d").toCode();}
     * @param delimiter the delimiter
     * @return this {@code CodeBuilder}
     */
    public CodeBuilder beginDelimiter(String delimiter) {
        if (throwOnNull(delimiter).isEmpty()) return endDelimiter();
        return beginDelimiter(fromString(delimiter));
    }

    /**
     * Start using a delimiter for following appends. The delimiter will not be added <em>before</em> the next item.
     * <p>
     * {@code "a,b,c,d" == new CodeBuilder().beginDelimiter(",").append("a").append("b").append("c", "d").toCode();}
     * @param delimiter the delimiter
     * @return this {@code CodeBuilder}
     */
    public CodeBuilder beginDelimiter(Code delimiter) {
        return runBuilderAction(() -> setDelimiter(throwOnNull(delimiter)));
    }

    /**
     * Stop using a delimiter, if one was used previously.
     * @return this {@code CodeBuilder}
     */
    public CodeBuilder endDelimiter() {
        return runBuilderAction(() -> setDelimiter(null));
    }

    private void setDelimiter(Code delimiter) {
        this.delimiter = delimiter;
        this.firstDelimitedItem = delimiter != null;
    }

    /**
     * Start adding the given prefix to appended items
     * @param prefix the prefix
     * @return this {@code CodeBuilder}
     */
    public CodeBuilder beginPrefix(String prefix) {
        return beginPrefix(fromString(throwOnNull(prefix)));
    }
    /**
     * Start adding the given prefix to appended items
     * @param prefix the prefix
     * @return this {@code CodeBuilder}
     */
    public CodeBuilder beginPrefix(Code prefix) {
        return runBuilderAction(() -> this.prefix = throwOnNull(prefix));
    }
    /**
     * Stop using a prefix, if one was used previously.
     * @return this {@code CodeBuilder}
     */
    public CodeBuilder endPrefix() {
        return runBuilderAction(() -> prefix = null);
    }

    /**
     * Start adding the given suffix to appended items. Suffixes are never indented.
     * @param suffix the suffix
     * @return this {@code CodeBuilder}
     */
    public CodeBuilder beginSuffix(String suffix) {
        return beginSuffix(fromString(throwOnNull(suffix)));
    }
    /**
     * Start adding the given suffix to appended items. Suffixes are never indented.
     * @param suffix the suffix
     * @return this {@code CodeBuilder}
     */
    public CodeBuilder beginSuffix(Code suffix) {
        return runBuilderAction(() -> this.suffix = throwOnNull(suffix).withoutIndentation());
    }
    /**
     * Stop using a suffix, if one was used previously.
     * @return this {@code CodeBuilder}
     */
    public CodeBuilder endSuffix() {
        return runBuilderAction(() -> this.suffix = null);
    }

    /**
     * Start a conditional branch. If the boolean argument is false, all subsequent actions will be ignored until call
     * to {@code endConditional} is made. Ignored actions include appends, delimiters, prefixes, and suffixes but not
     * additional calls to {@code beginConditional}. Thus, conditional branches can be nested.
     * <p>
     * {@code "ac" == new CodeBuilder.append("a").beginConditional(false).append("b").endConditional().append("c").toCode();}
     * @param includeNext the condition for the conditional branch
     * @return this {@code CodeBuilder}
     */
    public CodeBuilder beginConditional(boolean includeNext) {
        conditionalBlocks.push(isActive() && includeNext);
        return this;
    }

    /**
     * End the innermost conditional branch.
     * @return this {@code CodeBuilder}
     * @throws IllegalStateException if there are no conditional branches (the * number of calls to endConditional are
     * greater than the number of calls to beginConditional)
     */
    public CodeBuilder endConditional() {
        if (conditionalBlocks.isEmpty())
            throw new IllegalStateException("No conditional block to end");
        conditionalBlocks.pop();
        return this;
    }

    private CodeBuilder runBuilderAction(Runnable action) {
        if (isActive())
            action.run();
        return this;
    }

    private boolean isActive() { return conditionalBlocks.isEmpty() || conditionalBlocks.peek(); }

    @Override
    public String toCode(Indentation indentation) {
        return fragments.stream().map(c -> c.toCode(indentation)).collect(Collectors.joining());
    }

    private <T> T throwOnNull(T obj) {
        if (obj == null)
            throw new IllegalArgumentException("CodeBuilder: null argument");
        return obj;
    }

    @Override
    public String toString() {
        return "CodeBuilder{fragments=" + fragments + '}';
    }
}
