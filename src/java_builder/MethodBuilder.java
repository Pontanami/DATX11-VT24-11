package java_builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java_builder.Code.fromString;

public class MethodBuilder implements Code {
    private Code returnType;
    private Code identifier;
    private final List<Code> modifiers;
    private final List<Parameter> parameters;
    private final List<Code> statements;
    private final boolean generateBody;

    public MethodBuilder() { this(true); }

    public MethodBuilder(boolean generateBody) {
        modifiers = new ArrayList<>();
        parameters = new ArrayList<>();
        statements = new ArrayList<>();
        this.generateBody = generateBody;
    }

    public MethodBuilder(MethodBuilder source) { this(true, source); }

    public MethodBuilder(boolean generateBody, MethodBuilder source) {
        this.returnType = source.returnType;
        this.identifier = source.identifier;
        this.modifiers = new ArrayList<>(source.modifiers);
        this.parameters = new ArrayList<>(source.parameters);
        this.statements = new ArrayList<>(source.statements);
        this.generateBody = generateBody;
    }

    /////////////////// Setters ///////////////////

    public MethodBuilder addModifier(String modifier) {
        return addModifier(fromString(modifier));
    }
    public MethodBuilder addModifier(Code modifier) {
        modifiers.add(modifier);
        return this;
    }
    public MethodBuilder setReturnType(String returnType) {
        return setReturnType(fromString(returnType));
    }
    public MethodBuilder setReturnType(Code returnType) {
        this.returnType = returnType;
        return this;
    }
    public MethodBuilder setIdentifier(String identifier) {
        return setIdentifier(fromString(identifier));
    }
    public MethodBuilder setIdentifier(Code identifier) {
        this.identifier = identifier;
        return this;
    }

    public MethodBuilder addParameter(String type, String name) {
        return addParameter(new Parameter(type, name));
    }
    public MethodBuilder addParameter(Code type, Code name) {
        return addParameter(new Parameter(type.toCode(), name.toCode()));
    }
    public MethodBuilder addParameter(Parameter parameter) {
        parameters.add(parameter);
        return this;
    }

    public MethodBuilder addStatement(String statement) {
        return addStatement(statements.size(), statement);
    }
    public MethodBuilder addStatement(int index, String statement) {
        return addStatement(index, fromString(statement));
    }
    public MethodBuilder addStatement(Code statement) {
        return addStatement(statements.size(), statement);
    }
    public MethodBuilder addStatement(int index, Code statement) {
        if (!generateBody) {
            throw new IllegalStateException("Cannot add statement, no body will be generated for this method");
        }
        statements.add(index, statement);
        return this;
    }

    /////////////////// Generate Code ///////////////////

    @Override
    public String toCode(Indentation indentation) {
        if (identifier == null) {
            throw new IllegalStateException("Cannot generate code: Missing method identifier");
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

    public Code getReturnType()       { return returnType; }
    public Code getIdentifier()       { return identifier; }
    public List<Code> getModifiers()  { return new ArrayList<>(modifiers); }
    public List<Parameter> getParameters() { return new ArrayList<>(parameters); }
    public List<Code> getStatements() { return new ArrayList<>(statements); }
    public boolean generatesBody()    { return generateBody; }

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

    /////////////////// Parameters ///////////////////

    // method/constructor parameter that contains type and identifier, equals is based on the type only
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
