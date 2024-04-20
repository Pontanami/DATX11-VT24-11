package transpiler.visitors;

import grammar.gen.ConfluxParser;
import java_builder.CodeBuilder;

import java.util.List;

public class ComponentsTranspiler{

    // 'componentCode' is all the lines of components/aggregates together,
    // 'line' is each individual component/aggregate
    public static CodeBuilder visitComponentsBlock(ConfluxParser.ComponentsBlockContext ctx){
        CodeBuilder componentCode = new CodeBuilder();
        componentCode.beginPrefix("private final ");
        componentCode.beginSuffix(";");

        List<ConfluxParser.ComponentsDeclarationContext> components = ctx.componentsDeclaration();
        if(components != null)
        {
            for(ConfluxParser.ComponentsDeclarationContext componentContext : components)
            {

                if(componentContext.aggregateDeclaration() != null)
                {
                    String type = componentContext.aggregateDeclaration().declarationNoAssign().type().getText();
                    String identifier = componentContext.aggregateDeclaration().declarationNoAssign().Identifier(0).getText();
                    CodeBuilder line = new CodeBuilder();
                    line.append(type).append(" ").append(identifier);
                    componentCode.appendLine(0, line);

                } else if (componentContext.compositeDeclaration() != null)
                {
                    String type = componentContext.compositeDeclaration().declaration().type().getText();
                    String identifier = componentContext.compositeDeclaration().declaration().declarationPart(0).Identifier().getText();
                    String constructorSignature = componentContext.compositeDeclaration().declaration().declarationPart(0).expression().getText();
                    CodeBuilder line = new CodeBuilder();
                    line.append(type).append(" ").append(identifier).append(" = ").append(constructorSignature);
                    componentCode.appendLine(0, line);

                }
            }
        }
        return componentCode;
    }
}
