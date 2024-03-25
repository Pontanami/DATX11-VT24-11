# DATX11-VT24-11

 ## Test Suite

The test suite is divided into three subfolders. Any file contained in these folders that has a `.txt` extension is considered a test file.

* /good: Contains tests that should pass parsing and type checking and run successfully (if they contain a main procedure). If the test requires input, it should be located in a file with an identical name plus the extension `.input`. If the test is expected to produce some output, it should be located in a file with an identical name plus the extension `.output`.

* /bad: Contains tests that should fail either parsing or type checking. Running the transpiler on these files should cause it to exit with a non-zero exit code and std err or std out should include the text "PARSER ERROR" or "TRANSPILER ERROR".

* /bad-runtime: Contains tests that should pass parsing and type checking but fail during runtime. Running the transpiler on these files should produce output that includes the text "Exception" (on std err or std out).

 The test program `test-transpiler` takes the location of the transpiler source code, compiles the transpiler (using a makefile), and runs all the tests included in the test suite. To see the options, run the program without arguments. The options can be used, for instance, to only run a subset of the tests or print additional debugging information.

 ### Todo

 * Test that `.input` files work correctly
 * Test that bad-runtime tests work correctly
 * Change .txt extensions on test files to something that's specific for the project language
