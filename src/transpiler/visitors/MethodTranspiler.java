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
        StringBuilder arguments = new StringBuilder();
        for (int i = 0; i < mb.getParameters().size(); i++) {
            arguments.append(mb.getParameters().get(i).toCode());
            if(i != mb.getParameters().size()-1) arguments.append(", ");
        }
        return mb.getReturnType().toCode() + " " + mb.getIdentifier().toCode() + "(" + arguments +");";
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
        System.out.println("VisitMethodDeclaration: " + mb.getReturnType().toCode() + " " + mb.getIdentifier().toCode() + " " +
                mb.getParameters().toString() + " " + mb.getStatements().toString());

     //   statements hämtas in
              //  för varje statement -> accepta till node?

        List<ConfluxParser.StatementContext> statements = ctx.methodBody().statement();


        for (int i = 0; i < statements.size(); i++) {
            mb.addStatement(st.visitStatement(statements.get(i))) ;
        }

        mb.getStatements();


        return null;    }
}

/* GAMMALT
*
    @Override
    public String visitMethodType(ConfluxParser.MethodTypeContext ctx) {
        // Hämta första (enda) barnet/lövet. getText() för värdet som sträng (i detta fall datatypen)
        return ctx.getChild(0).getText();
    }

    @Override
    public String visitMethodSignature(ConfluxParser.MethodSignatureContext ctx) {
        System.out.println("i visitMethodSignature");
        //Hämta typ och namn
        String type = ctx.methodType().getText();
        String name = ctx.methodName().getText();
        //Om variableList finns så blir dessa argument, annars är argument en tom sträng
        // TODO: refaktorisera...
        StringBuilder sb = new StringBuilder();
        if(ctx.variableList()!= null){
            int i = 0;
            while (ctx.variableList().variable(i) != null) {
                if(i!=0) sb.append(" ");
                String argType = ctx.variableList().variable(i).type().getText();
                String argName = ctx.variableList().variable(i).variableId().getText();
                sb.append(argType).append(" ").append(argName).append(",");
                i++;
            }
            //Ta bort sista kommatecknet
            sb.deleteCharAt(sb.length()-1);
        }
        // Assigna arguments som variabellistan eller tom sträng beroende på situation
        String arguments = ctx.variableList() != null? sb.toString(): "";
        return type + " " + name + "(" + arguments + ");";
    }

    @Override
    public String visitMethodDeclaration(ConfluxParser.MethodDeclarationContext ctx) {
        System.out.println("Testar visitMethodDeclaration");
        String type = ctx.methodType().getText();
        String name = ctx.methodName().getText(); // Ändrade parser-regeln för MethodDeclaration så den tar methodName istället flr Identifier.
        // TODO: refaktorisera...
        StringBuilder sb = new StringBuilder();
        if(ctx.variableList()!= null){
            int i = 0;
            while (ctx.variableList().variable(i) != null) {
                if(i!=0) sb.append(" ");
                String argType = ctx.variableList().variable(i).type().getText();
                String argName = ctx.variableList().variable(i).variableId().getText();
                sb.append(argType).append(" ").append(argName).append(",");
                i++;
            }
            //Ta bort sista kommatecknet
            sb.deleteCharAt(sb.length()-1);
        }
        // Assigna arguments som variabellistan eller tom sträng beroende på situation
        String arguments = ctx.variableList() != null? sb.toString(): "";
        String signature = type + " " + name + "(" + arguments + ")";
        String body = "Body TODO...";
        return signature + body;
    }
*/