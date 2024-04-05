package transpiler;

import java_builder.ClassBuilder;
import java_builder.Code;
import java_builder.InterfaceBuilder;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.List;
import java.util.function.BiConsumer;

// Represents the state transpiler
public interface TranspilerState {
    ClassBuilder lookupClass(String identifier);

    InterfaceBuilder lookupInterface(String identifier);

    ParseTree lookupSource(String fileName);

    void addClass(ClassBuilder builder);

    void addInterface(InterfaceBuilder builder);

    void addSource(String fileName, ParseTree source);

    void forEachOutput(BiConsumer<? super String, ? super Code> action);

    List<ParseTree> getSources();

    default boolean doesIdExist(String id) { return lookupClass(id) != null || lookupInterface(id) != null; }
}
