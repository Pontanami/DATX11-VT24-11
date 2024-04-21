package transpiler.visitors;

import grammar.gen.ConfluxParser;


public class AttributeTranspiler extends DefaultTranspiler {


    @Override
    public String visitAttributeDeclaration(ConfluxParser.AttributeDeclarationContext ctx) {

        StringBuilder result = new StringBuilder();

        result.append("private ");

        if (ctx.declaration().VAR() == null) {
            result.append("final ");
        }

        result.append(visit(ctx.declaration().type()));

        if (!ctx.declaration().declarationPart().isEmpty()) {
            for (ConfluxParser.DeclarationPartContext part : ctx.declaration().declarationPart()) {
                result.append(" ").append(visitDeclarationPart(part));
            }
        }


        return result.toString();    }


    public String visitDeclarationPart(ConfluxParser.DeclarationPartContext ctx) {
        StringBuilder result = new StringBuilder();
        result.append(ctx.Identifier().getText());

        if (ctx.ASSIGN() != null) {
            result.append(" = ").append(visit(ctx.expression()));
        }
        result.append(";");

        return result.toString();
    }


}

