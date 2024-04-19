package transpiler.visitors;

import grammar.gen.ConfluxParser;

public class AttributeTranspiler extends DefaultTranspiler {

    @Override
    public String visitAttributesBlock(ConfluxParser.AttributesBlockContext ctx) {
        StringBuilder result = new StringBuilder();

        // Visit each attribute in the attributes block and transpile
        for (ConfluxParser.AttributeDeclarationContext declaration : ctx.attributeDeclaration()) {
            result.append(transpileAttribute(declaration)).append(";\n");
        }
        return result.toString();
    }

    public String transpileAttribute(ConfluxParser.AttributeDeclarationContext declaration) {
        StringBuilder result = new StringBuilder();

        //if the 'var' keyword is not present, add private final to the attribute
        if (declaration.declaration().VAR() == null) {
            result.append("private final ");
        }

        //Attributes without assignments
        if (declaration.declaration() != null) {
            result.append(visit(declaration.declaration()));
        }

        //Attributes with assignments
        else {
            result.append(visit(declaration.declaration().type())).append(" ")
                    .append(visit(declaration.declaration().declarationPart(0)));
        }
        return result.toString();
    }
}

