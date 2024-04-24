package runtime.decorators;

// manages a chain of decorators on an object of type T
public final class _DecoratorHandler<T> {
    private _Decorator<T> decorator;

    public _DecoratorHandler(T base) {
        this.decorator = new _IdentityDecorator<>(base);
    }

    // return the topmost (possibly decorated) instance
    public T getTopLevelDecorator() {
        return decorator._getDecoratedInstance();
    }

    public DecoratorTag addDecorator(final _Decorator<T> next) {
        decorator._setNext(next);
        next._setPrevious(decorator);
        decorator = next;
        return new DecoratorTag(() -> removeDecorator(next));
    }

    private void removeDecorator(_Decorator<T> toRemove) {
        if (toRemove._getNext() == null) {
            decorator = toRemove._getPrevious();
            decorator._setNext(null);
        } else {
            toRemove._getPrevious()._setNext(toRemove._getNext());
            toRemove._getNext()._setPrevious(toRemove._getPrevious());
        }
    }
}
