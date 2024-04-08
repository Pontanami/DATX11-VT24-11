package transpiler.tasks;

import transpiler.TranspilerState;

// While traversing the parse tree, it may be necessary to generate code at a later point in the process. This interface
// represents such tasks. The single method run() receives the current output of the transpiler which can be modified
// by adding code to existing classes or interfaces as well as adding new ones.
public interface TranspilerTask {
    void run(TranspilerState state);
}
