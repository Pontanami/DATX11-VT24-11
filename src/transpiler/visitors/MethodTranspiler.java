package transpiler.visitors;

/*
* I main i slutet av try-blocket:
*             System.out.println("Testar METHOD-TRANSPILERN::::\n");

            ParseTree prog = parser.methodBlock(); // byt methodBlock() till den funktion som ska testas.
            MethodTranspiler methodTranspiler = new MethodTranspiler();

            System.out.println(prog.accept(methodTranspiler));
*
* Kör method-test.txt som main argument för att testa. method-different-tests.txt är bara en samling av alla olika tester.
* */

import grammar.gen.ConfluxParser;
import grammar.gen.ConfluxParserBaseVisitor;
import java_builder.Code;
import java_builder.CodeBuilder;
import java_builder.MethodBuilder;
import org.antlr.v4.runtime.tree.ParseTree;
import transpiler.tasks.TaskQueue;

import java.util.List;

public class MethodTranspiler extends ConfluxParserBaseVisitor<Void> {

    public MethodBuilder mb;
    private StatementTranspiler st;

    public MethodTranspiler(MethodBuilder mb, StatementTranspiler st){
        this.mb = mb;
        this.st = st;
    }

    @Override
    public Void visitMethodSignature(ConfluxParser.MethodSignatureContext ctx) {
        mb.setReturnType(ctx.methodType().getText());
        mb.setIdentifier(ctx.methodName().getText());
        if(ctx.variableList()!= null){
            int i = 0;
            while (ctx.variableList().variable(i) != null) {
                String argType = ctx.variableList().variable(i).type().getText();
                String argName = ctx.variableList().variable(i).variableId().getText();
                mb.addParameter(argType, argName);
                i++;
            }
        }
        return null;
    }

    // Skriver ut motsvarande javakod i konsolen.
    public String methodSignatureToString() {
        String arguments = parametersAsString();
        return mb.getReturnType().toCode() + " " + mb.getIdentifier().toCode() + "(" + arguments +");";
    }

    private String parametersAsString(){
        StringBuilder parameters = new StringBuilder();
        for (int i = 0; i < mb.getParameters().size(); i++) {
            parameters.append(mb.getParameters().get(i).toCode());
            if(i != mb.getParameters().size()-1) parameters.append(", ");
        }
        return parameters.toString();
    }

    @Override
    public Void visitMethodDeclaration(ConfluxParser.MethodDeclarationContext ctx) {
        mb.setReturnType(ctx.methodType().getText());
        mb.setIdentifier(ctx.methodName().getText());
        if(ctx.variableList()!= null){
            int i = 0;
            while (ctx.variableList().variable(i) != null) {
                String argType = ctx.variableList().variable(i).type().getText();
                String argName = ctx.variableList().variable(i).variableId().getText();
                mb.addParameter(argType, argName);
                i++;
            }
        }

        List<ConfluxParser.StatementContext> statements = ctx.methodBody().statement();

        for (ConfluxParser.StatementContext statement : statements) {
            mb.addStatement(st.visitStatement(statement));
        }

        return null;
    }

    public String methodDeclarationToString(){
        StringBuilder sb = new StringBuilder();
        sb.append(mb.getReturnType().toCode()).append(" ").append(mb.getIdentifier().toCode()).append("(").append(parametersAsString()).append(")").append("{\n");
        for (int i = 0; i < mb.getStatements().size(); i++) {
            sb.append(mb.getStatements().get(i).toCode());
            if (i != mb.getStatements().size()-1) sb.append("\n");
        }
        sb.append("\n}");
        return sb.toString();
    }

    public String methodBodyToString(){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mb.getStatements().size(); i++) {
            sb.append(mb.getStatements().get(i).toCode());
            if (i != mb.getStatements().size()-1) sb.append("\n");
        }
        return sb.toString();
    }
}
