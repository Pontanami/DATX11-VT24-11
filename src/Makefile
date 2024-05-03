
# Variables
###########################################################

# The names of the lexer and parser
parser := ConfluxParser
lexer := ConfluxLexer

# Antlr options
antlr_out := ../src/grammar/gen
antlr_pkg := grammar.gen
antlr_options := -visitor -o "${antlr_out}" -package ${antlr_pkg}

# Antlr generated files
antlr_gen := \
	${antlr_out}/${parser}.java \
	${antlr_out}/${lexer}.java \
	${antlr_out}/${parser}BaseListener.java \
	${antlr_out}/${parser}BaseVisitor.java \
	${antlr_out}/${parser}Listener.java \
	${antlr_out}/${parser}Visitor.java

# location of the antlr jar file
antlr_jar := ../antlr-4.13.1-complete.jar

ifeq ($(OS),Windows_NT)
    CLASSPATH_SEPARATOR := ;
else
    CLASSPATH_SEPARATOR := :
endif

# java source files that make up the transpiler
java_src := $(filter-out %package-info.java, $(wildcard ../src/*.java ../src/java_builder/*.java ../src/runtime/observers/*.java \
	../src/transpiler/*.java ../src/transpiler/tasks/*.java ../src/transpiler/visitors/*.java ../src/runtime/decorators/*.java))

# output location for java compilation (same as IntelliJ uses)
java_out_dir := ../out/production/DATX11-VT24-11

java_out := $(java_src:../src/%.java=$(java_out_dir)/%.class) $(antlr_gen:../src/%.java=$(java_out_dir)/%.class)

# The name of the jar file for the transpiler
transpiler_jar := ../conflux.jar

# classes to include in the jar
transpiler_jar_contents := -C $(java_out_dir) Main.class -C $(java_out_dir) Options.class -C $(java_out_dir) grammar/gen -C $(java_out_dir) java_builder \
	-C $(java_out_dir) runtime -C $(java_out_dir) transpiler

# the manifest file for creating the jar
transpiler_jar_manifest := conflux-manifest.txt

# Targets
###########################################################

.PHONY : default

# default target (has to be defined before the other ones)
default : ${transpiler_jar}

# Create the jar file
${transpiler_jar} : .makefile-java-sentinel
	@echo creating ${transpiler_jar}...
	@jar cfm ${transpiler_jar} ${transpiler_jar_manifest} ${transpiler_jar_contents}

# Compile all java source files
.makefile-java-sentinel : ${antlr_gen} ${java_src}
	@echo compiling java...
	@javac -cp "${antlr_jar}${CLASSPATH_SEPARATOR}../src" -d "${java_out_dir}" $?
	@echo dummy-value > .makefile-java-sentinel

# Generate the lexer and parser with antlr
${antlr_gen} : grammar/${parser}.g4 grammar/${lexer}.g4
	@echo running antlr...
	@java -jar ${antlr_jar} ${antlr_options} grammar/${lexer}.g4
	@java -jar ${antlr_jar} ${antlr_options} grammar/${parser}.g4
