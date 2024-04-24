package runtime.decorators;

public abstract class _AbstractDecorator<T> implements _Decorator<T> {
    private _Decorator<T> previous;
    private _Decorator<T> next;

    protected final T _getBase() {
        if (previous == null) {
            throw new IllegalStateException("Cannot get base: this decorator doesn't decorate an object");
        }
        return previous._getDecoratedInstance();
    }

    @Override
    public final _Decorator<T> _getPrevious() { return previous; }

    @Override
    public final void _setPrevious(_Decorator<T> previous) { this.previous = previous; }

    @Override
    public final _Decorator<T> _getNext() { return next; }

    @Override
    public final void _setNext(_Decorator<T> next) { this.next = next; }
}
