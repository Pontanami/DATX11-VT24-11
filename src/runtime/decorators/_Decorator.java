package runtime.decorators;

public interface _Decorator {

    void _setNext(_Decorator next);

    _Decorator _getNext();

    _Decorator _getPrevious();

    void _setPrevious(_Decorator previous);

    <R> R _invoke(Class<R> returnType, String methodName, Class<?>[] argTypes, Object[] args);

    default <R> R _invoke(Class<R> returnType, String methodName) {
        return _invoke(returnType, methodName, new Class[]{}, new Object[]{});
    }
}
