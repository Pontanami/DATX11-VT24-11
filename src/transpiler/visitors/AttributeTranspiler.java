package transpiler.visitors;

import grammar.gen.ConfluxParser;
import java_builder.MethodBuilder;


public class AttributeTranspiler extends DefaultTranspiler {


    @Override
    public String visitAttributeDeclaration(ConfluxParser.AttributeDeclarationContext ctx) {

        if (ctx.AS() != null) {
            return generateGetter(ctx);
        } else {
            return generateAttribute(ctx);
        }
    }

    private String generateAttribute(ConfluxParser.AttributeDeclarationContext ctx) {
        StringBuilder result = new StringBuilder();

        result.append("private ");

        if (ctx.declaration().VAR() == null) {
            result.append("final ");
        }
        result.append(visit(ctx.declaration().type()));
        result.append(" ").append(visitDeclarationPart(ctx.declaration().declarationPart(0)));
        result.append(";");
        return result.toString();
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
            result.append(" = ").append(visit(ctx.expression()));
        }

        return result.toString();
    }


}

