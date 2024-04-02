package java_builder;

import java.util.ArrayList;
import java.util.List;

public class InterfaceBuilder implements Code {
    private final List<Code> imports;
    private final List<Code> modifiers;
    private Code identifier;
    private final List<Code> extendedInterfaces;
    private final List<Code> methods;

    public InterfaceBuilder() {
        imports = new ArrayList<>();
        modifiers = new ArrayList<>();
        extendedInterfaces = new ArrayList<>();
        methods = new ArrayList<>();
    }

    /////////////////// Setters ///////////////////

    public InterfaceBuilder addImport(String imp) { return addImport(i -> imp); }
    public InterfaceBuilder addImport(Code imp) {
        imports.add(imp);
        return this;
    }
    public InterfaceBuilder addModifier(String modifier) { return addModifier(i -> modifier); }
    public InterfaceBuilder addModifier(Code modifier) {
        modifiers.add(modifier);
        return this;
    }
    public InterfaceBuilder setIdentifier(String identifier) { return setIdentifier(i -> identifier); }
    public InterfaceBuilder setIdentifier(Code identifier) {
        this.identifier = identifier;
        return this;
    }

    public InterfaceBuilder addExtendedInterface(String ei) { return addExtendedInterface(i -> ei); }
    public InterfaceBuilder addExtendedInterface(Code extendedInterface) {
        extendedInterfaces.add(extendedInterface);
        return this;
    }

    public InterfaceBuilder addMethod(String method) { return addMethod(i -> i.string() + method); }
    public InterfaceBuilder addMethod(Code method) {
        methods.add(method);
        return this;
    }

    /////////////////// Generate Code ///////////////////

    @Override
    public String toCode(Indentation indentation) {
        if (identifier == null) {
            throw new IllegalStateException("Cannot generate code: Missing interface identifier");
        }
        final StringBuilder result = new StringBuilder(indentation.string());

        imports.forEach(imp -> result.append("import ").append(imp.toCode()).append(";\n"));
        if (!imports.isEmpty()) result.append("\n");

        modifiers.forEach(mod -> result.append(mod.toCode()).append(" "));
        result.append("interface ").append(identifier.toCode());

        if (!extendedInterfaces.isEmpty()) {
            result.append(" extends ");
            for (int i = 0; i < extendedInterfaces.size(); i++) {
                if (i > 0) result.append(", ");
                result.append(extendedInterfaces.get(i).toCode());
            }
        }
        result.append(" {\n");

        final Indentation nextLevel = indentation.adjustLevel(1);
        methods.forEach(m -> result.append(m.toCode(nextLevel)).append("\n"));

        return result.append(indentation.string()).append("}").toString();
    }

    /////////////////// Getters ///////////////////

    public List<Code> getModifiers() { return new ArrayList<>(modifiers); }
    public Code getIdentifier() { return identifier; }
    public List<Code> getExtendedInterfaces() { return new ArrayList<>(extendedInterfaces); }
    public List<Code> getMethods() { return new ArrayList<>(methods); }

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
