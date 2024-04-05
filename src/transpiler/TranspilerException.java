package transpiler;

public class TranspilerException extends RuntimeException {
    public TranspilerException() {}
    public TranspilerException(String message) { super(message); }
    public TranspilerException(String message, Throwable cause) { super(message, cause); }
    public TranspilerException(Throwable cause) { super(cause); }
    public TranspilerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
