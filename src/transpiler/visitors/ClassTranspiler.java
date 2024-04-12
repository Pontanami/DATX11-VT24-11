package transpiler.visitors;

import grammar.gen.ConfluxParser;
import grammar.gen.ConfluxParserBaseVisitor;
import java_builder.ClassBuilder;
import java_builder.MethodBuilder;
import transpiler.TranspilerState;
import transpiler.tasks.TaskQueue;
import transpiler.tasks.TranspilerTask;

import java.util.ArrayList;
import java.util.List;

public class ClassTranspiler extends ConfluxParserBaseVisitor<Void> {

    private ClassBuilder genClass;
    private TaskQueue taskQueue;

    private StatementTranspiler statementTranspiler;
    public ClassTranspiler(TaskQueue _taskQueue) {
        taskQueue = _taskQueue;
        statementTranspiler = new StatementTranspiler(new ObserverTranspiler(taskQueue));
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
        if(ctx.methodDeclaration() != null){
            List<ConfluxParser.MethodDeclarationContext> methods = ctx.methodDeclaration();
            for(ConfluxParser.MethodDeclarationContext method : methods){
                //genClass.addMethod(method.accept(new MethodTranspiler()));
            }
        }
        return null;
    }
    @Override
    public Void visitAttributesBlock(ConfluxParser.AttributesBlockContext ctx){
        if(ctx.declaration() != null){
            List<ConfluxParser.DeclarationContext> attributes = ctx.declaration();
            List<ConfluxParser.DeclaredVariableListContext> variableList = new ArrayList<>();
            for(ConfluxParser.DeclarationContext attribute : attributes){
                variableList.add(attribute.variableDeclaration().declaredVariableList());
            }
            variableList.forEach(declaredVariableList -> {
                //genClass.addAttributes(declaredVariableList.accept(new AttributeTranspiler()));

                ConfluxParser.VariableContext variable = declaredVariableList.variable(0);
                String type = variable.type().toString();
                if(variable.type().getChildCount() > 1){
                    if(variable.type().primitiveType().getChildCount()>1) {
                        type = variable.type().primitiveType().getChild(0).toString();
                    }
                    else {
                        type = variable.type().getChild(0).toString();
                    }
                }
                String name = variable.variableId().Identifier().toString();
                genClass.addField("private final " + type + " " + name);
            });


        }
        return null;
    }

    private class ClassTask implements TranspilerTask {

        @Override
        public void run(TranspilerState state) {
            //Build class?

            state.addClass(genClass);
        }
    }

    private class ConstructorTask implements TranspilerTask {

        @Override
        public void run(TranspilerState state) {

        }
    }

}