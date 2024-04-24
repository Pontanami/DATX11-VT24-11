package runtime.decorators;

public interface _Decorator<T> {
    T _getDecoratedInstance();

    void _setNext(_Decorator<T> next);

    _Decorator<T> _getNext();

    _Decorator<T> _getPrevious();

    void _setPrevious(_Decorator<T> previous);
}
