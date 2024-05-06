package transpiler.tasks;

import grammar.gen.ConfluxParser;
import grammar.gen.ConfluxParserBaseVisitor;
import grammar.gen.ConfluxParserVisitor;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import transpiler.TranspilerException;
import transpiler.TranspilerState;

// For a given immutable type assert that all immediate subtypes are immutable
public class AssertImmutableTask implements TranspilerTask {
    private final String immutableTypeId;

    public AssertImmutableTask(String immutableTypeId) { this.immutableTypeId = immutableTypeId; }

    @Override
    public void run(TranspilerState state) {
        ConfluxParserVisitor<Void> asserter = new AssertImmutableVisitor();
        for (ParseTree tree : state.getSources().values()) {
            tree.accept(asserter);
        }
    }

    private class AssertImmutableVisitor extends ConfluxParserBaseVisitor<Void> {
        @Override
        public Void visitProgram(ConfluxParser.ProgramContext ctx) {
            return ctx.typeDeclaration() == null ? null : visitTypeDeclaration(ctx.typeDeclaration());
        }

        @Override
        public Void visitTypeDeclaration(ConfluxParser.TypeDeclarationContext ctx) {
            if (ctx.Identifier().getText().equals(immutableTypeId)) return null;//this type is the immutable type, abort
            if (ctx.typeExtend() == null) return null; // this type doesn't extend anything, abort

            for (TerminalNode node : ctx.typeExtend().Identifier()) {
                if (node.toString().equals(immutableTypeId)) { // this type extends the immutable type
                    if (ctx.typeModifier().stream().noneMatch(c -> c.IMMUTABLE() != null)) {
                        String id = ctx.Identifier().toString();
                        throw new TranspilerException("Type '" + id + "' isn't immutable but extends immutable type '"
                                                      + immutableTypeId + "'");
                    }
                }
            }
            return null;
        }
    }
}
