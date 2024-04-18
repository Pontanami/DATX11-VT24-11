package transpiler.visitors;

/*
* För att testa. Lägg till detta i main i slutet av try blocket:
            System.out.println("Testar METHOD-TRANSPILERN::::\n");

            MethodBuilder mb = new MethodBuilder();
            StatementTranspiler st = new StatementTranspiler(new ObserverTranspiler(new TaskQueue()));

            ParseTree prog = parser.methodDeclaration();
            MethodTranspiler methodTranspiler = new MethodTranspiler(mb, st);

            prog.accept(methodTranspiler);

            System.out.println("Method declaration:");
            System.out.println(methodTranspiler.methodDeclarationToString());

            System.out.println("\nMethod signature:");
            System.out.println(methodTranspiler.methodSignatureToString());


            System.out.println("\nMethod body:");
            System.out.println(methodTranspiler.methodBodyToString());
* */

import grammar.gen.ConfluxParser;
import grammar.gen.ConfluxParserBaseVisitor;
import java_builder.MethodBuilder;

import java.util.List;

public class MethodTranspiler extends ConfluxParserBaseVisitor<Void> {

    public MethodBuilder mb;
    private final StatementTranspiler st;

    public MethodTranspiler(MethodBuilder mb, StatementTranspiler st){
        this.mb = mb;
        this.st = st;
    }

    // Visitormetoder_____________________________
    @Override
    public Void visitMethodSignature(ConfluxParser.MethodSignatureContext ctx) {
        mb.setReturnType(ctx.methodType().getText());
        mb.setIdentifier(ctx.methodId().getText());
        addParameters(ctx.variableList(), ctx.variableList(), ctx.variableList(), ctx.variableList());
        return null;
    }

    @Override
    public Void visitMethodDeclaration(ConfluxParser.MethodDeclarationContext ctx) {
        mb.setReturnType(ctx.methodType().getText());
        mb.setIdentifier(ctx.methodId().getText());
        addParameters(ctx.variableList(), ctx.variableList(), ctx.variableList(), ctx.variableList());
        List<ConfluxParser.StatementContext> statements = ctx.methodBody().statement();
        for (ConfluxParser.StatementContext statement : statements) {
            mb.addStatement(st.visitStatement(statement));
        }
        return null;
    }

    // För testing____________________________
    public String methodSignatureToString() {
        String arguments = parametersAsString();
        return mb.getReturnType().toCode() + " " + mb.getIdentifier().toCode() + "(" + arguments +");";
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

    // Helpers
    private String parametersAsString(){
        StringBuilder parameters = new StringBuilder();
        for (int i = 0; i < mb.getParameters().size(); i++) {
            parameters.append(mb.getParameters().get(i).toCode());
            if(i != mb.getParameters().size()-1) parameters.append(", ");
        }
        return parameters.toString();
    }

    private void addParameters(ConfluxParser.VariableListContext ctx, ConfluxParser.VariableListContext ctx1,
                               ConfluxParser.VariableListContext ctx2, ConfluxParser.VariableListContext ctx3) {
        if (ctx != null) {
            int i = 0;
            while (ctx1.variable(i) != null) {
                String argType = ctx2.variable(i).type().getText();
                String argName = ctx3.variable(i).variableId().getText();
                mb.addParameter(argType, argName);
                i++;
            }
        }
    }
}
