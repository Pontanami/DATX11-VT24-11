package transpiler;

import java_builder.ClassBuilder;
import java_builder.Indentation;
import java_builder.InterfaceBuilder;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.List;
import java.util.Map;

// Represents the state transpiler, lookup methods should return null if the object to search for doesn't exist
// When adding classes or interfaces, they must have an identifier so that they can be looked up later
public interface TranspilerState {
    String lookupMainClassId();

    ClassBuilder lookupClass(String identifier);

    InterfaceBuilder lookupInterface(String identifier);

    ParseTree lookupSource(String fileName);

    void setMainClassId(String id);

    void addClass(ClassBuilder builder);

    void addInterface(InterfaceBuilder builder);

    void addSource(String fileName, ParseTree source);

    List<ClassBuilder> getClasses();

    List<InterfaceBuilder> getInterfaces();

    Map<String, ParseTree> getSources();

    // return true if the transpiler state contains a java class or interface with the given id
    default boolean doesJavaIdExist(String id) { return lookupClass(id) != null || lookupInterface(id) != null; }
}
