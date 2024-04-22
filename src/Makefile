
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

# java source files that make up the transpiler 	TODO: add ../src/transpiler/runtime/decorators/*.java
java_src := $(filter-out %package-info.java, $(wildcard ../src/*.java ../src/java_builder/*.java ../src/runtime/observers/*.java \
	../src/transpiler/*.java ../src/transpiler/tasks/*.java ../src/transpiler/visitors/*.java))

# output location for java compilation (same as IntelliJ uses)
java_out_dir := ../out/production/DATX11-VT24-11

java_out := $(java_src:../src/%.java=$(java_out_dir)/%.class) $(antlr_gen:../src/%.java=$(java_out_dir)/%.class)

# The name of the jar file for the transpiler
transpiler_jar := ../conflux.jar

# classes to include in the jar
# transpiler_jar_contents := $(java_src:../src/%.java=-C $(java_out_dir) %.class) $(antlr_gen:../src/%.java=-C $(java_out_dir) %.class)
transpiler_jar_contents := -C $(java_out_dir) Main.class -C $(java_out_dir) Options.class -C $(java_out_dir) grammar/gen -C $(java_out_dir) java_builder \
	-C $(java_out_dir) runtime -C $(java_out_dir) transpiler

# the manifest file for creating the jar
transpiler_jar_manifest := conflux-manifest.txt

# Targets
###########################################################

.PHONY : default

# default target (has to be defined before the other ones)
default :
	:

# Create the jar file
${transpiler_jar} : ${java_out}/Main.class
	@jar cfm ${transpiler_jar} ${transpiler_jar_manifest} ${transpiler_jar_contents}
	@echo created ${transpiler_jar}

# Compile the main class    ${java_src}
${java_out}/Main.class : ${antlr_out}/${parser}.java
	javac -cp "${antlr_jar};../src" -d "${java_out_dir}" Main.java

# Generate the lexer and parser with antlr
${antlr_out}/${parser}.java : grammar/${parser}.g4 grammar/${lexer}.g4
	java -jar ${antlr_jar} ${antlr_options} "grammar/${lexer}.g4"
	java -jar ${antlr_jar} ${antlr_options} "grammar/${parser}.g4"
