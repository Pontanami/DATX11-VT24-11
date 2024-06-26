package transpiler.visitors;

import grammar.gen.ConfluxParser;
import java_builder.MethodBuilder;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;
import java.util.Objects;


public class AttributeTranspiler extends DefaultTranspiler {
    private boolean convertFloat = false;

    @Override
    public String visitAttributeDeclaration(ConfluxParser.AttributeDeclarationContext ctx) {

        return generateAttribute(ctx);
    }

    private String generateAttribute(ConfluxParser.AttributeDeclarationContext ctx) {
        StringBuilder result = new StringBuilder();

        constructAttribute(ctx, 0, result);
        List<TerminalNode> iterations = ctx.declaration().COMMA();
        for(int i = 1;i<=iterations.size(); i++){
            constructAttribute(ctx, i, result);
        }
        return result.toString();
    }

    private void constructAttribute(ConfluxParser.AttributeDeclarationContext ctx, int i, StringBuilder result) {
        result.append("private ");

        if (ctx.declaration().VAR() == null) {
            result.append("final ");
        }
        result.append(visit(ctx.declaration().type()));

        convertFloat = Objects.equals(ctx.declaration().type().getText(), "float");
        result.append(" ").append(visitDeclarationPart(ctx.declaration().declarationPart(i))).append(";");
    }

    private String generateGetter(ConfluxParser.AttributeDeclarationContext ctx) {

        String returnType = visit(ctx.declaration().type());
        String attributeName = ctx.declaration().declarationPart(0).Identifier().getText();
        String methodName = ctx.Identifier().getText();


        MethodBuilder methodBuilder = new MethodBuilder();
        methodBuilder.addModifier("private");
        if (ctx.declaration().VAR() == null) {
            methodBuilder.addModifier("final");
        }
        methodBuilder.setReturnType(returnType);
        methodBuilder.setIdentifier(methodName);
        methodBuilder.addStatement("return " + attributeName + ";");

        return methodBuilder.toCode();

    }

    public String visitDeclarationPart(ConfluxParser.DeclarationPartContext ctx) {
        StringBuilder result = new StringBuilder();
        result.append(ctx.Identifier().getText());

        if (ctx.ASSIGN() != null) {
            result.append(" = ").append(visitExpression(ctx.expression()));
        }

        return result.toString();
    }

    @Override
    public String visitLiterals(ConfluxParser.LiteralsContext ctx) {
        String text = super.visitLiterals(ctx);
        if (convertFloat && (ctx.DECIMALNUMBER() != null || ctx.SIGNED_DECIMALNUMBER() != null)) {
            text += "F";
        }
        return text;
    }
}

