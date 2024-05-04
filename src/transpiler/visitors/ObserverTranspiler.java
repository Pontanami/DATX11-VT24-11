package transpiler.visitors;

import grammar.gen.ConfluxParser.*;
import grammar.gen.ConfluxParserBaseVisitor;
import grammar.gen.ConfluxParserVisitor;
import java_builder.*;
import transpiler.Environment;
import transpiler.TranspilerState;
import transpiler.tasks.AssertPublishableTask;
import transpiler.tasks.TaskQueue;
import transpiler.tasks.TranspilerTask;

import java.util.*;

import static transpiler.tasks.TaskQueue.Priority;

// Transpiles everything related to observers:
public class ObserverTranspiler extends ConfluxParserBaseVisitor<String> {
    private static final String PUBLISH = Environment.reservedId("publish");
    private static final String ADD_SUBSCRIBER = Environment.reservedId("addSubscriber");
    private static final String REMOVE_SUBSCRIBER = Environment.reservedId("removeSubscriber");

    private final TaskQueue taskQueue;
    private ConfluxParserVisitor<String> expressionTranspiler;

    private String typeId;
    private String classId;

    public ObserverTranspiler(TaskQueue taskQueue) {
        this.taskQueue = taskQueue;
    }

    public void setExpressionTranspiler(ConfluxParserVisitor<String> expressionTranspiler) {
        this.expressionTranspiler = expressionTranspiler;
    }

    @Override
    public String visitTypeDeclaration(TypeDeclarationContext ctx) {
        if (ctx.typePublishes() == null) {
            return "";
        }
        typeId = ctx.Identifier().getText();
        classId = Environment.classId(typeId);
        return visitTypePublishes(ctx.typePublishes());
    }

    @Override
    public String visitTypePublishes(TypePublishesContext ctx) {
        List<String> eventTypes = ctx.type().stream().map(TypeContext::getText).map(Environment::boxedId).toList();

        taskQueue.addTask(Priority.CHECK_PUBLISHABLE, new AssertPublishableTask(typeId, eventTypes));
        taskQueue.addTask(Priority.MAKE_OBSERVER_INTERFACES, new PublisherInterfaceTask(typeId, eventTypes));
        taskQueue.addTask(Priority.MAKE_OBSERVER_INTERFACES, new CallbackInterfaceTask(eventTypes));
        taskQueue.addTask(Priority.MAKE_OBSERVER_CLASSES, new PublisherClassTask(typeId, classId));
        return "";
    }

    @Override
    public String visitPublishStatement(PublishStatementContext ctx) {
        String event = ctx.expression().accept(expressionTranspiler);
        if (ctx.explicitEventTypes() == null) {
            return makePublishCall(event, null);
        } else if (ctx.explicitEventTypes().type().size() == 1) {
            return makePublishCall(event, Environment.boxedId(ctx.explicitEventTypes().type(0).getText()));
        } else {
            StringBuilder builder = new StringBuilder().append("{");
            ctx.explicitEventTypes().type().forEach(type -> {
                String eventType = Environment.boxedId(type.getText());
                builder.append(makePublishCall(event, eventType));
            });
            return builder.append("}").toString();
        }
    }

    private static String makePublishCall(String event, String explicitEventType) {
        return new CodeBuilder()
                .append(PUBLISH).append("(")
                .beginConditional(explicitEventType != null)
                .append("(").append(explicitEventType).append(") ")
                .endConditional()
                .append(event)
                .append(");")
                .toCode();
    }

    @Override
    public String visitAddSubscriberStatement(AddSubscriberStatementContext ctx) {
        String publisher = ctx.publisherExpression().accept(expressionTranspiler);
        String subscriber = ctx.subscriberExpression().accept(expressionTranspiler);
        String callback = ctx.subscriberCallback().getText();

        if (ctx.explicitEventTypes() == null) {
            return makeAddSubscriberCall(publisher, subscriber, callback, null);
        } else if (ctx.explicitEventTypes().type().size() == 1) {
            String eventType = Environment.boxedId(ctx.explicitEventTypes().type().get(0).getText());
            return makeAddSubscriberCall(publisher, subscriber, callback, eventType);
        } else {
            StringBuilder builder = new StringBuilder().append("{ ");
            ctx.explicitEventTypes().type().forEach(type -> {
                String eventType = Environment.boxedId(type.getText());
                builder.append(makeAddSubscriberCall(publisher, subscriber, callback, eventType));
            });
            return builder.append(" }").toString();
        }
    }

    private String makeAddSubscriberCall(String publisher, String subscriber, String callback, String eventType) {
        return new CodeBuilder()
                .append(publisher).append(".").append(ADD_SUBSCRIBER).append("(")
                .beginDelimiter(", ")
                .append(subscriber)
                .append('"' + callback + '"')
                .append(new CodeBuilder()
                        .beginConditional(eventType != null)
                        .append("(").append(subscriberCallbackType(eventType)).append(") ")
                        .endConditional()
                        .append(subscriber).append("::").append(callback)
                ).endDelimiter()
                .append(");")
                .toCode();
    }

