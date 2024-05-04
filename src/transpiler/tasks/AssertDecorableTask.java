package transpiler.tasks;

import grammar.gen.ConfluxParser;
import grammar.gen.ConfluxParserBaseVisitor;
import grammar.gen.ConfluxParserVisitor;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import transpiler.TranspilerException;
import transpiler.TranspilerState;

// For a given decorable type, assert that all immediate subtypes are decorable
public class AssertDecorableTask implements TranspilerTask {
    private final String decorableTypeId;

    public AssertDecorableTask(String decorableTypeId) { this.decorableTypeId = decorableTypeId; }

    @Override
    public void run(TranspilerState state) {
        ConfluxParserVisitor<Void> asserter = new AssertDecorableVisitor();
        for (ParseTree tree : state.getSources().values()) {
            tree.accept(asserter);
        }
    }

    private class AssertDecorableVisitor extends ConfluxParserBaseVisitor<Void> {
        @Override
        public Void visitProgram(ConfluxParser.ProgramContext ctx) {
            return ctx.typeDeclaration() == null ? null : visitTypeDeclaration(ctx.typeDeclaration());
        }

        @Override
        public Void visitTypeDeclaration(ConfluxParser.TypeDeclarationContext ctx) {
            if (ctx.Identifier().getText().equals(decorableTypeId)) return null;//this type is the decorable type, abort
            if (ctx.typeExtend() == null) return null; // this type doesn't extend anything, abort

            for (TerminalNode node : ctx.typeExtend().Identifier()) {
                if (node.toString().equals(decorableTypeId)) { // this type extends the decorable type
                    if (ctx.typeModifier().stream().noneMatch(c -> c.DECORABLE() != null)) {
                        String id = ctx.Identifier().toString();
                        throw new TranspilerException("Type '" + id + "' isn't decorable but extends decorable type '"
                                                      + decorableTypeId + "'");
                    }
                }
            }
            return null;
        }
    }
}
