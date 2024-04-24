package transpiler.visitors;

import grammar.gen.ConfluxLexer;
import grammar.gen.ConfluxParser;
import java_builder.Code;
import java_builder.CodeBuilder;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import transpiler.Environment;
import transpiler.TranspilerException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExpressionTranspiler extends DefaultTranspiler {
    private static final String NEW_ARRAY_SIZED = "ofSize";
    private static final String NEW_ARRAY_VALUES = "of";

    @Override
    public String visitArrayConstructor(ConfluxParser.ArrayConstructorContext ctx) {
        String constructorId = ctx.Identifier().getText();
        String elementType = ctx.arrayType().getChild(0).getText();
        List<Code> parameters = new ArrayList<>();
        ctx.parameterList().expression().forEach(e -> parameters.add(Code.fromString(e.accept(this))));

        return switch (constructorId) {
            case NEW_ARRAY_SIZED -> new CodeBuilder()
                    .append("new ")
                    .append(elementType)
                    .beginPrefix("[").beginSuffix("]")
                    .append(parameters)
                    .toCode();
            case NEW_ARRAY_VALUES -> new CodeBuilder()
                    .append("new ")
                    .append(ctx.arrayType().getText())
                    .append("{")
                    .beginDelimiter(", ")
                    .append(parameters)
                    .endDelimiter()
                    .append("}")
                    .toCode();
            default -> throw new TranspilerException("Invalid array constructor: " + constructorId);
        };
    }

    @Override
    public String visitAddDecorator(ConfluxParser.AddDecoratorContext ctx) {
        return "%s.%s(%s.%s(%s))".formatted(
                visitDecoratedObject(ctx.decoratedObject()),
                Environment.reservedId("addDecorator"),
                visitDecoratorId(ctx.decoratorId()),
                visitMethodId(ctx.methodId()),
                ctx.parameterList() == null ? "" : visitParameterList(ctx.parameterList())
        );
    }

    @Override
    public String visitDecoratorId(ConfluxParser.DecoratorIdContext ctx) {
        return Environment.classId(ctx.getText());
    }

    @Override
    public String visitTerminal(TerminalNode node) {
        if (node.getSymbol().getType() == ConfluxLexer.BASE) {
            return Environment.reservedId("getBase") + "()";
        }
        return super.visitTerminal(node);
    }
}
