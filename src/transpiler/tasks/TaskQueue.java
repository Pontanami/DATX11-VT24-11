package transpiler.tasks;

import transpiler.TranspilerState;
import transpiler.tasks.TranspilerTask;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

// While traversing the parse tree, it may become necessary to generate code at a later point in the process. This
// interface represents the queue for such tasks and contains the enum Priority that defines the order of execution for
// the different kinds of tasks.
public class TaskQueue {
    // The order (from top to bottom) in which to run the tasks
    public enum Priority {
        ADD_INTERFACE,
        ADD_CLASS,
        ADD_GETTER,
        ADD_MAIN_CLASS,
        CHECK_IMMUTABLE,
        MAKE_CONSTRUCTORS,
        MAKE_OBSERVERS,
        MAKE_DECORATORS,
        MAKE_DECORATOR_WRAPPER
    }

    private final TreeMap<Priority, List<TranspilerTask>> tasks = new TreeMap<>();

    public void addTask(Priority priority, TranspilerTask task) {
        List<TranspilerTask> taskList;
        if (tasks.containsKey(priority)) {
            taskList = tasks.get(priority);
        } else {
            taskList = new ArrayList<>();
            tasks.put(priority, taskList);
        }
        taskList.add(task);
    }

    public void runTasks(TranspilerState state) {
        tasks.values().forEach(taskList -> taskList.forEach(t -> t.run(state)));
        tasks.clear();
    }
}