    @Override
    public String visitRemoveSubscriberStatement(RemoveSubscriberStatementContext ctx) {
        String publisher = ctx.publisherExpression().accept(expressionTranspiler);
        String subscriber = ctx.subscriberExpression().accept(expressionTranspiler);
        String callback = ctx.subscriberCallback().getText();

        if (ctx.explicitEventTypes() == null) {
            return makeRemoveSubscriberCall(publisher, subscriber, callback, null);
        } else if (ctx.explicitEventTypes().type().size() == 1) {
            String eventType = Environment.boxedId(ctx.explicitEventTypes().type().get(0).getText());
            return makeRemoveSubscriberCall(publisher, subscriber, callback, eventType);
        } else {
            StringBuilder builder = new StringBuilder().append("{ ");
            ctx.explicitEventTypes().type().forEach(type -> {
                String eventType = Environment.boxedId(type.getText());
                builder.append(makeRemoveSubscriberCall(publisher, subscriber, callback, eventType));
            });
            return builder.append(" }").toString();
        }
    }

    private String makeRemoveSubscriberCall(String publisher, String subscriber, String callback, String eventType) {
        return new CodeBuilder()
                .append(publisher).append(".").append(REMOVE_SUBSCRIBER).append("(")
                .beginDelimiter(", ")
                .append(subscriber)
                .append('"' + callback + '"')
                .append(new CodeBuilder()
                        .beginConditional(eventType != null)
                        .append("(").append(subscriberCallbackType(eventType)).append(") ")
                        .endConditional()
                        .append(subscriber).append("::").append(callback)
                ).endDelimiter()
                .append(");")
                .toCode();
    }

    // Create the name of the event handler instance variable that the publisher uses
    private static String eventHandlerId(String eventType) {
        return Environment.reservedId(eventType + "Handler");
    }
    // Create the identifier of the interface for subscriber callbacks for the given event
    private static String subscriberCallbackType(String eventType) {
        return Environment.reservedId(eventType + "Callback");
    }

    ///////////////////////////////////////// Observer tasks /////////////////////////////////////////////////

    // Add methods to the publisher interfaces
    private record PublisherInterfaceTask(String typeId, List<String> eventTypes) implements TranspilerTask {
        @Override
        public void run(TranspilerState state) {
            InterfaceBuilder publisher = typeId == null ? null : state.lookupInterface(typeId);
            if (publisher == null)
                return;
            for (String eventType : eventTypes) {
                publisher.addMethod(publishMethod(eventType))
                         .addMethod(addSubscriberMethod(eventType))
                         .addMethod(removeSubscriberMethod(eventType));
            }
        }
    }

    // Create callback interfaces for the given event types
    private record CallbackInterfaceTask(List<String> eventTypes) implements TranspilerTask {
        @Override
        public void run(TranspilerState state) {
            for (String eventType : eventTypes) {
                String callbackType = subscriberCallbackType(eventType);
                if (!state.doesJavaIdExist(callbackType)) {
                    state.addInterface(new InterfaceBuilder()
                            .addImport("java.util.function.Consumer")
                            .addModifier("public")
                            .setIdentifier(callbackType)
                            .addExtendedInterface("Consumer<" + eventType + ">")
                    );
                }
            }
        }
    }

    // Add methods and fields to publisher classes
    private record PublisherClassTask(String typeId, String classId) implements TranspilerTask {
        @Override
        public void run(TranspilerState state) {
            ClassBuilder publisher = classId == null ? null : state.lookupClass(classId);
            if (publisher == null)
                return;

            List<String> eventTypes = getEventTypes(state);
            for (String eventType : eventTypes) {
                String handlerId = eventHandlerId(eventType);

                MethodBuilder removeSubMethod = removeSubscriberMethod(eventType).setGenerateBody(true);
                removeSubMethod.addStatement("%s.%s(%s, %s);".formatted(
                        eventHandlerId(eventType),
                        REMOVE_SUBSCRIBER,
                        removeSubMethod.getParameters().get(0).argId(),
                        removeSubMethod.getParameters().get(1).argId()
                ));
                publisher.addField(handlerVariable(eventType))
                         .addMethod(publishMethod(eventType).delegateMethod(handlerId))
                         .addMethod(addSubscriberMethod(eventType).delegateMethod(handlerId))
                         .addMethod(removeSubMethod);
            }
        }

        private List<String> getEventTypes(TranspilerState state) {
            return state.lookupSource(typeId).typeDeclaration().typePublishes().type()
                        .stream().map(TypeContext::getText).map(Environment::boxedId).toList();
        }
    }

    ////////////////////////////////////// Generated Publisher Methods/Fields ///////////////////////////////////////

    private static Code handlerVariable(String eventType) {
        String handlerType = Environment.reservedId("EventHandler") + "<" + eventType + ">";
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

    private static MethodBuilder publishMethod(String eventType) {
        return new MethodBuilder(false)
                .addModifier("public")
                .setReturnType("void")
                .setIdentifier(PUBLISH)
                .addParameter(eventType, "event");
    }

    private static MethodBuilder addSubscriberMethod(String eventType) {
        return new MethodBuilder(false)
                .addModifier("public")
                .setReturnType("void")
                .setIdentifier(ADD_SUBSCRIBER)
                .addParameter("Object", "subscriber")
                .addParameter("String", "callbackName")
                .addParameter(subscriberCallbackType(eventType), "callback");
    }

    private static MethodBuilder removeSubscriberMethod(String eventType) {
        return new MethodBuilder(false)
                .addModifier("public")
                .setReturnType("void")
                .setIdentifier(REMOVE_SUBSCRIBER)
                .addParameter("Object", "subscriber")
                .addParameter("String", "callbackName")
                .addParameter(subscriberCallbackType(eventType), Environment.unusedIdentifier());
    }
}
