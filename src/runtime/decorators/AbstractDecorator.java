package runtime.decorators;

public abstract class AbstractDecorator<T> implements Decorator<T> {
    private Decorator<T> previous;
    private Decorator<T> next;

    protected final T getBase() {
        return previous.getDecoratedInstance();
    }

    @Override
    public final Decorator<T> getPrevious() { return previous; }

    @Override
    public final void setPrevious(Decorator<T> previous) { this.previous = previous; }

    @Override
    public final Decorator<T> getNext() { return next; }

    @Override
    public final void setNext(Decorator<T> next) { this.next = next; }
}
