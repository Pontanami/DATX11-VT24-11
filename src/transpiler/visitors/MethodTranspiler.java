package transpiler.visitors;

import grammar.gen.ConfluxParser;
import grammar.gen.ConfluxParserBaseVisitor;

public class MethodTranspiler extends ConfluxParserBaseVisitor<String> {

    @Override
    public String visitMethodType(ConfluxParser.MethodTypeContext ctx) {
        // Hämta första (enda) barnet/lövet. getText() värdet som sträng (i detta fall datatypen)
        return ctx.getChild(0).getText();
    }

    @Override
    public String visitMethodName(ConfluxParser.MethodNameContext ctx) {
        return super.visitMethodName(ctx);
    }

    @Override
    public String visitMethodSignature(ConfluxParser.MethodSignatureContext ctx) {
        System.out.println("i visitMethodSignature");
        //Hämta typ och namn
        String type = ctx.methodType().getText();
        String name = ctx.methodName().getText();
        //Om variableList finns så blir dessa argument, annars är argument en tom sträng
        String arguments = ctx.variableList() != null? ctx.variableList().getText(): "";

        System.out.println("Variables:" + arguments);

        String result = type + " " + name + "(" + arguments + ");";

        return result;
    }

    @Override
    public String visitMethodDeclaration(ConfluxParser.MethodDeclarationContext ctx) {
        return super.visitMethodDeclaration(ctx);
    }

    @Override
    public String visitMethodCall(ConfluxParser.MethodCallContext ctx) {
        return super.visitMethodCall(ctx);
    }

    @Override
    public String visitMethodBlock(ConfluxParser.MethodBlockContext ctx) {
        return super.visitMethodBlock(ctx);
    }

    @Override
    public String visitMethodBody(ConfluxParser.MethodBodyContext ctx) {
        return super.visitMethodBody(ctx);
    }

}
