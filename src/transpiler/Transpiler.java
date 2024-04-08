package transpiler;

import java.util.*;

import java_builder.*;
import org.antlr.v4.runtime.tree.ParseTree;
import transpiler.tasks.TaskQueue;

public class Transpiler {
    private final State state;

    public Transpiler() {
        state = new State();
    }

    public void addSource(String fileName, ParseTree source) {
        state.addSource(fileName, source);
    }

    // Transpile all the sources
    public TranspilerOutput transpile() {
        TaskQueue taskQueue = new TaskQueue();

        // TODO: create visitors and transpile, send the task queue to visitors that need it

        taskQueue.runTasks(state);
        return state.createOutput(new SpaceIndentation(3));
    }


    private static class State implements TranspilerState {
        private final Map<String, ClassBuilder> classes = new HashMap<>();
        private final Map<String, InterfaceBuilder> interfaces = new HashMap<>();
        private final Map<String, ParseTree> sources = new LinkedHashMap<>();
        private String mainClass;

        public String lookupMainClassId() {
            return mainClass;
        }
        public ClassBuilder lookupClass(String identifier) {
            return classes.get(identifier);
        }
        public InterfaceBuilder lookupInterface(String identifier) {
            return interfaces.get(identifier);
        }
        public ParseTree lookupSource(String fileName) {
            return sources.get(fileName);
        }

        public void setMainClassId(String id) {
            mainClass = id;
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
            sources.put(fileName, source);
        }

        public List<ClassBuilder> getClasses() {
            return new ArrayList<>(classes.values());
        }
        public List<InterfaceBuilder> getInterfaces() {
            return new ArrayList<>(interfaces.values());
        }
        public Map<String, ParseTree> getSources() {
            return new LinkedHashMap<>(sources);
        }

        TranspilerOutput createOutput(Indentation indentation) {
            final List<String> fileNames = new ArrayList<>();
            final Map<String, String> output = new HashMap<>();
            final String mainFile = mainClass == null ? null : mainClass + ".java";

            classes.forEach((id, c) -> {
                fileNames.add(id + ".java");
                output.put(id + ".java", c.toCode(indentation));
            });
            interfaces.forEach((id, c) -> {
                fileNames.add(id + ".java");
                output.put(id + ".java", c.toCode(indentation));
            });
            return new TranspilerOutput() {
                public List<String> allFileNames() {
                    return new ArrayList<>(fileNames);
                }
                public String getTranspiledCode(String fileName) {
                    return output.get(fileName);
                }
                public String lookupMainFileName() {
                    return mainFile;
                }
            };
        }

        private String validateId(Code id) {
            if (id == null) {
                throw new IllegalArgumentException("Cannot add class or interface: it doesn't have an identifier");
            }
            String identifier = id.toCode();
            if (doesJavaIdExist(identifier)) {
                throw new IllegalArgumentException("Cannot add class or interface: identifier '"
                                                   + identifier + "' already exist");
            }
            return identifier;
        }
    }
}
