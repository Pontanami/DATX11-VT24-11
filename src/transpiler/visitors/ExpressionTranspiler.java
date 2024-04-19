package transpiler.visitors;

import grammar.gen.ConfluxParser;
import java_builder.Code;
import java_builder.CodeBuilder;
import transpiler.TranspilerException;

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
}
