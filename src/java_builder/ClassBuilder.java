package java_builder;

import java.util.ArrayList;
import java.util.List;

import static java_builder.Code.fromString;

public class ClassBuilder implements Code {
    private final List<Code> imports;
    private final List<Code> modifiers;
    private Code identifier;
    private final List<Code> implementedInterfaces;
    private final List<Code> fields;
    private final List<MethodBuilder> constructors;
    private final List<MethodBuilder> methods;

    public ClassBuilder() {
        imports = new ArrayList<>();
        modifiers = new ArrayList<>();
        implementedInterfaces = new ArrayList<>();
        constructors = new ArrayList<>();
        fields = new ArrayList<>();
        methods = new ArrayList<>();
    }

    /////////////////// Setters ///////////////////

    public ClassBuilder addImport(String imp) { return addImport(fromString(imp)); }
    public ClassBuilder addImport(Code imp) {
        imports.add(imp);
        return this;
    }

    public ClassBuilder addModifier(String modifier) { return addModifier(fromString(modifier)); }
    public ClassBuilder addModifier(Code modifier) {
        modifiers.add(modifier);
        return this;
    }

    public ClassBuilder setIdentifier(String identifier) { return setIdentifier(fromString(identifier)); }
    public ClassBuilder setIdentifier(Code identifier) {
        this.identifier = identifier;
        return this;
    }

    public ClassBuilder addImplementedInterface(String iface) { return addImplementedInterface(fromString(iface)); }
    public ClassBuilder addImplementedInterface(Code implementedInterface) {
        implementedInterfaces.add(implementedInterface);
        return this;
    }

    public ClassBuilder addField(String field) { return addField(fromString(field)); }
    public ClassBuilder addField(Code field) {
        fields.add(field);
        return this;
    }

    public ClassBuilder addConstructor(MethodBuilder constructor) {
        constructors.add(constructor);
        return this;
    }

    public ClassBuilder addMethod(MethodBuilder method) {
        methods.add(method);
        return this;
    }

    /////////////////// Generate Code ///////////////////

    @Override
    public String toCode(Indentation indentation) {
        if (identifier == null) {
            throw new IllegalStateException("Cannot generate code: Missing class identifier");
        }
        final StringBuilder result = new StringBuilder(indentation.string());

        imports.forEach(imp -> result.append("import ").append(imp.toCode()).append(";\n"));
        if (!imports.isEmpty()) result.append("\n");

        modifiers.forEach(mod -> result.append(mod.toCode()).append(" "));
        result.append("class ").append(identifier.toCode());

        if (!implementedInterfaces.isEmpty()) {
            result.append(" implements ");
            for (int i = 0; i < implementedInterfaces.size(); i++) {
                if (i > 0) result.append(", ");
                result.append(implementedInterfaces.get(i).toCode());
            }
        }
        result.append(" {\n");

        final Indentation nextLevel = indentation.adjustLevel(1);
        fields.forEach(f -> result.append(f.toCode(nextLevel)).append("\n"));
        if (!fields.isEmpty()) result.append("\n");
        constructors.forEach(c -> result.append(c.toCode(nextLevel)).append("\n"));
        if (!constructors.isEmpty()) result.append("\n");
        methods.forEach(m -> result.append(m.toCode(nextLevel)).append("\n"));

        return result.append(indentation.string()).append("}").toString();
    }

    /////////////////// Getters ///////////////////

    public List<Code> getModifiers() { return new ArrayList<>(modifiers); }
    public Code getIdentifier() { return identifier; }
    public List<Code> getImplementedInterfaces() { return new ArrayList<>(implementedInterfaces); }
    public List<Code> getFields() { return new ArrayList<>(fields); }
    public List<MethodBuilder> getConstructors() { return new ArrayList<>(constructors); }
    public List<MethodBuilder> getMethods() { return new ArrayList<>(methods); }

    @Override
    public String toString() {
        return "ClassBuilder{" +
               "imports=" + imports +
               ", modifiers=" + modifiers +
               ", identifier=" + identifier +
               ", implementedInterfaces=" + implementedInterfaces +
               ", fields=" + fields +
               ", constructors=" + constructors +
               ", methods=" + methods +
               '}';
    }
}
