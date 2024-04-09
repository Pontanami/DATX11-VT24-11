# DATX11-VT24-11

 ## Test Suite

The test suite is divided into three subfolders. Any file contained in these folders that has a `.flux` extension is considered a test file.
Any directory contained in these folders starting with `test_` is considered a test module, ie. a set of files that together make up one test.
All `.flux` files in a test module are transpiled together, and if one
of them contain a main block it will be run (there should only be one main file per test module).
The three subfolders are:

* /good: Contains tests that should pass parsing and type checking and run successfully (if they contain a main procedure). If the test is a single file and is expected to produce some output, it should be located in a file with an identical name plus the extension `.output`. For test modules, the expected output can have any name but must have the `.output` extension.

* /bad: Contains tests that should fail either parsing or type checking. Running the transpiler on these files should cause it to exit with a non-zero exit code and std err or std out should include the text "PARSER ERROR" or "TRANSPILER ERROR".

* /bad-runtime: Contains tests that should pass parsing and type checking but fail during runtime. Executing the transpiled files should produce output that includes the text "Exception" (on std err or std out).

 The test program `test-transpiler` compiles the transpiler (using a makefile), and runs all the tests included in the test suite. To see the options, run the program with the help flag -h. The options can be used, for instance, to only run a subset of the tests or print additional debugging information.
