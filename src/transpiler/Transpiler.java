package transpiler;

import java.util.*;
import java.util.function.BiConsumer;

import grammar.gen.*;
import java_builder.*;
import org.antlr.v4.runtime.tree.ParseTree;
import transpiler.tasks.TaskQueue;
import transpiler.tasks.TaskQueue.Priority;
import transpiler.tasks.TranspilerTask;

public class Transpiler {
    private final TranspilerState state;

    public Transpiler() {
        state = new State();
    }

    public void addSource(String fileName, ParseTree source) {
        state.addSource(fileName, source);
    }

    // Transpile all the sources, return a map where the keys are output file names, and the values are java code
    public Map<String, String> transpile() {

        // TODO: transpile

        Map<String, String> output = new HashMap<>();
        Indentation indent = new SpaceIndentation(3);
        state.forEachOutput((id, topLevelDef) -> output.put(id + ".java", topLevelDef.toCode(indent)));
        return output;
    }


    private static class State implements TranspilerState {
        private final Map<String, ClassBuilder> classes = new HashMap<>();
        private final Map<String, InterfaceBuilder> interfaces = new HashMap<>();
        private final Map<String, ParseTree> sources = new HashMap<>();

        public ClassBuilder lookupClass(String identifier) {
            return classes.get(identifier);
        }
        public InterfaceBuilder lookupInterface(String identifier) {
            return interfaces.get(identifier);
        }
        public ParseTree lookupSource(String fileName) {
            return sources.get(fileName);
        }

        public void addClass(ClassBuilder builder) {
            String identifier = validateId(builder.getIdentifier());
            classes.put(identifier, builder);
        }
        public void addInterface(InterfaceBuilder builder) {
            String identifier = validateId(builder.getIdentifier());
            interfaces.put(identifier, builder);
        }
        public void addSource(String fileName, ParseTree source) {
            if (lookupSource(fileName) != null)
                throw new IllegalArgumentException("source with fileName '" + fileName + "' already exist");
        }

        public void forEachOutput(BiConsumer<? super String, ? super Code> action) {
            new HashMap<>(classes).forEach(action);
            new HashMap<>(interfaces).forEach(action);
        }

        public List<ParseTree> getSources() {
            return new ArrayList<>(sources.values());
        }

        private String validateId(Code id) {
            if (id == null) {
                throw new IllegalArgumentException("Cannot add class or interface: it doesn't have an identifier");
            }
            String identifier = id.toCode();
            if (doesIdExist(identifier)) {
                throw new IllegalArgumentException("Cannot add class or interface: identifier '"
                                                   + identifier + "' already exist");
            }
            return identifier;
        }
    }
}
