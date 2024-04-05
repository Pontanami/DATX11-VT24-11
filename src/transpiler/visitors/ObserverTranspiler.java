package transpiler.visitors;

import grammar.gen.TheParser.*;
import grammar.gen.TheParserBaseVisitor;
import grammar.gen.TheParserVisitor;
import java_builder.*;
import org.antlr.v4.runtime.tree.TerminalNode;
import transpiler.Environment;
import transpiler.tasks.TaskQueue;
import transpiler.TranspilerState;
import transpiler.tasks.TranspilerTask;

import java.util.List;

import static transpiler.tasks.TaskQueue.Priority;

public class ObserverTranspiler extends TheParserBaseVisitor<Code> {
    private final String publish = Environment.reservedId("publish");
    private final String addSubscriber = Environment.reservedId("addSubscriber");
    private final String removeSubscriber = Environment.reservedId("removeSubscriber");

    private final TheParserVisitor<Code> expressionTranspiler;

    public ObserverTranspiler(TheParserVisitor<Code> expressionTranspiler) {
        this.expressionTranspiler = expressionTranspiler;
    }

    public void handlePublishesClause(
            String publisherInterfaceId,
            String publisherClassId,
            TypePublishesContext ctx,
            TaskQueue taskQueue
    ) {
        List<TerminalNode> eventNodes = ctx.Identifier();
        for (int i = 0; i < eventNodes.size(); i++) {
            String eventType = eventNodes.get(i).toString();
            PublishTask task = new PublishTask(publisherInterfaceId, publisherClassId, eventType, i == 0);
            taskQueue.addTask(Priority.MAKE_OBSERVERS, task);
        }
    }

    @Override
    public Code visitPublishStatement(PublishStatementContext ctx) {
        String explicitEventType = ctx.Identifier() == null ? null : ctx.Identifier().toString();
        Code event = ctx.expression().accept(expressionTranspiler);
        return new CodeBuilder()
                .append(publish).append("(")
                .beginConditional(explicitEventType != null)
                .append("(").append(explicitEventType).append(") ")
                .endConditional()
                .append(event)
                .append(");");
    }

    @Override
    public Code visitAddSubscriberStatement(AddSubscriberStatementContext ctx) {
        Code publisher = ctx.expression().accept(expressionTranspiler);
        String subscriber = ctx.Identifier(0).toString(); //TODO: should maybe be qualified
        String callback = ctx.Identifier(1).toString();
        String explicitEventType = ctx.Identifier(2) == null ? null : ctx.Identifier(2).toString();

        return new CodeBuilder()
                .append(publisher).append(".").append(addSubscriber).append("(")
                .beginDelimiter(", ")
                .append(subscriber)
                .append("\"").append(callback).append("\"")
                .append(new CodeBuilder()
                        .beginConditional(explicitEventType != null)
                        .append("(")
                        .append(subscriberCallbackType(explicitEventType))
                        .append(") ")
                        .endConditional()
                        .append(subscriber).append("::").append(callback)
                )
                .endDelimiter()
                .append(");");
    }

    @Override
    public Code visitRemoveSubscriberStatement(RemoveSubscriberStatementContext ctx) {
        Code publisher = ctx.expression().accept(expressionTranspiler);
        String subscriber = ctx.Identifier(0).toString(); //TODO: should maybe be qualified
        String callback = ctx.Identifier(1).toString();
        String explicitEventType = ctx.Identifier(2) == null ? null : ctx.Identifier(2).toString();

        return new CodeBuilder()
                .append(publisher).append(".").append(removeSubscriber).append("(")
                .beginDelimiter(", ")
                .append(subscriber)
                .append("\"").append(callback).append("\"")
                .append(new CodeBuilder()
                        .beginConditional(explicitEventType != null)
                        .append("(")
                        .append(subscriberCallbackType(explicitEventType))
                        .append(") ")
                        .endConditional()
                        .append(subscriber).append("::").append(callback)
                )
                .endDelimiter()
                .append(");");
    }

    // Create the name of the event handler instance variable that the publisher uses
    private String eventHandlerId(String eventType) {
        return Environment.reservedId(eventType + "Handler");
    }

    // Create the identifier of the interface for subscriber callbacks for the given event
    private String subscriberCallbackType(String eventType) {
        return Environment.reservedId(eventType + "Callback");
    }

    private class PublishTask implements TranspilerTask {
        private final String publisherInterfaceId;
        private final String publisherClassId;
        private final String eventType;
        private final boolean addImport;

        private PublishTask(String publisherInterfaceId, String publisherClassId, String eventType, boolean addImport) {
            this.publisherInterfaceId = publisherInterfaceId;
            this.publisherClassId = publisherClassId;
            this.eventType = eventType;
            this.addImport = addImport;
        }

        @Override
        public void run(TranspilerState currentOutput) {
            InterfaceBuilder publisherInterface = currentOutput.lookupInterface(publisherInterfaceId);
            if (publisherInterface != null) {
                publisherInterface.addMethod(new MethodBuilder(false, removeSubscriberMethod()))
                                  .addMethod(new MethodBuilder(false, addSubscriberMethod()));
            }

            ClassBuilder publisherClass = currentOutput.lookupClass(publisherClassId);
            if (publisherClass != null) {
                if (addImport) {
                    publisherClass.addImport(Environment.RUNTIME_PACKAGE + ".EventHandler");
                }
                publisherClass.addField(handlerVariable())
                              .addMethod(publishMethod())
                              .addMethod(addSubscriberMethod())
                              .addMethod(removeSubscriberMethod());
            }
            String callbackType = subscriberCallbackType(eventType);
            if (!currentOutput.doesIdExist(callbackType)) {
                currentOutput.addInterface(new InterfaceBuilder()
                        .addImport("java.util.function.Consumer")
                        .addModifier("public")
                        .setIdentifier(callbackType)
                        .addExtendedInterface("Consumer<" + eventType + ">")
                );
            }
        }

        private Code handlerVariable() {
            String handlerType = "EventHandler<" + eventType + ">";
            return new CodeBuilder()
                    .beginDelimiter(" ")
                    .append("private final")
                    .append(handlerType)
                    .append(eventHandlerId(eventType))
                    .append("= new")
                    .append(handlerType)
                    .endDelimiter()
                    .append("();");
        }

        private MethodBuilder publishMethod() {
            return new MethodBuilder()
                    .addModifier("private")
                    .setReturnType("void")
                    .setIdentifier(publish)
                    .addParameter(eventType, "event")
                    .addStatement(new CodeBuilder().append(eventHandlerId(eventType)).append(".publish(event);"));
        }

        private MethodBuilder addSubscriberMethod() {
            return new MethodBuilder()
                    .addModifier("public")
                    .setReturnType("void")
                    .setIdentifier(addSubscriber)
                    .addParameter("Object", "subscriber")
                    .addParameter("String", "callbackName")
                    .addParameter(subscriberCallbackType(eventType), "callback")
                    .addStatement(new CodeBuilder()
                            .append(eventHandlerId(eventType))
                            .append(".addSubscriber(subscriber, callbackName, callback);"));
        }

        private MethodBuilder removeSubscriberMethod() {
            return new MethodBuilder()
                    .addModifier("public")
                    .setReturnType("void")
                    .setIdentifier(removeSubscriber)
                    .addParameter("Object", "subscriber")
                    .addParameter("String", "callbackName")
                    .addParameter(subscriberCallbackType(eventType), "ignored") // needed for type checking only
                    .addStatement(new CodeBuilder()
                            .append(eventHandlerId(eventType))
                            .append(".removeSubscriber(subscriber, callbackName);"));
        }
    }
}
