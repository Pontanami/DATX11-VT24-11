package transpiler.visitors;

import grammar.gen.ConfluxParser;
import grammar.gen.ConfluxParser.ProgramContext;
import grammar.gen.ConfluxParser.TypeDeclarationContext;
import grammar.gen.ConfluxParserBaseVisitor;
import grammar.gen.ConfluxParserVisitor;
import java_builder.ClassBuilder;
import java_builder.InterfaceBuilder;
import java_builder.MethodBuilder;
import java_builder.SpaceIndentation;
import org.antlr.v4.runtime.tree.TerminalNode;
import transpiler.Environment;

import java.util.List;
import java.util.function.Consumer;

public class TypeVisitor extends ConfluxParserBaseVisitor<Void> {
    //private ConfluxParserVisitor<?> classVisitor
    ConfluxParserVisitor<String> observerTranspiler;

    @Override
    public Void visitProgram(ProgramContext ctx) {
        if (ctx.typeDeclaration() != null) {
            return visitTypeDeclaration(ctx.typeDeclaration());
        } else {
            throw new RuntimeException("Top level declaration not implemented");
        }
    }

    @Override
    public Void visitTypeDeclaration(TypeDeclarationContext ctx) {
        String typeId = ctx.Identifier().toString();
        InterfaceBuilder interfaceBuilder = new InterfaceBuilder();
        interfaceBuilder.setIdentifier(typeId);
        if (ctx.typeExtend() != null) { // if there is an extends clause
            ctx.typeExtend().Identifier().forEach(t -> interfaceBuilder.addExtendedInterface(t.toString()));
        }
        if (ctx.typePublishes() != null) { // if there is a publishes clause
            observerTranspiler.visitTypePublishes(ctx.typePublishes());
        }
        ctx.typeBody().interfaceBlock().methodSignature().forEach(m -> {
            interfaceBuilder.addMethod(transpileMethod(m));
        });
        System.out.println(interfaceBuilder.toCode(new SpaceIndentation(3)));
        return null;

        /*if (ctx.typeBody().getChildCount() > 1) { // means that we need to generate a class
            ClassBuilder classBuilder = new ClassBuilder();
            classBuilder.setIdentifier(Environment.classId(typeId));
        }*/
    }

    // TODO: make this a visitor
    private MethodBuilder transpileMethod(ConfluxParser.MethodSignatureContext m) {
        MethodBuilder methodBuilder = new MethodBuilder();
        methodBuilder.setIdentifier(m.methodName().toString());


        return new MethodBuilder(false).setIdentifier("test").setReturnType("void");
    }
}
