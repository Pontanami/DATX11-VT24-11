package transpiler.visitors;

import grammar.gen.ConfluxParser;

public class AttributeTranspiler extends DefaultTranspiler {

    @Override
    public String visitAttributesBlock(ConfluxParser.AttributesBlockContext ctx) {
        StringBuilder result = new StringBuilder();

        // Visit each attribute in the attributes block and transpile
        for (ConfluxParser.DeclarationContext declaration : ctx.declaration()) {
            result.append(transpileAttribute(declaration)).append(";\n");
        }
        return result.toString();
    }

    public String transpileAttribute(ConfluxParser.DeclarationContext declaration) {
        StringBuilder result = new StringBuilder();

        //if the 'var' keyword is not present, add private final to the attribute
        if (declaration.VAR() == null) {
            result.append("private final ");
        }

        //Attributes without assignments
        if (declaration.variableDeclaration() != null) {
            result.append(visit(declaration.variableDeclaration()));
        }

        //Attributes with assignments
        else {
            result.append(visit(declaration.type())).append(" ")
                    .append(visit(declaration.assignment()));
        }
        return result.toString();
    }
}

