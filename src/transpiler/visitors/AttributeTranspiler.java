package transpiler.visitors;

import grammar.gen.ConfluxParser;
import grammar.gen.ConfluxParserBaseVisitor;


public class AttributeTranspiler extends DefaultTranspiler {
    @Override
    public String visitAttributesBlock(ConfluxParser.AttributesBlockContext ctx) {
            StringBuilder result = new StringBuilder();

            // Visit each attribute in the attributes block
            for (ConfluxParser.DeclarationContext declaration : ctx.declaration()) {
                //if the var keyword is not present, add private final to the attribute
                if (declaration.VAR() == null) {
                    result.append("private final ");
                }

                //Attributes without assignments
                if (declaration.variableDeclaration() != null) {
                    String variableDeclaration = declaration.variableDeclaration().getText();
                    result.append(variableDeclaration).append(";\n");
                }
                //Attributes with assignments
                else {
                    String attributeType = declaration.type().getText();

                    String attributeAssignment = declaration.assignment().getText().replace("=", " = ");

                    //Build the rest of the string
                    result.append(attributeType).append(" ").append(attributeAssignment).append(";\n");
                }
            }
            return result.toString();
        }
}

