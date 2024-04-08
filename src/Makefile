
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

# output location for java compilation (same as IntelliJ uses)
java_out := ../out/production/DATX11-VT24-11

# the java command for running the transpiler
run_java := java -cp "${antlr_jar};${java_out}" Main

# Targets
###########################################################

.PHONY : default transpiler-batch

# default target (has to be defined before the other ones)
default : ${java_out}/Main.class transpiler-batch

# Generate the batch file that runs the transpiler
transpiler-batch:
	echo @${run_java} %%^*> Transpiler.bat

# Compile the main class
${java_out}/Main.class : Main.java ${antlr_gen}
	javac -d "${java_out}" Main.java

# Generate the lexer and parser with antlr
${antlr_gen} : grammar/${parser}.g4 grammar/${lexer}.g4
	java -jar ${antlr_jar} ${antlr_options} "grammar/${lexer}.g4"
	java -jar ${antlr_jar} ${antlr_options} "grammar/${parser}.g4"
