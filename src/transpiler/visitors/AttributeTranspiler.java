package transpiler.visitors;

import grammar.gen.ConfluxParser;
import grammar.gen.ConfluxParserBaseVisitor;


public class AttributeTranspiler extends ConfluxParserBaseVisitor<String> {
    @Override
    public String visitAttributesBlock(ConfluxParser.AttributesBlockContext ctx) {
            StringBuilder result = new StringBuilder();

            // Visit each attribute in the attributes block
            for (ConfluxParser.DeclarationContext declaration : ctx.declaration()) {
                String attributeType = declaration.type().getText();
                String attributeAssignment = declaration.assignment().getText().replace("=", " = ");

                //Add private final to each attribute and build the string
                result.append("private final ").append(attributeType).append(" ").append(attributeAssignment).append(";\n");
            }

            return result.toString();
        }
}

