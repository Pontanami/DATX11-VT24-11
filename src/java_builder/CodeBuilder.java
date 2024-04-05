package java_builder;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java_builder.Code.fromString;

// Builder for putting together code fragments. All methods throw IllegalArgumentException if given null unless
// the builder is in a conditional branch that isn't executed
public class CodeBuilder implements Code {
    private static final Code NEW_LINE = new Code() {
        public String toCode() { return "\n"; }
        public String toCode(Indentation indentation) { return "\n"; }
        public String toString() { return "[NEW_LINE]"; }
    };

    private List<Code> fragments;
    private Deque<Boolean> conditionalBlocks;
    private Code delimiter; // null means don't use delimiter
    private boolean firstDelimitedItem; // for remembering not to put a delimiter before the first item
    private Code prefix; // null means don't use prefix
    private Code suffix; // null means don't use suffix

    public CodeBuilder() { init(); }
    public CodeBuilder(String... fragments) {
        init();
        append(fragments);
    }
    public CodeBuilder(Code... fragments) {
        init();
        append(fragments);
    }
    private void init() {
        fragments = new ArrayList<>();
        conditionalBlocks = new ArrayDeque<>();
        delimiter = null;
        firstDelimitedItem = false;
        prefix = null;
        suffix = null;
    }

    public boolean isEmpty() { return fragments.isEmpty(); }

    public CodeBuilder append(String... fragments) {
        return runBuilderAction(() -> {
            for (String s : throwOnNull(fragments))
                appendCode(Code.fromString(s));
        });
    }
    // append each fragment on a new line with the given change in indentation (must be >= 0)
    public CodeBuilder appendLine(int indentDelta, String... fragments) {
        return runBuilderAction(() -> {
            for (String line : throwOnNull(fragments))
                appendCode(fromString(line), indentDelta);
        });
    }

    public CodeBuilder append(Code... fragments) {
        return runBuilderAction(() -> {
            for (Code code : throwOnNull(fragments))
                appendCode(code);
        });
    }
    public CodeBuilder appendLine(int indentDelta, Code... fragments) {
        return runBuilderAction(() -> {
            for (Code f : throwOnNull(fragments))
                appendCode(f, indentDelta);
        });
    }

    public CodeBuilder append(Collection<? extends Code> fragments) {
        return runBuilderAction(() -> throwOnNull(fragments).forEach(this::appendCode));
    }
    public CodeBuilder appendLine(int indentDelta, Collection<? extends Code> fragments) {
        return runBuilderAction(() -> throwOnNull(fragments).forEach(line -> appendCode(line, indentDelta)));
    }

    public CodeBuilder append(boolean ignoreEmpty, CodeBuilder... builders) {
        return runBuilderAction(() -> {
            for (CodeBuilder b : throwOnNull(builders))
                appendCode(ignoreEmpty, b);
        });
    }
    public CodeBuilder appendLine(boolean ignoreEmpty, int indentDelta, CodeBuilder... builders) {
        return runBuilderAction(() -> {
            for (CodeBuilder b : throwOnNull(builders))
                appendCode(ignoreEmpty, indentDelta, b);
        });
    }

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
        boolean onNewLine = indentDelta != null;
        if (onNewLine && indentDelta < 0)
            throw new IllegalArgumentException("indentDelta < 0");
        if (delimiter != null) {
            if (firstDelimitedItem)
                firstDelimitedItem = false;
            else
                this.fragments.add(delimiter.withoutIndentation());
        }
        if (onNewLine && !isEmpty())
            fragments.add(NEW_LINE);
        if (prefix != null)
            fragments.add(prefix);
        fragments.add(onNewLine ? fragment.withIndentationDelta(indentDelta) : fragment.withoutIndentation());
        if (suffix != null)
            fragments.add(suffix);
    }

    // Start putting the given delimiter between items (delimiter isn't added before the next item)
    // "a,b,c,d" == new CodeBuilder().beginDelimiter(",").append("a").append("b").append("c", "d").toCode();
    public CodeBuilder beginDelimiter(String delimiter) {
        if (throwOnNull(delimiter).isEmpty()) return endDelimiter();
        return beginDelimiter(fromString(delimiter));
    }

    public CodeBuilder beginDelimiter(Code delimiter) {
        return runBuilderAction(() -> setDelimiter(throwOnNull(delimiter)));
    }

    // Stop using delimiter (the default)
    public CodeBuilder endDelimiter() {
        return runBuilderAction(() -> setDelimiter(null));
    }

    private void setDelimiter(Code delimiter) {
        this.delimiter = delimiter;
        this.firstDelimitedItem = delimiter != null;
    }

    // start adding the given prefix to appended items (without indentation)
    public CodeBuilder beginPrefix(String prefix) {
        return beginPrefix(fromString(throwOnNull(prefix)));
    }
    // start adding the given prefix to appended items (without indentation)
    public CodeBuilder beginPrefix(Code prefix) {
        return runBuilderAction(() -> this.prefix = throwOnNull(prefix).withoutIndentation());
    }
    // stop adding prefixes to appended items (the default)
    public CodeBuilder endPrefix() {
        return runBuilderAction(() -> prefix = null);
    }

    // start adding the given suffix to appended items (without indentation)
    public CodeBuilder beginSuffix(String suffix) {
        return beginSuffix(fromString(throwOnNull(suffix)));
    }
    // start adding the given suffix to appended items (without indentation)
    public CodeBuilder beginSuffix(Code suffix) {
        return runBuilderAction(() -> this.suffix = throwOnNull(suffix).withoutIndentation());
    }
    // stop adding suffixes to appended items (the default)
    public CodeBuilder endSuffix() {
        return runBuilderAction(() -> this.suffix = null);
    }

    // Begin a conditional branch: if the given boolean is false, ignore all actions until a call to endConditional is
    // made. All actions are suspended except nested calls to beginConditional and endConditional
    // "ac" == new CodeBuilder.append("a").beginConditional(false).append("b").endConditional().append("c").toCode();
    public CodeBuilder beginConditional(boolean includeNext) {
        conditionalBlocks.push(isActive() && includeNext);
        return this;
    }

    // End the innermost conditional branch, throws an IllegalStateException if there are no conditional branches
    // (the number of calls to endConditional are greater than the number of calls to beginConditional)
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
        return indentation.string() + fragments.stream().map(c -> c.toCode(indentation)).collect(Collectors.joining());
    }

    private <T> T throwOnNull(T obj) {
        if (obj == null)
            throw new IllegalArgumentException("CodeBuilder: null argument");
        return obj;
    }
}
