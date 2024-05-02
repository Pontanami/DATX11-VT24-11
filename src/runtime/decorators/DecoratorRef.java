package runtime.decorators;

public final class DecoratorRef {
    private final Object decoratedObj;
    private final _Decorator decorator;

    public DecoratorRef(Object decoratedObj, _Decorator decorator) {
        this.decoratedObj = decoratedObj;
        this.decorator = decorator;
    }

    public Object _decoratedObject() {
        return decoratedObj;
    }

    public _Decorator _decorator() {
        return decorator;
    }
}
