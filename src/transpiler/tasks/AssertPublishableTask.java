package transpiler.tasks;

import grammar.gen.ConfluxParser;
import grammar.gen.ConfluxParser.TypeContext;
import grammar.gen.ConfluxParserBaseVisitor;
import grammar.gen.ConfluxParserVisitor;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import transpiler.Environment;
import transpiler.TranspilerException;
import transpiler.TranspilerState;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

// For a given publisher type, assert that all immediate subtypes publishes (at least) all the same events as the given
// type
public class AssertPublishableTask implements TranspilerTask {
    private final String publisherTypeId;
    private final Set<String> eventTypes;

    public AssertPublishableTask(String publisherTypeId, Collection<String> eventTypes) {
        this.publisherTypeId = publisherTypeId;
        this.eventTypes = new HashSet<>(eventTypes);
    }

    @Override
    public void run(TranspilerState state) {
        ConfluxParserVisitor<Void> asserter = new AssertEventsPublishedVisitor();
        for (ParseTree tree : state.getSources().values()) {
            tree.accept(asserter);
        }
    }

    private class AssertEventsPublishedVisitor extends ConfluxParserBaseVisitor<Void> {
        @Override
        public Void visitProgram(ConfluxParser.ProgramContext ctx) {
            return ctx.typeDeclaration() == null ? null : visitTypeDeclaration(ctx.typeDeclaration());
        }

        @Override
        public Void visitTypeDeclaration(ConfluxParser.TypeDeclarationContext ctx) {
            if (ctx.Identifier().getText().equals(publisherTypeId)) return null;//this type is the publisher type, abort
            if (ctx.typeExtend() == null) return null; // this type doesn't extend anything, abort

            for (TerminalNode node : ctx.typeExtend().Identifier()) {
                if (node.toString().equals(publisherTypeId)) { // this type extends the publisher type
                    String id = ctx.Identifier().toString();
                    if (ctx.typePublishes() == null) {
                        throw new TranspilerException("Type '" + id + "' isn't a publisher but extends publisher type '"
                                                      + publisherTypeId + "'");
                    }
                    Set<String> subtypeEvents = ctx
                            .typePublishes().type().stream().map(TypeContext::getText).map(Environment::boxedId)
                            .collect(Collectors.toCollection(HashSet::new));
                    if (!subtypeEvents.containsAll(eventTypes)) {
                        throw new TranspilerException("Type '" + id + "' doesn't publish all the events of super type '"
                                                      + publisherTypeId + "'");
                    }
                }
            }
            return null;
        }
    }
}
