package transpiler;

import java.util.List;

public interface TranspilerOutput {
    List<String> allFileNames();

    String getTranspiledCode(String fileName);

    String lookupMainFileName(); // null if no main file exist
}
