package runtime.decorators;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public abstract class _AbstractDecorator implements _Decorator {
    private _Decorator previous;
    private _Decorator next;

    public <R> R _invoke(Class<R> returnType, String methodName, Class<?>[] argTypes, Object[] args) {
        try {
            return returnType.cast(getClass().getMethod(methodName, argTypes).invoke(this, args));
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        } catch (IllegalAccessException e) {
            throw new AssertionError("This is unreachable - getMethod only returns public methods");
        } catch (NoSuchMethodException e) {
            // Method didn't exist on this decorator, forward the call to the previous decorator
            assert previous != null : "The first decorator in the chain is missing the requested method: "
                                      + methodName + Arrays.toString(argTypes);
            return previous._invoke(returnType, methodName, argTypes, args);
        }
    }

    @Override
    public final _Decorator _getPrevious() { return previous; }

    @Override
    public final void _setPrevious(_Decorator previous) { this.previous = previous; }

    @Override
    public final _Decorator _getNext() { return next; }

    @Override
    public final void _setNext(_Decorator next) { this.next = next; }
}
