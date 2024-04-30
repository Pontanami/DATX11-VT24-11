package runtime.decorators;

import java.lang.reflect.InvocationTargetException;

// An identity decorator, i.e. a decorator that doesn't extend the behavior of the base object
public final class _IdentityDecorator extends _AbstractDecorator {
    private final Object base;

    public _IdentityDecorator(Object base) { this.base = base; }

    @Override
    public <R> R _invoke(Class<R> returnType, String methodName, Class<?>[] argTypes, Object[] args) {
        try {
            return returnType.cast(base.getClass().getMethod(methodName, argTypes).invoke(base, args));
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getTargetException());
        } catch (IllegalAccessException e) {
            throw new AssertionError("This is unreachable - getMethod only returns public methods");
        } catch (NoSuchMethodException e) {
            throw new AssertionError("This is unreachable - the transpiler ensures that the first" +
                                     " decorator in the chain contains all the requested methods");
        }
    }
}
