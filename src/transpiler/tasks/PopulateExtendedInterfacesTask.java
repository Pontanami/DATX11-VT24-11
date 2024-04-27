package transpiler.tasks;

import java_builder.Code;
import java_builder.InterfaceBuilder;
import java_builder.MethodBuilder;
import java_builder.MethodBuilder.MethodSignature;
import transpiler.TranspilerState;

import java.util.*;
import java.util.stream.Collectors;

// Add all interface methods from all super interfaces to all sub interfaces
public class PopulateExtendedInterfacesTask implements TranspilerTask {
    // Map interface ids to their depth in the inheritance hierarchy, represented as thunk (to be computed later)
    private final Map<String, LevelThunk> levels = new HashMap<>();

    public void addInterface(InterfaceBuilder toAdd) {
        String id = toAdd.getIdentifier().toCode();
        List<String> superInterfaces = toAdd.getExtendedInterfaces().stream().map(Code::toCode).toList();
        levels.put(id, new LevelThunk(superInterfaces));
    }

    @Override
    public void run(TranspilerState state) {
        List<InterfaceWithLevel> interfaces = new ArrayList<>();
        levels.forEach((id, thunk) -> {
            int level = thunk.computeLevel();
            if (level > 0) { // omit interfaces that doesn't extend anything
                interfaces.add(new InterfaceWithLevel(level, state.lookupInterface(id)));
            }
        });

        Comparator<InterfaceWithLevel> comparator = Comparator.comparingInt(InterfaceWithLevel::level);
        interfaces.sort(comparator);// Sort by level (maximum depth in the inheritance hierarchy), smallest first
        interfaces.forEach(i -> copyMethods(state, i.builder));
    }

    // Copy all methods from the immediate super interfaces into the given interface
    private void copyMethods(TranspilerState state, InterfaceBuilder subInterface) {
        Set<MethodSignature> presentMethods = methodSet(subInterface);
        for (Code superInterfaceId : subInterface.getExtendedInterfaces()) {
            InterfaceBuilder superInterface = state.lookupInterface(superInterfaceId.toCode());
            for (MethodBuilder method : superInterface.getMethods()) {
                MethodSignature signature = method.getSignature();
                if (!presentMethods.contains(signature)) {
                    subInterface.addMethod(method);
                    presentMethods.add(signature);
                }
            }
        }
    }

    private HashSet<MethodSignature> methodSet(InterfaceBuilder builder) {
        return builder.getMethods()
                      .stream()
                      .map(MethodBuilder::getSignature)
                      .collect(Collectors.toCollection(HashSet::new));
    }

    // Represent a level that may or may not be computed yet, a call to computeLevel will force the computation of this
    // level and all its dependencies (levels of all super interfaces)
    private class LevelThunk {
        private final Collection<String> dependencies;
        private Integer level;

        LevelThunk(Collection<String> dependencies) {
            this.dependencies = dependencies;
        }

        int computeLevel() {
            if (level == null) {
                level = 0;
                for (String dep : dependencies) {
                    level = Math.max(level, 1 + levels.get(dep).computeLevel());
                }
            }
            return level;
        }
    }

    private record InterfaceWithLevel(int level, InterfaceBuilder builder) {}
}
