package java_builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java_builder.Code.fromString;

/**
 * Builder for java methods. All the 'setter' methods in this class returns the instance for chaining. All methods in
 * this class throw an {@link IllegalArgumentException} for null parameters. The toCode method requires the identifier
 * to be set before the call, otherwise an {@link IllegalStateException} will be thrown.
 */
public class MethodBuilder implements Code {
    private Code returnType;
    private Code identifier;
    private final List<Code> modifiers;
    private final List<Parameter> parameters;
    private final List<Code> statements;
    private final boolean generateBody;

    /**
     * Create a MethodBuilder with generateBody set to true
     */
    public MethodBuilder() { this(true); }

    /**
     * Create a MethodBuilder with the given value for generateBody
     * @param generateBody If set to true, the toCode method will include statements in braces following the header
     *                     If set to false, the toCode method will put a semicolon after header and adding statements
     *                     will result in an {@link IllegalStateException}.
     */
    public MethodBuilder(boolean generateBody) {
        modifiers = new ArrayList<>();
        parameters = new ArrayList<>();
        statements = new ArrayList<>();
        this.generateBody = generateBody;
    }

    /**
     * Create a MethodBuilder using all the attributes of the given MethodBuilder, with generateBody set to true
     * @param source the MethodBuilder to copy from, the attributes themselves are not copied.
     */
    public MethodBuilder(MethodBuilder source) { this(true, source); }

    /**
     * Create a MethodBuilder using all the attributes of the given MethodBuilder
     * @param generateBody If set to true, the toCode method will include statements in braces following the header
     *                     If set to false, the toCode method will put a semicolon after header and adding statements
     *                     will result in an {@link IllegalStateException}.
     * @param source the MethodBuilder to copy from, the attributes themselves are not copied.
     */
    public MethodBuilder(boolean generateBody, MethodBuilder source) {
        this.returnType = throwOnNull(source).returnType;
        this.identifier = source.identifier;
        this.modifiers = new ArrayList<>(source.modifiers);
        this.parameters = new ArrayList<>(source.parameters);
        this.statements = new ArrayList<>(source.statements);
        this.generateBody = generateBody;
    }

    /////////////////// Setters ///////////////////

    public MethodBuilder addModifier(String modifier) {
        return addModifier(fromString(throwOnNull(modifier)));
    }
    public MethodBuilder addModifier(Code modifier) {
        modifiers.add(throwOnNull(modifier));
        return this;
    }
    public MethodBuilder setReturnType(String returnType) {
        return setReturnType(fromString(throwOnNull(returnType)));
    }
    public MethodBuilder setReturnType(Code returnType) {
        this.returnType = throwOnNull(returnType);
        return this;
    }
    public MethodBuilder setIdentifier(String identifier) {
        return setIdentifier(fromString(throwOnNull(identifier)));
    }
    public MethodBuilder setIdentifier(Code identifier) {
        this.identifier = throwOnNull(identifier);
        return this;
    }

    public MethodBuilder addParameter(String type, String name) {
        return addParameter(new Parameter(throwOnNull(type), throwOnNull(name)));
    }
    public MethodBuilder addParameter(Code type, Code name) {
        return addParameter(new Parameter(throwOnNull(type).toCode(), throwOnNull(name).toCode()));
    }
    public MethodBuilder addParameter(Parameter parameter) {
        parameters.add(throwOnNull(parameter));
        return this;
    }

    public MethodBuilder addStatement(String statement) {
        return addStatement(statements.size(), statement);
    }
    public MethodBuilder addStatement(int index, String statement) {
        return addStatement(index, fromString(throwOnNull(statement)));
    }
    public MethodBuilder addStatement(Code statement) {
        return addStatement(statements.size(), statement);
    }
    public MethodBuilder addStatement(int index, Code statement) {
        if (!generateBody) {
            throw new IllegalStateException("MethodBuilder: cannot add statement, no body will be generated for this method");
        }
        statements.add(index, throwOnNull(statement));
        return this;
    }

    /////////////////// Generate Code ///////////////////

    @Override
    public String toCode(Indentation indentation) {
        throwOnNull(indentation);
        if (identifier == null) {
            throw new IllegalStateException("MethodBuilder: cannot generate code, missing method identifier");
        }
        return new CodeBuilder()
                .beginDelimiter(" ")
                .append(modifiers)
                .beginConditional(returnType != null).append(returnType).endConditional()
                .append(identifier)
                .endDelimiter()
                .append("(").beginDelimiter(", ").append(parameters).endDelimiter().append(")")
                .beginConditional(!generateBody).append(";").endConditional()
                .beginConditional(generateBody)
                .append(" {")
                .appendLine(1, statements)
                .appendLine(0, "}")
                .endConditional()
                .toCode(indentation);
    }

    /////////////////// Getters ///////////////////

    public Code getReturnType()            { return returnType; }
    public Code getIdentifier()            { return identifier; }
    public List<Code> getModifiers()       { return new ArrayList<>(modifiers); }
    public List<Parameter> getParameters() { return new ArrayList<>(parameters); }
    public List<Code> getStatements()      { return new ArrayList<>(statements); }
    public boolean generatesBody()         { return generateBody; }

    public boolean signatureEquals(MethodBuilder other) {
        throwOnNull(other);
        if (this.identifier == null || other.identifier == null) {
            throw new IllegalArgumentException("MethodBuilder: Cannot compare signatures, null identifier");
        }
        return this.identifier.toCode().equals(other.identifier.toCode()) && this.parameters.equals(other.parameters);
    }

    @Override
    public String toString() {
        return "MethodBuilder{" +
               "returnType=" + returnType +
               ", identifier=" + identifier +
               ", modifiers=" + modifiers +
               ", parameters=" + parameters +
               ", statements=" + statements +
               ", generateBody=" + generateBody +
               '}';
    }

    private <T> T throwOnNull(T obj) {
        if (obj == null)
            throw new IllegalArgumentException("MethodBuilder: null argument");
        return obj;
    }

    /////////////////// Parameters ///////////////////

    /**
     * A method parameter that consists of a type and an identifier. Equality is based on the type only
     */
    public static final class Parameter implements Code {
        private final String argType;
        private final String argId;
        public Parameter(String argType, String argId) {
            this.argType = Objects.requireNonNull(argType);
            this.argId = Objects.requireNonNull(argId);
        }
        @Override
        public String toCode(Indentation indentation) {
            return argType + " " + argId;
        }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Parameter parameter = (Parameter) o;
            return Objects.equals(argType, parameter.argType);
        }
        @Override
        public int hashCode() { return Objects.hash(argType); }
        public String getArgType() { return argType; }
        public String getArgId()   { return argId; }
        @Override
        public String toString() { return "Parameter[argType=" + argType + ", argId=" + argId + ']'; }
    }
}
