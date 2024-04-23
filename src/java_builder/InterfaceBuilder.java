package java_builder;

import java.util.ArrayList;
import java.util.List;

import static java_builder.Code.fromString;

/**
 * Builder for java interfaces. All the 'setter' methods in this class returns the instance for chaining. All methods
 * in this class throw an {@link IllegalArgumentException} for null parameters. The toCode method requires the
 * identifier to be set before the call, otherwise an {@link IllegalStateException} will be thrown.
 */
public class InterfaceBuilder implements Code {
    private Code packageName;
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

    public InterfaceBuilder setPackage(String pkg) { return setPackage(fromString(throwOnNull(pkg))); }
    public InterfaceBuilder setPackage(Code pkg) {
        packageName = throwOnNull(pkg);
        return this;
    }

    public InterfaceBuilder addImport(String imp) { return addImport(fromString(throwOnNull(imp))); }
    public InterfaceBuilder addImport(Code imp) {
        imports.add(throwOnNull(imp));
        return this;
    }
    public InterfaceBuilder addModifier(String modifier) { return addModifier(fromString(throwOnNull(modifier))); }
    public InterfaceBuilder addModifier(Code modifier) {
        modifiers.add(throwOnNull(modifier));
        return this;
    }

    public InterfaceBuilder setIdentifier(String identifier) {
        return setIdentifier(fromString(throwOnNull(identifier)));
    }
    public InterfaceBuilder setIdentifier(Code identifier) {
        this.identifier = throwOnNull(identifier);
        return this;
    }

    public InterfaceBuilder addExtendedInterface(String ei) {
        return addExtendedInterface(fromString(throwOnNull(ei)));
    }
    public InterfaceBuilder addExtendedInterface(Code extendedInterface) {
        extendedInterfaces.add(throwOnNull(extendedInterface));
        return this;
    }

    public InterfaceBuilder addMethod(MethodBuilder method) {
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
                .beginDelimiter(" ").append(modifiers).append("interface").append(identifier)
                .beginConditional(!extendedInterfaces.isEmpty())
                    .append("extends ").beginDelimiter(", ").append(extendedInterfaces).endDelimiter()
                .endConditional().endDelimiter().append(" {");

        return new CodeBuilder()
                .beginConditional(packageName != null)
                    .append("package ").append(packageName).append(";").newLine(0)
                .endConditional()
                .beginPrefix("import ").beginSuffix(";").appendLine(0, imports).endPrefix().endSuffix()
                .beginConditional(!imports.isEmpty()).newLine().endConditional()
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

    private <T> T throwOnNull(T obj) {
        if (obj == null)
            throw new IllegalArgumentException("InterfaceBuilder: null argument");
        return obj;
    }
}
