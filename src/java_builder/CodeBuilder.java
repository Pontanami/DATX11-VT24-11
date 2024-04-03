package java_builder;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java_builder.Code.fromString;

// Builder for putting together code fragments. All methods throw IllegalArgumentException if given null unless
// the builder is in a conditional branch that isn't executed
public class CodeBuilder implements Code {
    private List<Code> fragments;
    private Deque<Boolean> conditionalBlocks;
    private boolean indentChildren;
    private int indentDelta;
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
        indentChildren = false;
        indentDelta = 0;
        delimiter = null;
        firstDelimitedItem = false;
        prefix = null;
        suffix = null;
    }

    public boolean isEmpty() { return fragments.isEmpty(); }

    public CodeBuilder append(String... fragments) {
        return runBuilderAction(() -> Stream.of(throwOnNull(fragments)).map(Code::fromString).forEach(this::appendCode));
    }

    public CodeBuilder append(Code... fragments) {
        return runBuilderAction(() -> Stream.of(throwOnNull(fragments)).forEach(this::appendCode));
    }

    public CodeBuilder append(Collection<? extends Code> fragments) {
        return runBuilderAction(() -> throwOnNull(fragments).forEach(this::appendCode));
    }

    public CodeBuilder append(Supplier<String> fragment) {
        return runBuilderAction(() -> appendCode(fromString(throwOnNull(fragment).get())));
    }

    public CodeBuilder append(boolean ignoreEmpty, CodeBuilder... builders) {
        return runBuilderAction(() -> Stream.of(throwOnNull(builders)).forEach(b -> appendCode(ignoreEmpty, b)));
    }

    private void appendCode(boolean ignoreEmpty, CodeBuilder builder) {
        throwOnNull(builder);
        if (ignoreEmpty && builder.isEmpty()) return;
        appendCode(builder);
    }

    private void appendCode(Code fragment) {
        throwOnNull(fragment);
        if (delimiter != null) {
            if (firstDelimitedItem)
                firstDelimitedItem = false;
            else
                this.fragments.add(delimiter.withoutIndentation());
        }
        if (prefix != null)
            fragments.add(prefix);
        fragments.add(indentChildren ? fragment.withIndentationDelta(indentDelta) : fragment.withoutIndentation());
        if (suffix != null)
            fragments.add(suffix);
    }

    // Start putting the given delimiter between items (delimiter isn't added before the next item)
    // "a,b,c,d" == new CodeBuilder().beginDelimiter(",").append("a").append("b").append("c", "d").toCode();
    public CodeBuilder beginDelimiter(String delimiter) {
        return runBuilderAction(() -> setDelimiter(fromString(throwOnNull(delimiter))));
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
        return runBuilderAction(() -> setPrefix(throwOnNull(prefix), null));
    }
    // start adding the given prefix to appended items (with indentation adjusted by the given delta)
    public CodeBuilder beginPrefix(String prefix, int indentDelta) {
        return runBuilderAction(() -> setPrefix(throwOnNull(prefix), indentDelta));
    }
    public CodeBuilder endPrefix() {
        return runBuilderAction(() -> setPrefix(null, null));
    }
    private void setPrefix(String prefixStr, Integer indentDelta) {
        this.prefix = prefixStr == null ? null : indentDelta == null
                ? fromString(prefixStr).withoutIndentation()
                : fromString(prefixStr).withIndentationDelta(indentDelta);
    }

    // start adding the given suffix to appended items (without indentation)
    public CodeBuilder beginSuffix(String suffix) {
        return runBuilderAction(() -> setSuffix(throwOnNull(suffix), null));
    }
    // start adding the given suffix to appended items (with indentation adjusted by the given delta)
    public CodeBuilder beginSuffix(String suffix, int indentDelta) {
        return runBuilderAction(() -> setSuffix(throwOnNull(suffix), indentDelta));
    }
    public CodeBuilder endSuffix() {
        return runBuilderAction(() -> setSuffix(null, null));
    }
    private void setSuffix(String suffixStr, Integer indentDelta) {
        this.suffix = suffixStr == null ? null : indentDelta == null
                ? fromString(suffixStr).withoutIndentation()
                : fromString(suffixStr).withIndentationDelta(indentDelta);
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

    // Start using indentation on appended fragments, the given delta is used to increase indentation, must be >= 0
    public CodeBuilder beginIndentItems(int indentDelta) {
        if (indentDelta < 0)
            throw new IllegalArgumentException("indentDelta < 0");
        return runBuilderAction(() -> {
            this.indentChildren = true;
            this.indentDelta = indentDelta;
        });
    }

    // ignore indentation for appended fragments (the default)
    public CodeBuilder endIndentItems() {
        return runBuilderAction(() -> indentChildren = false);
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
