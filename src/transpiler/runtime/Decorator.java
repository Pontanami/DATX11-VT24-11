package transpiler.runtime;

public interface Decorator<T> {
    T getDecoratedInstance();

    void setNext(Decorator<T> next);

    Decorator<T> getNext();

    Decorator<T> getPrevious();

    void setPrevious(Decorator<T> previous);
}
