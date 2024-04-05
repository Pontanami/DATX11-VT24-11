package transpiler.tasks;

import grammar.gen.TheParser;
import grammar.gen.TheParserBaseVisitor;
import grammar.gen.TheParserVisitor;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import transpiler.TranspilerException;
import transpiler.TranspilerState;

// For a given type assert that all subtypes are immutable
public class AssertImmutableTask implements TranspilerTask {
    private final String immutableTypeId;

    public AssertImmutableTask(String immutableTypeId) { this.immutableTypeId = immutableTypeId; }

    @Override
    public void run(TranspilerState state) {
        TheParserVisitor<Void> asserter = new AssertImmutableVisitor();
        for (ParseTree tree : state.getSources()) {
            tree.accept(asserter);
        }
    }

    private class AssertImmutableVisitor extends TheParserBaseVisitor<Void> {
        @Override
        public Void visitProgram(TheParser.ProgramContext ctx) {
            if (ctx.typeDeclaration() == null) return null; // this isn't a type, abort
            return visitTypeDeclaration(ctx.typeDeclaration());
        }

        @Override
        public Void visitTypeDeclaration(TheParser.TypeDeclarationContext ctx) {
            if (ctx.typeExtend() == null) return null; // this type doesn't extend anything, abort

            for (TerminalNode node : ctx.typeExtend().Identifier()) {
                if (node.toString().equals(immutableTypeId)) { // this type extends the immutable type
                    boolean isImmutable = true; // TODO: replace with check for immutable keyword
                    if (!isImmutable) {
                        String id = ctx.Identifier().toString();
                        throw new TranspilerException("Type '" + id + "' isn't immutable but extends an immutable type");
                    } else
                        break;
                }
            }
            return null;
        }
    }
}
