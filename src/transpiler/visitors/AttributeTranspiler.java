package transpiler.visitors;

import grammar.gen.ConfluxParser;
import grammar.gen.ConfluxParserBaseVisitor;


public class AttributeTranspiler extends DefaultTranspiler {
    @Override
    public String visitAttributesBlock(ConfluxParser.AttributesBlockContext ctx) {
            StringBuilder result = new StringBuilder();

            // Visit each attribute in the attributes block
            for (ConfluxParser.DeclarationContext declaration : ctx.declaration()) {
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
                    result.append(visit(declaration.type()));
                    result.append(" ");

                    result.append(visit(declaration.assignment()));
                }
                result.append(";");
                result.append("\n");
            }
            return result.toString();
        }
}

