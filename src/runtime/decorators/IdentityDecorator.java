package runtime.decorators;

// The first decorator in the chain of decorators
public final class IdentityDecorator<T> implements Decorator<T> {
    private final T base;
    private Decorator<T> next;

    public IdentityDecorator(T base) { this.base = base; }

    @Override
    public T getDecoratedInstance() {
        return base;
    }

    @Override
    public void setNext(Decorator<T> next) { this.next = next; }

    @Override
    public Decorator<T> getNext() { return next; }

    @Override
    public void setPrevious(Decorator<T> previous) { }

    @Override
    public Decorator<T> getPrevious() { return null; }
}
