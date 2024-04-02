package java_builder;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java_builder.Code.fromString;

// Builder for putting together code fragments, supports delimiters, setting indentation and conditional appends.
public class CodeBuilder implements Code {
    private List<Code> fragments;
    private Deque<Boolean> conditionalBlocks;
    private boolean indentChildren;
    private int indentDelta;
    private Code delimiter;
    private boolean firstDelimitedItem;

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
    }

    public CodeBuilder append(String... fragments) {
        return runBuilderAction(() -> Stream.of(notNull(fragments)).map(Code::fromString).forEach(this::appendCode));
    }

    public CodeBuilder append(Code... fragments) {
        return runBuilderAction(() -> Stream.of(notNull(fragments)).forEach(this::appendCode));
    }

    public CodeBuilder append(Collection<? extends Code> fragments) {
        return runBuilderAction(() -> notNull(fragments).forEach(this::appendCode));
    }

    public CodeBuilder append(Supplier<String> fragment) {
        return runBuilderAction(() -> appendCode(fromString(notNull(fragment).get())));
    }

    private void appendCode(Code fragment) {
        if (delimiter != null) {
            if (firstDelimitedItem)
                firstDelimitedItem = false;
            else
                this.fragments.add(delimiter.withoutIndentation());
        }
        this.fragments.add(indentChildren
                ? fragment.withIndentationDelta(indentDelta)
                : fragment.withoutIndentation()
        );
    }

    // Start putting the given delimiter between items (delimiter isn't added before the next item)
    // "a,b,c,d" == new CodeBuilder().beginDelimiter(",").append("a").append("b").append("c", "d").toCode();
    public CodeBuilder beginDelimiter(String delimiter) {
        return beginDelimiter(fromString(notNull(delimiter)));
    }

    public CodeBuilder beginDelimiter(Code delimiter) {
        return runBuilderAction(() -> setDelimiter(notNull(delimiter)));
    }

    public CodeBuilder endDelimiter() {
        return runBuilderAction(() -> setDelimiter(null));
    }

    private void setDelimiter(Code delimiter) {
        this.delimiter = delimiter;
        this.firstDelimitedItem = delimiter != null;
    }

    // if the given boolean is false, ignore all actions until a call to endConditional is made. All mutating methods
    // are suspended except nested calls to beginConditional and endConditional
    // "ac" == new CodeBuilder.append("a").beginConditional(false).append("b").endConditional().append("c").toCode();
    public CodeBuilder beginConditional(boolean includeNext) {
        conditionalBlocks.push(isActive() && includeNext);
        return this;
    }

    // End the innermost conditional branch, if the number of calls to endConditional are greater than the number of
    // calls to begin conditional an IllegalStateException is thrown
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
    public CodeBuilder endIndentChildren() {
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

    private <T> T notNull(T obj) {
        if (obj == null)
            throw new IllegalArgumentException("CodeBuilder: null argument");
        return obj;
    }
}
