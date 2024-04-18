package transpiler.visitors;

import grammar.gen.ConfluxParser.*;
import grammar.gen.ConfluxParserBaseVisitor;
import grammar.gen.ConfluxParserVisitor;
import java_builder.*;
import org.antlr.v4.runtime.tree.RuleNode;
import transpiler.Environment;
import transpiler.TranspilerState;
import transpiler.tasks.TaskQueue;
import transpiler.tasks.TranspilerTask;

import java.util.*;

import static transpiler.tasks.TaskQueue.Priority;

// Visitor for everything related to observers, handles the following methods:
// visitTypePublishes
// visitPublishStatement
// visitAddSubscriberStatement
// visitRemoveSubscriberStatement
public class ObserverTranspiler extends ConfluxParserBaseVisitor<String> {
    private static final String publish = Environment.reservedId("publish");
    private static final String addSubscriber = Environment.reservedId("addSubscriber");
    private static final String removeSubscriber = Environment.reservedId("removeSubscriber");

    private final ConfluxParserVisitor<String> expressionTranspiler = new DefaultTranspiler(); //TODO: switch to ExpressionTranspiler
    private final TaskQueue taskQueue;

    private String typeId;
    private String classId;

    public ObserverTranspiler(TaskQueue taskQueue) {
        this.taskQueue = taskQueue;
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
        ObserverTask task = new ObserverTask(typeId, classId, ctx.accept(new EventTypesGetter()));
        taskQueue.addTask(Priority.MAKE_OBSERVERS, task);
        return "";
    }

    @Override
    public String visitPublishStatement(PublishStatementContext ctx) {
        String explicitEventType = getExplicitEventType(ctx.explicitEventType());
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
        String publisher = ctx.publisherExpression().accept(expressionTranspiler);
        String subscriber = ctx.subscriberExpression().accept(expressionTranspiler);
        String callback = ctx.subscriberCallback().getText();
        String explicitEventType = getExplicitEventType(ctx.explicitEventType());

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
        String publisher = ctx.publisherExpression().accept(expressionTranspiler);
        String subscriber = ctx.subscriberExpression().accept(expressionTranspiler);
        String callback = ctx.subscriberCallback().getText();
        String explicitEventType = getExplicitEventType(ctx.explicitEventType());

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

    private String getExplicitEventType(ExplicitEventTypeContext ctx) {
        return ctx == null ? null : autobox(ctx.type());
    }

    // Create the name of the event handler instance variable that the publisher uses
    private static String eventHandlerId(String eventType) {
        return Environment.reservedId(eventType + "Handler");
    }
    // Create the identifier of the interface for subscriber callbacks for the given event
    private static String subscriberCallbackType(String eventType) {
        return Environment.reservedId(eventType + "Callback");
    }

    // Represents the task of adding all the methods/instance variables to publisher classes/interfaces
    private static class ObserverTask implements TranspilerTask {
        private final String typeId;
        private final String classId;
        private final List<String> eventTypes;

        ObserverTask(String typeId, String classId, List<String> eventTypes) {
            this.typeId = typeId;
            this.classId = classId;
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
            ClassBuilder publisherClass = state.lookupClass(classId);
            if (publisherClass != null) {
                addPublisherClassAttributes(publisherClass, getAllClassEvents(state));
            }
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
        // Return the set of all event types that can be published by the type from which the current class is generated
        // as well as all its supertypes
        private Set<String> getAllClassEvents(TranspilerState state) {
            Set<String> classEventTypes = new HashSet<>(eventTypes);
            // stack of supertypes that need to be checked for additional published events
            Deque<Code> unchecked = new ArrayDeque<>(state.lookupInterface(typeId).getExtendedInterfaces());
            while (!unchecked.isEmpty()) {
                String superType = unchecked.pop().toCode();
                classEventTypes.addAll(state.lookupSource(superType).accept(new EventTypesGetter()));
                unchecked.addAll(state.lookupInterface(superType).getExtendedInterfaces());
            }
            return classEventTypes;
        }
        // Add all the methods and fields necessary for publisher classes, to the given class
        private void addPublisherClassAttributes(ClassBuilder publisherClass, Set<String> eventTypes) {
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
    // Visitor for getting all the publishable event types from a type
    private static class EventTypesGetter extends ConfluxParserBaseVisitor<List<String>> {
        @Override
        public List<String> visitProgram(ProgramContext ctx) {
            if (ctx.typeDeclaration() != null) {
                return visitTypeDeclaration(ctx.typeDeclaration());
            }
            return defaultResult();
        }
        @Override
        public List<String> visitTypeDeclaration(TypeDeclarationContext ctx) {
            return visitTypePublishes(ctx.typePublishes());
        }
        @Override
        public List<String> visitTypePublishes(TypePublishesContext ctx) {
            if (ctx != null) {
                return ctx.type().stream().map(ObserverTranspiler::autobox).toList();
            }
            return defaultResult();
        }
        @Override
        protected boolean shouldVisitNextChild(RuleNode node, List<String> currentResult) {
            return false;
        }
        @Override
        protected List<String> defaultResult() {
            return List.of();
        }
    }

    private static String autobox(TypeContext ctx) {
        String type = ctx.getText();
        return switch (type) {
            case "int" -> "Integer";
            case "float" -> "Float";
            default -> type;
        };
    }
}
