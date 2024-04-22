package java_builder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java_builder.Code.fromString;

/**
 * Builder for java classes. All the 'setter' methods in this class returns the instance for chaining. All methods in
 * this class throw an {@link IllegalArgumentException} for null parameters. The toCode method requires the identifier
 * to be set before the call, otherwise an {@link IllegalStateException} will be thrown.
 */
public class ClassBuilder implements Code {
    private Code packageName;
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

    public ClassBuilder setPackage(String pkg) { return setPackage(fromString(throwOnNull(pkg))); }
    public ClassBuilder setPackage(Code pkg) {
        packageName = throwOnNull(pkg);
        return this;
    }

    public ClassBuilder addImport(String imp) { return addImport(fromString(throwOnNull(imp))); }
    public ClassBuilder addImport(Code imp) {
        imports.add(throwOnNull(imp));
        return this;
    }

    public ClassBuilder addModifier(String modifier) { return addModifier(fromString(throwOnNull(modifier))); }
    public ClassBuilder addModifier(Code modifier) {
        modifiers.add(throwOnNull(modifier));
        return this;
    }

    public ClassBuilder setIdentifier(String identifier) { return setIdentifier(fromString(throwOnNull(identifier))); }
    public ClassBuilder setIdentifier(Code identifier) {
        this.identifier = throwOnNull(identifier);
        return this;
    }

    public ClassBuilder addImplementedInterface(String iface) {
        return addImplementedInterface(fromString(throwOnNull(iface)));
    }
    public ClassBuilder addImplementedInterface(Code implementedInterface) {
        implementedInterfaces.add(throwOnNull(implementedInterface));
        return this;
    }

    public ClassBuilder addField(String field) { return addField(fromString(throwOnNull(field))); }
    public ClassBuilder addField(Code field) {
        fields.add(throwOnNull(field));
        return this;
    }

    public ClassBuilder addConstructor(MethodBuilder constructor) {
        if (throwOnNull(constructor).getIdentifier() == null) {
            if (identifier == null)
                throw new IllegalArgumentException("Cannot add constructor, missing identifier");
            else
                constructor.setIdentifier(identifier);
        }
        constructors.add(constructor);
        return this;
    }

    public ClassBuilder addMethod(MethodBuilder method) {
        if (throwOnNull(method).getIdentifier() == null) {
            throw new IllegalArgumentException("Cannot add method, missing identifier");
        }
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
                .beginDelimiter(" ").append(modifiers).append("class").append(identifier)
                .beginConditional(!implementedInterfaces.isEmpty())
                    .append("implements ").beginDelimiter(", ").append(implementedInterfaces).endDelimiter()
                .endConditional().endDelimiter().append(" {");

        return new CodeBuilder()
                .beginConditional(packageName != null)
                    .append("package ").append(packageName).append(";").newLine()
                .endConditional()
                .beginPrefix("import ").beginSuffix(";").appendLine(0, imports).endPrefix().endSuffix()
                .beginConditional(!imports.isEmpty()).newLine().endConditional()
                .appendLine(0, header)
                .beginDelimiter("\n")
                .appendLine(true, 0, members(fields), members(constructors), members(methods))
                .endDelimiter().appendLine(0, "}")
                .toCode(indentation);
    }

    private CodeBuilder members(Collection<? extends Code> members) {
        return new CodeBuilder().appendLine(1, members);
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

    private <T> T throwOnNull(T obj) {
        if (obj == null)
            throw new IllegalArgumentException("ClassBuilder: null argument");
        return obj;
    }
}
