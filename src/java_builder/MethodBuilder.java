package java_builder;

import java.util.ArrayList;
import java.util.List;

import static java_builder.Code.fromString;

public class MethodBuilder implements Code {
    private Code returnType;
    private Code identifier;
    private final List<Code> modifiers;
    private final List<Code> parameters;
    private final List<Code> statements;
    private final boolean generateBody;

    public MethodBuilder() { this(true); }

    public MethodBuilder(boolean generateBody) {
        modifiers = new ArrayList<>();
        parameters = new ArrayList<>();
        statements = new ArrayList<>();
        this.generateBody = generateBody;
    }

    public MethodBuilder(MethodBuilder source) { this(false, source); }

    public MethodBuilder(boolean generateBody, MethodBuilder source) {
        this.returnType = source.returnType;
        this.identifier = source.identifier;
        this.modifiers = new ArrayList<>(source.modifiers);
        this.parameters = new ArrayList<>(source.parameters);
        this.statements = new ArrayList<>(source.statements);
        this.generateBody = generateBody;
    }

    /////////////////// Setters ///////////////////

    public MethodBuilder addModifier(String modifier) { return addModifier(fromString(modifier)); }
    public MethodBuilder addModifier(Code modifier) {
        modifiers.add(modifier);
        return this;
    }
    public MethodBuilder setReturnType(String returnType) { return setReturnType(fromString(returnType)); }
    public MethodBuilder setReturnType(Code returnType) {
        this.returnType = returnType;
        return this;
    }
    public MethodBuilder setIdentifier(String identifier) { return setIdentifier(fromString(identifier)); }
    public MethodBuilder setIdentifier(Code identifier) {
        this.identifier = identifier;
        return this;
    }

    public MethodBuilder addParameter(String type, String name) {
        parameters.add(fromString(type + " " + name));
        return this;
    }
    public MethodBuilder addParameter(Code type, Code name) {
        parameters.add(new CodeBuilder(type).append(" ").append(name));
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
        StringBuilder result = buildHeader(indentation);
        if (!generateBody) {
            return result.toString();
        }
        Indentation nextLevel = indentation.adjustLevel(1);
        statements.forEach(stm -> result.append(stm.toCode(nextLevel)).append("\n"));
        return result.append(indentation.string()).append("}").toString();
    }

    private StringBuilder buildHeader(Indentation indentation) {
        final StringBuilder header = new StringBuilder(indentation.string());
        modifiers.forEach(mod -> header.append(mod.toCode()).append(" "));

        if (returnType != null) {
            header.append(returnType.toCode()).append(" ");
        }
        header.append(identifier.toCode()).append("(");

        for (int i = 0; i < parameters.size(); i++) {
            if (i > 0) header.append(", ");
            header.append(parameters.get(i).toCode());
        }

        if (generateBody) {
            return header.append(") {").append("\n");
        } else {
            return header.append(");");
        }
    }

    /////////////////// Getters ///////////////////

    public Code getReturnType() { return returnType; }
    public Code getIdentifier() { return identifier; }
    public List<Code> getModifiers() { return new ArrayList<>(modifiers); }
    public List<Code> getParameters() { return new ArrayList<>(parameters); }
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
}
