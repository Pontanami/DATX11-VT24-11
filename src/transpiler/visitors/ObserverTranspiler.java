package transpiler.visitors;

import grammar.gen.ConfluxParser.*;
import grammar.gen.ConfluxParserBaseVisitor;
import grammar.gen.ConfluxParserVisitor;
import java_builder.*;
import transpiler.Environment;
import transpiler.TranspilerState;
import transpiler.tasks.TaskQueue;
import transpiler.tasks.TranspilerTask;

import java.util.*;

import static transpiler.tasks.TaskQueue.Priority;

// Transpiles everything related to observers:
public class ObserverTranspiler extends ConfluxParserBaseVisitor<String> {
    private static final String SUBSCRIBER_TAG_TYPE_ID = "SubscriberTag";
    private static final String COMPOSITE_TAG_TYPE_ID = "CompositeSubscriberTag";
    private static final String PUBLISH = Environment.reservedId("publish");
    private static final String ADD_SUBSCRIBER = Environment.reservedId("addSubscriber");

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
        List<String> eventTypes = ctx.type().stream().map(ObserverTranspiler::autobox).toList();

        taskQueue.addTask(Priority.MAKE_OBSERVER_INTERFACES, new PublisherInterfaceTask(typeId, eventTypes));
        taskQueue.addTask(Priority.MAKE_OBSERVER_INTERFACES, new CallbackInterfaceTask(eventTypes));
        taskQueue.addTask(Priority.MAKE_OBSERVER_CLASSES, new PublisherClassTask(typeId, classId));
        return "";
    }

    @Override
    public String visitPublishStatement(PublishStatementContext ctx) {
        String explicitEventType = visitExplicitEventType(ctx.explicitEventType());
        String event = ctx.expression().accept(expressionTranspiler);
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
    public String visitAddSubscriber(AddSubscriberContext ctx) {
        String publisher = ctx.publisherExpression().accept(expressionTranspiler);
        String subscriber = ctx.subscriberExpression().accept(expressionTranspiler);
        String callback = ctx.subscriberCallback().getText();

        if (ctx.explicitEventTypes() == null) {
            return addSubscriberCall(publisher, subscriber, callback, null);
        } else if (ctx.explicitEventTypes().type().size() == 1) {
            String eventType = autobox(ctx.explicitEventTypes().type().get(0));
            return addSubscriberCall(publisher, subscriber, callback, eventType);
        } else {
            CodeBuilder builder = new CodeBuilder()
                    .append("new ").append(COMPOSITE_TAG_TYPE_ID).append("(").beginDelimiter(", ");
            ctx.explicitEventTypes().type().forEach(t ->
                    builder.append(addSubscriberCall(publisher, subscriber, callback, autobox(t)))
            );
            return builder.endDelimiter().append(")").toCode();
        }
    }

    private String addSubscriberCall(String publisher, String subscriber, String callback, String eventType) {
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
                .append(")")
                .toCode();
    }

    @Override
    public String visitExplicitEventType(ExplicitEventTypeContext ctx) {
        return ctx == null ? null : autobox(ctx.type());
    }

    private static String autobox(TypeContext ctx) {
        String type = ctx.getText();
        return switch (type) {
            case "byte" -> "Byte";
            case "short" -> "Short";
            case "int" -> "Integer";
            case "long" -> "Long";
            case "float" -> "Float";
            case "double" -> "Double";
            default -> type;
        };
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
                publisher.addMethod(addSubscriberMethod(eventType))
                         .addMethod(publishMethod(eventType));
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
                publisher.addField(handlerVariable(eventType))
                         .addMethod(publishMethod(eventType).delegateMethod(handlerId))
                         .addMethod(addSubscriberMethod(eventType).delegateMethod(handlerId));
            }
        }

        private List<String> getEventTypes(TranspilerState state) { //TODO: (quick fix solution) could be more robust
            List<String> eventTypes = new ArrayList<>();
            state.lookupInterface(typeId).getMethods().forEach(m -> {
                if (m.getIdentifier().toCode().equals(PUBLISH)) {
                    eventTypes.add(m.getParameters().get(0).argType());
                }
            });
            return eventTypes;
        }
    }

    ////////////////////////////////////// Generated Publisher Methods/Fields ///////////////////////////////////////

    private static Code handlerVariable(String eventType) {
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
                .setReturnType(SUBSCRIBER_TAG_TYPE_ID)
                .setIdentifier(ADD_SUBSCRIBER)
                .addParameter("Object", "subscriber")
                .addParameter("String", "callbackName")
                .addParameter(subscriberCallbackType(eventType), "callback");
    }
}
