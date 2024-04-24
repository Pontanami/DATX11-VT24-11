package transpiler.visitors;

import grammar.gen.ConfluxParser;
import java_builder.Code;
import org.antlr.v4.runtime.misc.Pair;

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

                    line.addComponent(Code.fromString(type + " " + identifier));

                    // See if there is any "handles ... as ..." methods
                    if(componentContext.aggregateDeclaration().handlesClause() != null){
                        for (ConfluxParser.DelegateMethodContext delegateCtx : componentContext.aggregateDeclaration().handlesClause().delegateMethod()) {
                            Code delegateMethod = Code.fromString(delegateCtx.methodId().getText());
                            Code asMethod = null;
                            if (delegateCtx.renameMethod() != null)
                            {
                                asMethod = Code.fromString(delegateCtx.renameMethod().getText());
                            }
                            line.addHandles(delegateMethod, asMethod);
                        }

/*
                        // See if there is an "as ..." method
                        if(componentContext.aggregateDeclaration().handlesClause().Identifier() != null) {
                            line.addNewMethodName(Code.fromString(componentContext.compositeDeclaration().handlesClause().Identifier().getText()));
                        }*/
                    }

                } else if (componentContext.compositeDeclaration() != null) {
                    String type = componentContext.compositeDeclaration().declaration().type().getText();
                    String identifier = componentContext.compositeDeclaration().declaration().declarationPart(0).Identifier().getText();
                    String constructorSignature = componentContext.compositeDeclaration().declaration().declarationPart(0).expression().accept(new ExpressionTranspiler());

                    line.addComponent(Code.fromString(type + " " + identifier + " = " + constructorSignature.replaceAll(" ", ""))); //TODO: expression.accept ger String med mellanrum mellan tecknen


                    // See if there is any "handles ... as ..." methods
                    if(componentContext.compositeDeclaration().handlesClause() != null){
                        for (ConfluxParser.DelegateMethodContext delegateCtx : componentContext.compositeDeclaration().handlesClause().delegateMethod()) {
                            Code delegateMethod = Code.fromString(delegateCtx.methodId().getText());
                            Code asMethod = null;
                            if (delegateCtx.renameMethod() != null)
                            {
                                asMethod = Code.fromString(delegateCtx.renameMethod().getText());
                            }
                            line.addHandles(delegateMethod, asMethod);
                        }
                        /*
                        if(componentContext.compositeDeclaration().handlesClause().Identifier() != null) {
                            line.addNewMethodName(Code.fromString(componentContext.compositeDeclaration().handlesClause().Identifier().getText()));
                        }*/
                    }
                }
                componentList.add(line);
            }
        }
        return componentList;
    }
}


class ComponentLine {
    private Code component = null;
    private List<Pair<Code, Code>> handlesPairs = null;

    protected void addHandles(Code delegateMethod, Code asMethod) {
        if(handlesPairs == null)
            handlesPairs = new ArrayList<>();
        handlesPairs.add(new Pair<>(delegateMethod,asMethod));
    }

    protected void addComponent(Code c) {
        component = c;
    }

    /**
     * @return a list of Code-pairs (a, b) where 'a' is the signature of a delegated method and 'b' is the identifier of
     * the new method name. List can be null if empty. 'b' can be null if there is no renaming for the 'a' method.
     */
    public List<Pair<Code, Code>> getHandlesPairs() {
        return handlesPairs;
    }

    /**
     * @return the component identifier as Code.
     */
    public Code getComponent()
    {
        return component;
    }
}
