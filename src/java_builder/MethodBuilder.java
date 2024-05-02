package java_builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java_builder.Code.fromString;

/**
 * Builder for java methods. All the 'setter' methods in this class returns the instance for chaining. All methods in
 * this class throw an {@link IllegalArgumentException} for null parameters unless otherwise specified. The toCode
 * method requires the identifier to be set before the call, otherwise an {@link IllegalStateException} will be thrown.
 */
public class MethodBuilder implements Code {
    private Code returnType;
    private Code identifier;
    private final List<Code> modifiers;
    private final List<Parameter> parameters;
    private final List<Code> statements;
    private boolean generateBody;

    /**
     * Create a MethodBuilder with generateBody set to true
     */
    public MethodBuilder() { this(true); }

    /**
     * Create a MethodBuilder with the given value for generateBody
     * @param generateBody If set to true, the toCode method will include statements in braces following the header If
     *                     set to false, the toCode method will put a semicolon after header and adding statements will
     *                     result in an {@link IllegalStateException}.
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
     * @param generateBody If set to true, the toCode method will include statements in braces following the header If
     *                     set to false, the toCode method will put a semicolon after header and adding statements will
     *                     result in an {@link IllegalStateException}.
     * @param source       the MethodBuilder to copy from, the attributes themselves are not copied.
     */
    public MethodBuilder(boolean generateBody, MethodBuilder source) {
        this.returnType = throwOnNull(source).returnType;
        this.identifier = source.identifier;
        this.modifiers = new ArrayList<>(source.modifiers);
        this.parameters = new ArrayList<>(source.parameters);
        this.statements = generateBody ? new ArrayList<>(source.statements) : null;
        this.generateBody = generateBody;
    }

    /////////////////// Setters ///////////////////

    /**
     * Set the value of {@code generateBody} to the given value. If the new value of {@code generateBody} is false,
     * all statements will be cleared from this {@code MethodBuilder}.
     * @param generateBody the new value for {@code generateBody}
     * @return this {@code MethodBuilder}, for chaining
     */
    public MethodBuilder setGenerateBody(boolean generateBody) {
        if (!generateBody) {
            this.statements.clear();
        }
        this.generateBody = generateBody;
        return this;
    }

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

    public MethodSignature getSignature() {
        if (identifier == null) {
            throw new IllegalArgumentException(
                    "Cannot create method signature: this MethodBuilder does not have an identifier"
            );
        }
        return new MethodSignature(identifier.toCode(), parameters);
    }

    public boolean signatureEquals(MethodBuilder other) {
        throwOnNull(other);
        if (this.identifier == null || other.identifier == null) {
            throw new IllegalArgumentException("MethodBuilder: Cannot compare signatures, null identifier");
        }
        return getSignature().equals(other.getSignature());
    }

    /**
     * Returns a new {@code MethodBuilder}, that has the same return type, identifier, parameters, and optionally the
     * same modifiers, as this {@code MethodBuilder}. The changes in the returned {@code MethodBuilder} will not affect
     * this {@code MethodBuilder} and vice versa.
     * @param copyModifiers - if true the returned {@code MethodBuilder} will possess the same modifiers as this
     * {@code MethodBuilder}
     * @return the copied {@code MethodBuilder}
     */
    public MethodBuilder copySignature(boolean copyModifiers) {
        MethodBuilder result = new MethodBuilder(generateBody)
                .setReturnType(returnType.toCode())
                .setIdentifier(identifier.toCode());

        if (copyModifiers) {
            modifiers.forEach(m -> result.addModifier(m.toCode()));
        }
        parameters.forEach(result::addParameter);
        return result;
    }

    /**
     * Create a 'delegate' method from this method. Where the method id for delegate is the same as this method.
     * Any modifier on this method will be copied to the returned method.
     * @param delegateId the identifier for the object which handles the method implementation
     * @return a method with the same signature and modifiers as this method, containing a single statement which
     * forwards the call to {@code delegateId}. If this method is subsequently modified, the modifications will not be
     * reflected in the returned method.
     */
    public MethodBuilder delegateMethod(String delegateId) {
        return delegateMethod(delegateId, null, true);
    }

    /**
     * Create a 'delegate' method from this method.
     * @param delegateId the identifier for the object which handles the method implementation
     * @param delegateMethodId the identifier of the method of the delegate that will handle the call, if null
     *                         the same identifier as this method is used
     * @param copyModifiers if true, the returned method will contain the same modifiers as this method
     * @return a method with the same signature, containing a single statement which forwards the call to
     * {@code delegateId}. If this method is subsequently modified, the modifications will not be reflected in the
     * returned method.
     */
    public MethodBuilder delegateMethod(String delegateId, String delegateMethodId, boolean copyModifiers) {
        String returnType = this.returnType.toCode();
        String identifier = this.identifier.toCode();
        MethodBuilder implementation = new MethodBuilder()
                .setReturnType(returnType)
                .setIdentifier(identifier);
        if (copyModifiers) {
            modifiers.forEach(m -> implementation.addModifier(m.toCode()));
        }
        CodeBuilder delegateCall = new CodeBuilder()
                .beginConditional(!returnType.equals("void")).append("return ").endConditional()
                .append(delegateId)
                .append(".")
                .append(delegateMethodId == null ? identifier : delegateMethodId)
                .append("(")
                .beginDelimiter(", ");
        parameters.forEach(param -> {
            implementation.addParameter(param);
            delegateCall.append(param.argId());
        });

        delegateCall.endDelimiter().append(");");
        return implementation.addStatement(delegateCall);
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

    /**
     * An immutable representation of a method signature consisting of an identifier and a list of parameters.
     * Equality is based on the identifier and parameter types only (not parameter identifiers)
     */
    public record MethodSignature(String identifier, List<Parameter> parameterTypes) {
        public MethodSignature(String identifier, List<Parameter> parameterTypes) {
            this.identifier = identifier;
            this.parameterTypes = List.copyOf(parameterTypes);
        }
    }

    /**
     * An immutable representation of a method parameter consisting of a type and an identifier.
     * Equality is based on the type only (not the identifier).
     */
    public record Parameter(String argType, String argId) implements Code {
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
        @Override
        public String toString() { return "Parameter[argType=" + argType + ", argId=" + argId + ']'; }
    }
}
