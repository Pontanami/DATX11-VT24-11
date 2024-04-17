package transpiler.runtime;

// manages a chain of decorators on an object of type T
public final class DecoratorHandler<T> {
    private Decorator<T> decorator;

    DecoratorHandler(T base) {
        this.decorator = new IdentityDecorator<>(base);
    }

    // return the topmost (possibly decorated) instance
    public T currentInstance() {
        return decorator.getDecoratedInstance();
    }

    public DecoratorTag addDecorator(Decorator<T> next) {
        decorator.setNext(next);
        next.setPrevious(decorator);
        decorator = next;
        return () -> removeDecorator(next);
    }

    private void removeDecorator(Decorator<T> toRemove) {
        if (decorator.getPrevious() != null && decorator == toRemove) {
            decorator = decorator.getPrevious();
            decorator.setNext(null);
            return;
        }
        Decorator<T> current = decorator;
        while (current.getPrevious() != null) {
            if (current == toRemove) {
                Decorator<T> previous = current.getPrevious();
                Decorator<T> next = current.getNext();
                previous.setNext(next);
                next.setPrevious(previous);
            }
            current = current.getPrevious();
        }
    }
}
