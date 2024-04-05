package java_builder;

import java.util.ArrayList;
import java.util.List;

import static java_builder.Code.fromString;

public class InterfaceBuilder implements Code {
    private final List<Code> imports;
    private final List<Code> modifiers;
    private Code identifier;
    private final List<Code> extendedInterfaces;
    private final List<MethodBuilder> methods;

    public InterfaceBuilder() {
        imports = new ArrayList<>();
        modifiers = new ArrayList<>();
        extendedInterfaces = new ArrayList<>();
        methods = new ArrayList<>();
    }

    /////////////////// Setters ///////////////////

    public InterfaceBuilder addImport(String imp) { return addImport(fromString(imp)); }
    public InterfaceBuilder addImport(Code imp) {
        imports.add(imp);
        return this;
    }
    public InterfaceBuilder addModifier(String modifier) { return addModifier(fromString(modifier)); }
    public InterfaceBuilder addModifier(Code modifier) {
        modifiers.add(modifier);
        return this;
    }
    public InterfaceBuilder setIdentifier(String identifier) { return setIdentifier(fromString(identifier)); }
    public InterfaceBuilder setIdentifier(Code identifier) {
        this.identifier = identifier;
        return this;
    }

    public InterfaceBuilder addExtendedInterface(String ei) { return addExtendedInterface(fromString(ei)); }
    public InterfaceBuilder addExtendedInterface(Code extendedInterface) {
        extendedInterfaces.add(extendedInterface);
        return this;
    }

    public InterfaceBuilder addMethod(MethodBuilder method) {
        methods.add(method);
        return this;
    }

    /////////////////// Generate Code ///////////////////

    @Override
    public String toCode(Indentation indentation) {
        if (identifier == null) {
            throw new IllegalStateException("Cannot generate code: Missing class identifier");
        }
        Code header = new CodeBuilder()
                .beginDelimiter(" ").append(modifiers).append("interface").append(identifier)
                .beginConditional(!extendedInterfaces.isEmpty())
                    .append("extends ").beginDelimiter(", ").append(extendedInterfaces).endDelimiter()
                .endConditional().append(" {");

        return new CodeBuilder()
                .beginPrefix("import ").beginSuffix(";\n").append(imports).endPrefix().endSuffix()
                .appendLine(0, header)
                .appendLine(1, methods)
                .appendLine(0, "}")
                .toCode(indentation);
    }

    /////////////////// Getters ///////////////////

    public List<Code> getModifiers() { return new ArrayList<>(modifiers); }
    public Code getIdentifier() { return identifier; }
    public List<Code> getExtendedInterfaces() { return new ArrayList<>(extendedInterfaces); }
    public List<MethodBuilder> getMethods() { return new ArrayList<>(methods); }

    @Override
    public String toString() {
        return "InterfaceBuilder{" +
               "imports=" + imports +
               ", modifiers=" + modifiers +
               ", identifier=" + identifier +
               ", extendedInterfaces=" + extendedInterfaces +
               ", methods=" + methods +
               '}';
    }
}
