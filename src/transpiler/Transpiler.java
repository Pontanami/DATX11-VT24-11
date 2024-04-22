package transpiler;

import java.util.*;

import grammar.gen.ConfluxParser.ProgramContext;
import java_builder.*;
import transpiler.tasks.TaskQueue;
import transpiler.visitors.StartVisitor;

public class Transpiler {
    private static final String DEFAULT_PACKAGE = "default_package";

    private final State state;

    public Transpiler() {
        state = new State();
    }

    public void addSource(String fileName, ProgramContext source) {
        state.addSource(fileName, source);
    }

    // Transpile all the sources
    public TranspilerOutput transpile() {
        TaskQueue taskQueue = new TaskQueue();
        StartVisitor startVisitor = new StartVisitor(taskQueue);

        state.getSources().forEach((name, tree) -> {
            startVisitor.setTypeFileName(name);
            tree.accept(startVisitor);
        });
        taskQueue.runTasks(state);
        return state.createOutput(new SpaceIndentation(3));
    }


    private static class State implements TranspilerState {
        private final Map<String, ClassBuilder> classes = new HashMap<>();
        private final Map<String, InterfaceBuilder> interfaces = new HashMap<>();
        private final Map<String, ProgramContext> sources = new LinkedHashMap<>();
        private String mainClass;
        private String packageId;

        public String lookupMainClassId() {
            return mainClass;
        }
        public String lookupPackageId() {
            return packageId;
        }
        public ClassBuilder lookupClass(String identifier) {
            return classes.get(identifier);
        }
        public InterfaceBuilder lookupInterface(String identifier) {
            return interfaces.get(identifier);
        }
        public ProgramContext lookupSource(String fileName) {
            return sources.get(fileName);
        }

        public void setMainClassId(String id) {
            mainClass = mainClass == null ? id : mainClass; // use the first added main class
        }
        public void setPackageId(String packageId) {
            this.packageId = packageId;
        }
        public void addClass(ClassBuilder builder) {
            String identifier = validateId(builder.getIdentifier());
            classes.put(identifier, builder);
        }
        public void addInterface(InterfaceBuilder builder) {
            String identifier = validateId(builder.getIdentifier());
            interfaces.put(identifier, builder);
        }
        public void addSource(String fileName, ProgramContext source) {
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
        public Map<String, ProgramContext> getSources() {
            return new LinkedHashMap<>(sources);
        }

        TranspilerOutput createOutput(Indentation indentation) {
            final List<String> fileNames = new ArrayList<>();
            final Map<String, String> output = new HashMap<>();
            final String mainFile = mainClass == null ? null : mainClass + ".java";
            final String packageName = packageId == null ? DEFAULT_PACKAGE : packageId;

            classes.forEach((id, c) -> {
                fileNames.add(id + ".java");
                output.put(id + ".java", c.setPackage(packageName).toCode(indentation));
            });
            interfaces.forEach((id, c) -> {
                fileNames.add(id + ".java");
                output.put(id + ".java", c.setPackage(packageName).toCode(indentation));
            });
            return new TranspilerOutput() {
                public List<String> allFileNames() { return new ArrayList<>(fileNames); }
                public String getTranspiledCode(String fileName) { return output.get(fileName); }
                public String lookupMainFileName() { return mainFile; }
                public String getPackageName() { return packageName; }
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
