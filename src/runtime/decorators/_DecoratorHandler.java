package runtime.decorators;

// manages a chain of decorators on an object of type T
public final class _DecoratorHandler<T> {
    private _Decorator decorator;

    public _DecoratorHandler(T base) {
        this.decorator = new _IdentityDecorator<>(base);
    }

    public <R> R callTopDecorator(Class<R> returnType, String methodName, Class<?>[] argTypes, Object[] args) {
        return decorator._invoke(returnType, methodName, argTypes, args);
    }

    public DecoratorTag addDecorator(final _Decorator next) {
        decorator._setNext(next);
        next._setPrevious(decorator);
        decorator = next;
        return new DecoratorTag(() -> removeDecorator(next));
    }

    private void removeDecorator(_Decorator toRemove) {
        if (toRemove == decorator) { // the decorator to remove is the last decorator in the chain
            decorator = toRemove._getPrevious();
            decorator._setNext(null);
        } else {
            toRemove._getPrevious()._setNext(toRemove._getNext());
            toRemove._getNext()._setPrevious(toRemove._getPrevious());
        }
    }
}
