package transpiler.visitors;

import grammar.gen.TheParser.*;
import grammar.gen.TheParserBaseVisitor;
import grammar.gen.TheParserVisitor;
import java_builder.*;
import transpiler.Environment;
import transpiler.tasks.TaskQueue;
import transpiler.TranspilerState;
import transpiler.tasks.TranspilerTask;

import java.util.List;

import static transpiler.tasks.TaskQueue.Priority;

// Visitor for everything related to observers, handles the following methods:
// visitTypePublishes
// visitPublishStatement
// visitAddSubscriberStatement
// visitRemoveSubscriberStatement
public class ObserverTranspiler extends TheParserBaseVisitor<String> {
    private final String publish = Environment.reservedId("publish");
    private final String addSubscriber = Environment.reservedId("addSubscriber");
    private final String removeSubscriber = Environment.reservedId("removeSubscriber");

    private final TheParserVisitor<String> expressionTranspiler;
    private final TaskQueue taskQueue;

    public ObserverTranspiler(TheParserVisitor<String> expressionTranspiler, TaskQueue taskQueue) {
        this.expressionTranspiler = expressionTranspiler;
        this.taskQueue = taskQueue;
    }

    @Override
    public String visitTypePublishes(TypePublishesContext ctx) {
        String typeId = ctx.getParent().accept(this);
        List<String> eventTypes = ctx.Identifier().stream().map(Object::toString).toList();
        taskQueue.addTask(Priority.MAKE_OBSERVERS, new ObserverTask(typeId, eventTypes));
        return "";
    }

    @Override
    // get the type identifier
    public String visitTypeDeclaration(TypeDeclarationContext ctx) {
        return ctx.Identifier().toString();
    }

    @Override
    public String visitPublishStatement(PublishStatementContext ctx) {
        String explicitEventType = ctx.Identifier() == null ? null : ctx.Identifier().toString();
        String event = ctx.expression().accept(expressionTranspiler);
        return new CodeBuilder()
                .append(publish).append("(")
                .beginConditional(explicitEventType != null)
                    .append("(").append(explicitEventType).append(") ")
                .endConditional()
                .append(event)
                .append(");")
                .toCode();
    }

    @Override
    public String visitAddSubscriberStatement(AddSubscriberStatementContext ctx) {
        String publisher = ctx.expression().accept(expressionTranspiler);
        String subscriber = ctx.Identifier(0).toString(); //TODO: should maybe be qualified
        String callback = ctx.Identifier(1).toString();
        String explicitEventType = ctx.Identifier(2) == null ? null : ctx.Identifier(2).toString();

        return new CodeBuilder()
                .append(publisher).append(".").append(addSubscriber).append("(")
                .beginDelimiter(", ")
                .append(subscriber)
                .append('"' + callback + '"')
                .append(new CodeBuilder()
                        .beginConditional(explicitEventType != null)
                            .append("(").append(subscriberCallbackType(explicitEventType)).append(") ")
                        .endConditional()
                        .append(subscriber).append("::").append(callback)
                ).endDelimiter()
                .append(");")
                .toCode();
    }

    @Override
    public String visitRemoveSubscriberStatement(RemoveSubscriberStatementContext ctx) {
        String publisher = ctx.expression().accept(expressionTranspiler);
        String subscriber = ctx.Identifier(0).toString(); //TODO: should maybe be qualified
        String callback = ctx.Identifier(1).toString();
        String explicitEventType = ctx.Identifier(2) == null ? null : ctx.Identifier(2).toString();

        return new CodeBuilder()
                .append(publisher).append(".").append(removeSubscriber).append("(")
                .beginDelimiter(", ")
                .append(subscriber)
                .append('"' + callback + '"')
                .append(new CodeBuilder()
                        .beginConditional(explicitEventType != null)
                            .append("(").append(subscriberCallbackType(explicitEventType)).append(") ")
                        .endConditional()
                        .append(subscriber).append("::").append(callback)
                ).endDelimiter()
                .append(");")
                .toCode();
    }

    // Create the name of the event handler instance variable that the publisher uses
    private String eventHandlerId(String eventType) {
        return Environment.reservedId(eventType + "Handler");
    }
    // Create the identifier of the interface for subscriber callbacks for the given event
    private String subscriberCallbackType(String eventType) {
        return Environment.reservedId(eventType + "Callback");
    }

    // Represents the task of adding all the methods/instance variables to publisher classes/interfaces
    private class ObserverTask implements TranspilerTask {
        private final String typeId;
        private final List<String> eventTypes;

        ObserverTask(String typeId, List<String> eventTypes) {
            this.typeId = typeId;
            this.eventTypes = eventTypes;
        }

        @Override
        public void run(TranspilerState state) {
            InterfaceBuilder publisherInterface = state.lookupInterface(typeId);
            if (publisherInterface == null) {
                throw new RuntimeException("Cannot add publisher methods, no interface found for type identifier '"
                                           + typeId + "'");
            }
            for (String eventType : eventTypes) {
                publisherInterface.addMethod(new MethodBuilder(false, addSubscriberMethod(eventType)))
                                  .addMethod(new MethodBuilder(false, removeSubscriberMethod(eventType)));

                addSubscriberCallbackInterface(state, eventType);
            }
            List<ClassBuilder> classes = state.getClasses().stream().filter(this::implementsPublisher).toList();
            makePublisherClasses(classes);
        }
        // returns true if the class implements the publisher interface
        private boolean implementsPublisher(ClassBuilder builder) {
            return builder.getImplementedInterfaces().stream().map(Code::toCode).anyMatch(i -> i.equals(typeId));
        }

        private void addSubscriberCallbackInterface(TranspilerState state, String eventType) {
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

        private void makePublisherClasses(List<ClassBuilder> publisherClasses) {
            for (ClassBuilder publisherClass : publisherClasses) {
                boolean firstEvent = true;
                for (String eventType : eventTypes) {
                    if (firstEvent) {
                        publisherClass.addImport(Environment.RUNTIME_PACKAGE + ".EventHandler");
                        firstEvent = false;
                    }
                    publisherClass.addField(handlerVariable(eventType))
                                  .addMethod(publishMethod(eventType))
                                  .addMethod(addSubscriberMethod(eventType))
                                  .addMethod(removeSubscriberMethod(eventType));
                }
            }
        }

        private Code handlerVariable(String eventType) {
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
        private MethodBuilder publishMethod(String eventType) {
            return new MethodBuilder()
                    .addModifier("private")
                    .setReturnType("void")
                    .setIdentifier(publish)
                    .addParameter(eventType, "event")
                    .addStatement(new CodeBuilder().append(eventHandlerId(eventType)).append(".publish(event);"));
        }
        private MethodBuilder addSubscriberMethod(String eventType) {
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
        private MethodBuilder removeSubscriberMethod(String eventType) {
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
