package transpiler.visitors;

import grammar.gen.ConfluxParser;
import java_builder.Code;

import java.util.ArrayList;
import java.util.List;

public class ComponentsTranspiler{

    public static List<ComponentLine> visitComponentsBlock(ConfluxParser.ComponentsBlockContext ctx){
        List<ComponentLine> componentList = new ArrayList<>();

        List<ConfluxParser.ComponentsDeclarationContext> components = ctx.componentsDeclaration();
        if(components != null) {
            for(ConfluxParser.ComponentsDeclarationContext componentContext : components) {
                ComponentLine line = new ComponentLine();
                if(componentContext.aggregateDeclaration() != null) {
                    String type = componentContext.aggregateDeclaration().declarationNoAssign().type().getText();
                    String identifier = componentContext.aggregateDeclaration().declarationNoAssign().Identifier(0).getText();

                    line.add(Code.fromString(type + " " + identifier));

                    // See if there is any "handles ..." methods
                    if(componentContext.aggregateDeclaration().handlesClause() != null){
                        for (ConfluxParser.DelegateMethodContext delegateCtx : componentContext.aggregateDeclaration().handlesClause().delegateMethod())
                            line.addHandlesSignature(Code.fromString(delegateCtx.methodId().getText()));

                        // See if there is an "as ..." method
                        if(componentContext.aggregateDeclaration().handlesClause().Identifier() != null) {
                            line.addNewMethodName(Code.fromString(componentContext.compositeDeclaration().handlesClause().Identifier().getText()));
                        }
                    }

                } else if (componentContext.compositeDeclaration() != null) {
                    String type = componentContext.compositeDeclaration().declaration().type().getText();
                    String identifier = componentContext.compositeDeclaration().declaration().declarationPart(0).Identifier().getText();
                    String constructorSignature = componentContext.compositeDeclaration().declaration().declarationPart(0).expression().getText();

                    line.add(Code.fromString(type + " " + identifier + " = " + constructorSignature));


                    // See if there is any "handles ..." methods
                    if(componentContext.compositeDeclaration().handlesClause() != null){
                        for (ConfluxParser.DelegateMethodContext delegateCtx : componentContext.compositeDeclaration().handlesClause().delegateMethod())
                            line.addHandlesSignature(Code.fromString(delegateCtx.methodId().getText()));

                        // See if there is an "as ..." method
                        if(componentContext.compositeDeclaration().handlesClause().Identifier() != null) {
                            line.addNewMethodName(Code.fromString(componentContext.compositeDeclaration().handlesClause().Identifier().getText()));
                        }
                    }

                    componentList.add(line);

                }
            }
        }
        return componentList;
    }
}

class ComponentLine {
    public Code component = null;
    public List<Code> delegateSignatures = null;
    public Code newMethodName = null;

    void addHandlesSignature(Code s){
        if(delegateSignatures == null)
            delegateSignatures = new ArrayList<>();
        delegateSignatures.add(s);
    }

    void add(Code c){
        component = c;
    }

    void addNewMethodName(Code n){
        newMethodName = n;
    }
}
