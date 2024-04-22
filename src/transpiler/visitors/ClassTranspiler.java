package transpiler.visitors;

import grammar.gen.ConfluxParser;
import grammar.gen.ConfluxParserBaseVisitor;
import grammar.gen.ConfluxParserVisitor;
import java_builder.ClassBuilder;
import java_builder.Code;
import java_builder.MethodBuilder;
import transpiler.Environment;
import transpiler.TranspilerException;
import transpiler.TranspilerState;
import transpiler.tasks.TaskQueue;
import transpiler.tasks.TranspilerTask;

import java.util.List;

public class ClassTranspiler extends ConfluxParserBaseVisitor<Void> {

    private ClassBuilder genClass;
    private final TaskQueue taskQueue;
    private String interfaceId;

    private final ConfluxParserVisitor<Code> sT;
    public ClassTranspiler(TaskQueue _taskQueue, ConfluxParserVisitor<Code> statementTranspiler) {
        taskQueue = _taskQueue;
        sT = statementTranspiler;
    }

    @Override
    public Void visitTypeDeclaration(ConfluxParser.TypeDeclarationContext ctx) {
        interfaceId = ctx.Identifier().getText();
        genClass = new ClassBuilder();
        taskQueue.addTask(TaskQueue.Priority.ADD_CLASS, new ClassTask(genClass));
        String typeId = ctx.Identifier().toString();
        genClass.addImplementedInterface(typeId);
        genClass.addModifier("public");
        genClass.setIdentifier(Environment.classId(typeId));
        return visitChildren(ctx);
    }

    @Override
    public Void visitMethodBlock(ConfluxParser.MethodBlockContext ctx){
        if(ctx.methodDeclaration() == null) {
            return null;
        }
        List<ConfluxParser.MethodDeclarationContext> methods = ctx.methodDeclaration();
        for(ConfluxParser.MethodDeclarationContext method : methods){
            MethodBuilder mB = new MethodBuilder(true).addModifier("public");
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
        //Hela detta är temp, i väntan på attributeTranspiler
        List<ConfluxParser.AttributeDeclarationContext> attributedDeclarations = ctx.attributeDeclaration();
        for(ConfluxParser.AttributeDeclarationContext aD : attributedDeclarations){
            ConfluxParser.DeclarationContext attribute = aD.declaration();
            if (attribute.VAR() != null) {
                AddVariableAttribute(attribute.declarationPart(0).Identifier().getText(), attribute.type().getText());
            } else {
                AddAttribute(attribute.declarationPart(0).Identifier().getText(), attribute.type().getText());
            }

            if(aD.AS() == null) {
                continue;
            }
            //Generate getter method
            String methodId = aD.Identifier().toString();
            String returnType = aD.declaration().type().getText();
            MethodBuilder mB = new MethodBuilder(true);
            mB.addModifier("public");
            mB.setIdentifier(methodId);
            mB.setReturnType(returnType);
            mB.addStatement("return this." + aD.declaration().declarationPart(0).Identifier() + ";");
            taskQueue.addTask(null, state -> {
                boolean found = false;
                for (MethodBuilder m : state.lookupClass(interfaceId).getMethods()) {
                    if (m.signatureEquals(mB)) {
                        found = true;
                    }
                }
                if (!found) {
                    throw new TranspilerException("Getter method not present in interface");
                }
            } );
            if (checkMethodExist(mB.getIdentifier().toString())) {
                throw new TranspilerException("Duplicate method id: " + mB.getIdentifier());
            } else {
                genClass.addMethod(mB);
            }


        }


        return visitChildren(ctx);
    }

    @Override
    public Void visitComponentsBlock(ConfluxParser.ComponentsBlockContext ctx){
        /*
        if(ctx.componentsDeclaration() != null){
            List<ConfluxParser.ComponentsDeclarationContext> components = ctx.componentsDeclaration();
            for(ConfluxParser.ComponentsDeclarationContext component : components){
                String id = " ";
              if(component.aggregateDeclaration() != null){
                  id = component.aggregateDeclaration().declarationNoAssign().Identifier().toString();
                  String type = component.aggregateDeclaration().declarationNoAssign().type().getText();
                  AddAttribute(id, type);
              }
              else if(component.compositeDeclaration() != null){
                  //TODO fix [] problem elsewhere
                  id = component.compositeDeclaration().declaration().declarationPart(0).Identifier().toString();
                  String type = component.compositeDeclaration().declaration().type().getText();
                  AddAttribute(id, type);
              }
            }
        }*/
        //String s = ComponentsTranspiler.visitComponentsBlock(ctx).toCode();
        for (ComponentLine component : ComponentsTranspiler.visitComponentsBlock(ctx))
            genClass.addField("private final " + component.component.toCode() + ";");
        return visitChildren(ctx);
    }

    private void AddAttribute(String Identifier, String type){
        genClass.addField("private final " + type + " " + Identifier.toLowerCase() + ";");
    }

    private void AddVariableAttribute(String Identifier, String type){
        genClass.addField("private " + type + " " + Identifier.toLowerCase() + ";");
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
    private static class ClassTask implements TranspilerTask {
        private final ClassBuilder genClass;

        private ClassTask(ClassBuilder genClass) { this.genClass = genClass; }

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