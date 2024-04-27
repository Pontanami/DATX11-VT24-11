package transpiler.visitors;

import grammar.gen.ConfluxParser;
import java_builder.CodeBuilder;
import java_builder.MethodBuilder;
import org.antlr.v4.runtime.misc.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class ComponentsTranspiler{

    public static List<Pair<String, List<MethodBuilder>>> visitComponentsBlock(ConfluxParser.ComponentsBlockContext ctx){
        List<Pair<String, List<MethodBuilder>>> components = new ArrayList<>();
        List<ConfluxParser.ComponentsDeclarationContext> componentContexts = ctx.componentsDeclaration();

        if(componentContexts != null) {
            for(ConfluxParser.ComponentsDeclarationContext componentContext : componentContexts) {
                List<MethodBuilder> mbList = new ArrayList<>();
                CodeBuilder component = new CodeBuilder();

                if(componentContext.aggregateDeclaration() != null) {
                    String type = componentContext.aggregateDeclaration().declarationNoAssign().type().getText();
                    String identifier = componentContext.aggregateDeclaration().declarationNoAssign().Identifier(0).getText();
                    component.append(type);
                    component.append(" ");
                    component.append(identifier);

                    // See if there is any "handles ... as ..." methods
                    if(componentContext.aggregateDeclaration().handlesClause().HANDLES() != null){
                        for (ConfluxParser.DelegateMethodContext delegateCtx : componentContext.aggregateDeclaration().handlesClause().delegateMethod()) {
                            handlesToMethods(mbList, identifier, delegateCtx);
                        }
                    }

                } else if (componentContext.compositeDeclaration() != null) {
                    String type = componentContext.compositeDeclaration().declaration().type().getText();
                    String identifier = componentContext.compositeDeclaration().declaration().declarationPart(0).Identifier().getText();
                    String constructorSignature = componentContext.compositeDeclaration().declaration().declarationPart(0).expression().accept(new ExpressionTranspiler());
                    component.append(type);
                    component.append(" ");
                    component.append(identifier);
                    component.append(" = ");
                    component.append(constructorSignature.replaceAll(" ", ""));

                    // See if there is any "handles ... as ..." methods
                    if(componentContext.compositeDeclaration().handlesClause().HANDLES() != null){
                        for (ConfluxParser.DelegateMethodContext delegateCtx : componentContext.compositeDeclaration().handlesClause().delegateMethod()) {
                            handlesToMethods(mbList, identifier, delegateCtx);
                        }
                    }
                }
                components.add(new Pair<>(component.toCode(), mbList));
            }
        }
        return components;
    }

    private static void handlesToMethods(List<MethodBuilder> mbList, String identifier, ConfluxParser.DelegateMethodContext delegateCtx) {
        String delegateID = delegateCtx.methodId().getText();
        StringBuilder delegateSignature = new StringBuilder(delegateID);
        delegateSignature.append("(");
        MethodBuilder mb = new MethodBuilder();
        mb.setIdentifier(delegateID);

        if(delegateCtx.variableList() != null) {
            for (ListIterator<ConfluxParser.VariableContext> it = delegateCtx.variableList().variable().listIterator(); it.hasNext(); ) {
                ConfluxParser.VariableContext vc = it.next();
                delegateSignature.append(vc.variableId().getText());

                if (it.hasNext())
                    delegateSignature.append(", ");
                else
                    delegateSignature.append(")");

                mb.addParameter(vc.type().getText(), vc.variableId().getText());
            }

        } else
            delegateSignature.append(")");
        if (delegateCtx.renameMethod() != null) {
            mb.setIdentifier(delegateCtx.renameMethod().Identifier().getText());
        }
        mb.addStatement(delegateSignature.insert(0, ".").insert(0, identifier).append(";").toString());
        mbList.add(mb);
    }
}