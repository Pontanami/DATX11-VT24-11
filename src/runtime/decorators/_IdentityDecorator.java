package runtime.decorators;

// An identity decorator that doesn't extend the behavior of the base object
public final class _IdentityDecorator<T> extends _AbstractDecorator<T> {
    private final T base;

    public _IdentityDecorator(T base) { this.base = base; }

    @Override
    public T _getDecoratedInstance() {
        return base;
    }
}
