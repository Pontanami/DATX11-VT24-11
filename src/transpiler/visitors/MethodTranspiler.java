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
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.List;

public class MethodTranspiler extends ConfluxParserBaseVisitor<String> {

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

    @Override
    public String visitMethodCall(ConfluxParser.MethodCallContext ctx) {
        return super.visitMethodCall(ctx);
    }

    @Override
    public String visitMethodBlock(ConfluxParser.MethodBlockContext ctx) {
        StringBuilder result = new StringBuilder();

        List<ParseTree> allMethods = ctx.children;
        if(allMethods.get(2) != null){
            //index 2 är efter "{" och size-1 är innan "}"
            for (int i = 2; i < allMethods.size()-1; i++) {
                result.append(allMethods.get(i).getText()).append(" ");
            }
        }
        return result.toString();
    }

    @Override
    public String visitMethodBody(ConfluxParser.MethodBodyContext ctx) {
        StringBuilder result = new StringBuilder();
        result.append("{");
        if (ctx.statement() != null){
            for (int i = 0; i < ctx.statement().size(); i++) {
                // TODO: få ut rätt saker från varje statement.
                //result.append(ctx.statement().get(i).getText());
                result.append(ctx.statement(i).getText());
            }
        }
        result.append("}");
        return result.toString();
    }

}