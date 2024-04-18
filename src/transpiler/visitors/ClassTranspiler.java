package transpiler.visitors;

import grammar.gen.ConfluxParser;
import grammar.gen.ConfluxParserBaseVisitor;
import java_builder.ClassBuilder;
import java_builder.MethodBuilder;
import transpiler.TranspilerException;
import transpiler.TranspilerState;
import transpiler.tasks.TaskQueue;
import transpiler.tasks.TranspilerTask;

import javax.swing.plaf.nimbus.State;
import java.util.ArrayList;
import java.util.List;

public class ClassTranspiler extends ConfluxParserBaseVisitor<Void> {

    private ClassBuilder genClass;
    private final TaskQueue taskQueue;

    private final StatementTranspiler sT;
    public ClassTranspiler(TaskQueue _taskQueue, StatementTranspiler statementTranspiler) {
        taskQueue = _taskQueue;
        sT = statementTranspiler;
    }

    @Override
    public Void visitTypeDeclaration(ConfluxParser.TypeDeclarationContext ctx) {
        genClass = new ClassBuilder();
        taskQueue.addTask(TaskQueue.Priority.ADD_CLASS, new ClassTask());
        String typeId = ctx.Identifier().toString();
        genClass.addModifier("public");
        genClass.setIdentifier(typeId);
        return null;
    }

    @Override
    public Void visitMethodBlock(ConfluxParser.MethodBlockContext ctx){
        if(ctx.methodDeclaration() == null) {
            return null;
        }
        List<ConfluxParser.MethodDeclarationContext> methods = ctx.methodDeclaration();
        for(ConfluxParser.MethodDeclarationContext method : methods){
            MethodBuilder mB = new MethodBuilder(true);
            MethodTranspiler mT = new MethodTranspiler(mB, sT);
            method.accept(mT);
            if(checkMethodExist(mT.mb.getIdentifier().toString())){
                throw new TranspilerException("Duplicate method id: " + mT.mb.getIdentifier());
            }
            else{
                genClass.addMethod(mT.mb);
            }
        }
        return null;
    }
    @Override
    public Void visitAttributesBlock(ConfluxParser.AttributesBlockContext ctx){
        if(ctx.attributeDeclaration() == null){
         return null;
        }
        List<ConfluxParser.AttributeDeclarationContext> attributedDeclarations = ctx.attributeDeclaration();
        for(ConfluxParser.AttributeDeclarationContext aD : attributedDeclarations){
            ConfluxParser.DeclarationContext attribute = aD.declaration();
            if (attribute.VAR() != null) {
                AddVariableAttribute(attribute.Identifier().toString());
            }
            AddAttribute(attribute.Identifier().toString());

            if(aD.AS() == null) {
                return null;
            }
            //Generate getter method
            String methodId = aD.Identifier().toString();
            MethodBuilder mB = new MethodBuilder(true);
            mB.addModifier("public");
            mB.setIdentifier("get" + methodId);
            mB.setReturnType(methodId);
            mB.addStatement("return this." + methodId.toLowerCase() + ";");
            if (checkMethodExist(mB.getIdentifier().toString())) {
                throw new TranspilerException("Duplicate method id: " + mB.getIdentifier());
            } else {
                genClass.addMethod(mB);
            }
        }
        return null;
    }

    @Override
    public Void visitComponentsBlock(ConfluxParser.ComponentsBlockContext ctx){
        if(ctx.componentsDeclaration() != null){
            List<ConfluxParser.ComponentsDeclarationContext> components = ctx.componentsDeclaration();
            for(ConfluxParser.ComponentsDeclarationContext component : components){
                String id = " ";
              if(component.aggregateDeclaration() != null){
                  id = component.aggregateDeclaration().declarationNoAssign().Identifier().toString();
                  AddAttribute(id);
              }
              else if(component.compositeDeclaration() != null){
                  id = component.compositeDeclaration().declaration().Identifier().toString();
                  AddAttribute(id);
              }
            }
        }
        return null;
    }

    private void AddAttribute(String Identifier){
        genClass.addField("private final " + Identifier + " " + Identifier.toLowerCase() + ";");
    }

    private void AddVariableAttribute(String Identifier){
        genClass.addField("private " + Identifier + " " + Identifier.toLowerCase() + ";");
    }

    private Boolean checkMethodExist(String methodId){
        List<MethodBuilder> methods = genClass.getMethods();
        for(MethodBuilder method : methods){
            if(method.getIdentifier().toString().equals(methodId)){
                return true;
            }
        }
        return false;

    }
    private class ClassTask implements TranspilerTask {

        @Override
        public void run(TranspilerState state) {
            //Build class?
            if(state.doesJavaIdExist(genClass.getIdentifier().toString())){
                throw new TranspilerException("Duplicate type id: " + genClass.getIdentifier());
            }
            state.addClass(genClass);
        }
    }

}