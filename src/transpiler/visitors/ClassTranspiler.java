package transpiler.visitors;

import grammar.gen.ConfluxParser;
import grammar.gen.ConfluxParserBaseVisitor;
import grammar.gen.ConfluxParserVisitor;
import java_builder.ClassBuilder;
import java_builder.Code;
import java_builder.MethodBuilder;
import org.antlr.v4.runtime.misc.Pair;
import transpiler.Environment;
import transpiler.TranspilerException;
import transpiler.TranspilerState;
import transpiler.tasks.AssertImmutableTask;
import transpiler.tasks.TaskQueue;
import transpiler.tasks.TranspilerTask;
import java.util.List;

public class ClassTranspiler extends ConfluxParserBaseVisitor<Void> {

    private ClassBuilder genClass;
    private final TaskQueue taskQueue;
    private String interfaceId;
    private boolean isImmutable = false;

    private final ConfluxParserVisitor<Code> sT;
    public ClassTranspiler(TaskQueue _taskQueue, ConfluxParserVisitor<Code> statementTranspiler) {
        taskQueue = _taskQueue;
        sT = statementTranspiler;
    }

    @Override
    public Void visitTypeDeclaration(ConfluxParser.TypeDeclarationContext ctx) {
        interfaceId = ctx.Identifier().getText();
        genClass = new ClassBuilder();
        taskQueue.addTask(TaskQueue.Priority.ADD_CLASS, new ClassTask(genClass, interfaceId));
        String typeId = ctx.Identifier().toString();
        genClass.addImplementedInterface(typeId);
        genClass.addModifier("public");
        genClass.setIdentifier(Environment.classId(typeId));

        //Immutable check
        if(ctx.typeModifier() != null) {
            if(ctx.typeModifier().IMMUTABLE() != null) {
                taskQueue.addTask(TaskQueue.Priority.CHECK_IMMUTABLE, new AssertImmutableTask(interfaceId));
                isImmutable = true;
            }
        }

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
            if(checkMethodExist(mT.mb)){
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
            String field = aD.accept(new AttributeTranspiler());
            if(isImmutable && !field.contains("final")) {
               throw new TranspilerException("Immutable type " + interfaceId +  " cannot have mutable fields");
            }
             genClass.addField(field);

            if(aD.AS() == null) {
                continue;
            }
            //Generate getter method
            MethodBuilder mB = generateGetter(aD);
            if (checkMethodExist(mB)) {
                throw new TranspilerException("Duplicate method id: " + mB.getIdentifier());
            } else {
                genClass.addMethod(mB);
            }
        }


        return visitChildren(ctx);
    }

    private MethodBuilder generateGetter(ConfluxParser.AttributeDeclarationContext aD) {
        String methodId = aD.Identifier().toString();
        String returnType = aD.declaration().type().getText();
        MethodBuilder mB = new MethodBuilder(true);
        mB.addModifier("public");
        mB.setIdentifier(methodId);
        mB.setReturnType(returnType);
        mB.addStatement("return this." + aD.declaration().declarationPart(0).Identifier() + ";");
        taskQueue.addTask(TaskQueue.Priority.ADD_GETTER, state -> {
            boolean found = false;
            for (MethodBuilder m : state.lookupInterface(interfaceId).getMethods()) {
                if (m.signatureEquals(mB)) {
                    found = true;
                }
            }
            if (!found) {
                throw new TranspilerException("Getter method not present in interface");
            }
        } );
        return mB;
    }

    @Override
    public Void visitComponentsBlock(ConfluxParser.ComponentsBlockContext ctx){
        for (Pair<String, List<MethodBuilder>> component : ComponentsTranspiler.visitComponentsBlock(ctx)) {
            genClass.addField("private final " + component.a + ";");
            if(!component.b.isEmpty())
                for (MethodBuilder mb : component.b) {
                    genClass.addMethod(mb);
                }
        }

        return visitChildren(ctx);
    }
    private Boolean checkMethodExist(MethodBuilder methodBuilder){
        List<MethodBuilder> methods = genClass.getMethods();
        for(MethodBuilder method : methods){
            if(method.signatureEquals(methodBuilder)){
                return true;
            }
        }
        return false;

    }
    private static class ClassTask implements TranspilerTask {
        private final ClassBuilder genClass;
        private final String interfaceId;

        private ClassTask(ClassBuilder genClass, String id) { this.genClass = genClass; this.interfaceId = id; }
        @Override
        public void run(TranspilerState state) {

            if(state.doesJavaIdExist(genClass.getIdentifier().toCode())){
                throw new TranspilerException("Duplicate type id: " + genClass.getIdentifier());
            }

            if(state.lookupInterface(interfaceId) == null) {
                throw new TranspilerException("No interface found for type: " + interfaceId);
            }

            //Cross-checking the methods in the interface with the methods in the class to add in the correct return
            // type for the component method
            List<MethodBuilder> methods = state.lookupInterface(interfaceId).getMethods();
            List<MethodBuilder> typeMethods = genClass.getMethods();
            methods.forEach(m -> typeMethods.forEach(t -> {
                if(m.signatureEquals(t) && t.getReturnType() == null){
                    t.setReturnType(m.getReturnType());
                    t.addModifier("public");
                }
            }));
            state.addClass(genClass);
        }
    }

}