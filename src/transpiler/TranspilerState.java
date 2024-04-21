package transpiler;

import grammar.gen.ConfluxParser;
import java_builder.ClassBuilder;
import java_builder.InterfaceBuilder;

import java.util.List;
import java.util.Map;

// Represents the state of the transpiler, lookup methods should return null if the object to search for doesn't exist
// When adding classes or interfaces, they must have an identifier so that they can be looked up later
public interface TranspilerState {
    String lookupMainClassId();

    String lookupPackageId();

    ClassBuilder lookupClass(String identifier);

    InterfaceBuilder lookupInterface(String identifier);

    ConfluxParser.ProgramContext lookupSource(String fileName);

    void setMainClassId(String id);

    void setPackageId(String id);

    void addClass(ClassBuilder builder);

    void addInterface(InterfaceBuilder builder);

    void addSource(String fileName, ConfluxParser.ProgramContext source);

    List<ClassBuilder> getClasses();

    List<InterfaceBuilder> getInterfaces();

    Map<String, ConfluxParser.ProgramContext> getSources();

    // return true if the transpiler state contains a java class or interface with the given id
    default boolean doesJavaIdExist(String id) { return lookupClass(id) != null || lookupInterface(id) != null; }
}
