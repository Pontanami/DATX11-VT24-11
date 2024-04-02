package transpiler;

import java_builder.ClassBuilder;
import java_builder.Code;
import java_builder.InterfaceBuilder;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.*;

public class Environment {
    private final Map<String, ClassBuilder> classes;
    private final Map<String, InterfaceBuilder> interfaces;
    private final Map<String, ParseTree> sources;

    public Environment() {
        classes = new HashMap<>();
        interfaces = new HashMap<>();
        sources = new HashMap<>();
    }

    public ClassBuilder createClass(String identifier) {
        return createClass(identifier, null);
    }

    public ClassBuilder createClass(String identifier, ParseTree source) {
        validateNewId(identifier);
        ClassBuilder builder = new ClassBuilder().setIdentifier(identifier);
        classes.put(identifier, builder);
        if (source != null)
            sources.put(identifier, source);
        return builder;
    }

    public InterfaceBuilder createInterface(String identifier) {
        return createInterface(identifier, null);
    }

    public InterfaceBuilder createInterface(String identifier, ParseTree source) {
        validateNewId(identifier);
        InterfaceBuilder builder = new InterfaceBuilder().setIdentifier(validateNewId(identifier));
        interfaces.put(identifier, builder);
        if (source != null)
            sources.put(identifier, source);
        return builder;
    }

    public ClassBuilder lookupClass(String identifier) {
        return classes.get(Objects.requireNonNull(identifier));
    }

    public InterfaceBuilder lookupInterface(String identifier) {
        return interfaces.get(Objects.requireNonNull(identifier));
    }

    public Code lookupCode(String identifier) {
        Code code = classes.get(Objects.requireNonNull(identifier));
        return code != null ? code : interfaces.get(identifier);
    }

    public ParseTree lookupSource(String identifier) {
        return sources.get(Objects.requireNonNull(identifier));
    }

    public List<String> getClassIds() {
        return new ArrayList<>(classes.keySet());
    }

    public List<String> getInterfaceIds() {
        return new ArrayList<>(interfaces.keySet());
    }

    public List<String> getSourceIds() {
        return new ArrayList<>(sources.keySet());
    }

    public List<String> getOutputIds() {
        List<String> output = new ArrayList<>(classes.keySet());
        output.addAll(interfaces.keySet());
        return output;
    }

    private String validateNewId(String identifier) {
        if (identifier == null) {
            throw new IllegalArgumentException("identifier is null");
        }
        if (classes.containsKey(identifier)) {
            throw new IllegalArgumentException("A class with id '" + identifier + "' already exist");
        }
        if (interfaces.containsKey(identifier)) {
            throw new IllegalArgumentException("An interface with id '" + identifier + "' already exist");
        }
        return identifier;
    }
}
