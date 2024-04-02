package transpiler;

import grammar.gen.TheParser.*;
import grammar.gen.TheParserBaseListener;
import grammar.gen.TheParserListener;
import java_builder.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;

public class ObserverTranspiler {
    // keywords/types
    private final String PUBLISH = reservedId("publish");
    private final String ADD_SUBSCRIBER = reservedId("addSubscriber");
    private final String REMOVE_SUBSCRIBER = reservedId("removeSubscriber");
    private final String PUBLIC = "public";
    private final String PRIVATE = "private";
    private final String FINAL = "final";
    private final String VOID = "void";
    private final String OBJECT = "Object";
    private final String STRING = "String";

    // Create an identifier that is reserved for the generated java code, thus unavailable in the source language
    private String reservedId(String id) {
        return "_" + id;
    }

    // Create the name of the event handler instance variable that the publisher uses
    private String eventHandlerId(String eventType) {
        return reservedId(eventType + "Handler");
    }

    // Create the identifier of the interface for subscriber callbacks for the given event
    private String subscriberCallbackType(String eventType) {
        return reservedId(eventType + "Callback");
    }

    private Code genericType(String id, String... typeArgs) {
        return new CodeBuilder(id).append("<").beginDelimiter(", ").append(typeArgs).endDelimiter().append(">");
    }

    public void transpile(Environment env) {
        hardCodeClasses(env);
        ParseTreeWalker walker = new ParseTreeWalker();
        Listener listener = new Listener(env);
        for (String id : env.getSourceIds()) {
            listener.currentTopLevelId = id;
            walker.walk(listener, env.lookupSource(id));
        }
    }

    private class Listener extends TheParserBaseListener implements TheParserListener {
        private final Environment environment;
        private String currentTopLevelId;
        private String currentMethod;

        private Listener(Environment environment) { this.environment = environment; }

        @Override
        public void enterTypePublishes(TypePublishesContext ctx) {
            ClassBuilder publisher = environment.lookupClass(currentTopLevelId);
            if (publisher == null) {
                throw new RuntimeException("Unhandled case: A publisher isn't a class");
            }
            List<TerminalNode> identifier = ctx.Identifier();
            for (int i = 0; i < identifier.size(); i++) {
                addPublisherAttributes(identifier.get(i).toString(), i == 0);
            }
        }

        private void addPublisherAttributes(String eventName, boolean firstEvent) {
            ClassBuilder publisher = environment.lookupClass(currentTopLevelId);
            Code handlerType = genericType("EventHandler", eventName);
            String handlerId = eventHandlerId(eventName);

            String callbackType = subscriberCallbackType(eventName);
            if (environment.lookupInterface(callbackType) == null) {
                environment.createInterface(callbackType)
                           .addImport("java.util.function.Consumer")
                           .addModifier(PUBLIC)
                           .addExtendedInterface(genericType("Consumer", eventName));
            }

            publisher.addField(createHandlerField(handlerType.toCode(), handlerId));
            publisher.addMethod(createPublishMethod(eventName, handlerId));
            publisher.addMethod(createAddSubscriberMethod(callbackType, handlerId));
            publisher.addMethod(createRemoveSubscriberMethod(callbackType, handlerId));
            if (firstEvent)
                publisher.addImport("transpiler.runtime.EventHandler");
        }

        @Override
        public void enterMethodDeclaration(MethodDeclarationContext ctx) {
            currentMethod = ctx.Identifier().toString();
        }

        @Override
        public void enterPublishStatement(PublishStatementContext ctx) {
            Code publishStatement = new CodeBuilder()
                    .append(PUBLISH).append("(")
                    .beginConditional(ctx.Identifier() != null)
                    .append("(").append(() -> ctx.Identifier().toString()).append(") ")
                    .endConditional()
                    .append(getPublishedEvent(ctx.expression()))
                    .append(");");
            getCurrentMethod().addStatement(publishStatement);
        }

        private Code getPublishedEvent(ExpressionContext exp) {
            return new CodeBuilder().append("new ")
                                    .append(exp.methodCall().qualifiedIdentifier().Identifier(0).toString())
                                    .append("()");
        }

        @Override
        public void enterAddSubscriberStatement(AddSubscriberStatementContext ctx) {
            String publisher = ctx.expression().qualifiedIdentifier().Identifier(0).toString();
            String subscriber = ctx.Identifier(0).toString();
            String callback = ctx.Identifier(1).toString();

            Code statement = new CodeBuilder()
                    .append(publisher).append(".").append(ADD_SUBSCRIBER).append("(")
                    .beginDelimiter(", ")
                    .append(subscriber)
                    .append("\"").append(callback).append("\"")
                    .append(new CodeBuilder()
                            .beginConditional(ctx.Identifier(2) != null)
                            .append("(")
                            .append(() -> subscriberCallbackType(ctx.Identifier(2).toString()))
                            .append(") ")
                            .endConditional()
                            .append(subscriber).append("::").append(callback)
                    )
                    .endDelimiter()
                    .append(");");
            getCurrentMethod().addStatement(statement);
        }

        @Override
        public void enterRemoveSubscriberStatement(RemoveSubscriberStatementContext ctx) {
            String publisher = ctx.expression().qualifiedIdentifier().Identifier(0).toString();
            String subscriber = ctx.Identifier(0).toString();
            String callback = ctx.Identifier(1).toString();

            Code statement = new CodeBuilder()
                    .append(publisher).append(".").append(REMOVE_SUBSCRIBER).append("(")
                    .beginDelimiter(", ")
                    .append(subscriber)
                    .append("\"").append(callback).append("\"")
                    .append(new CodeBuilder()
                            .beginConditional(ctx.Identifier(2) != null)
                            .append("(")
                            .append(() -> subscriberCallbackType(ctx.Identifier(2).toString()))
                            .append(") ")
                            .endConditional()
                            .append(subscriber).append("::").append(callback)
                    )
                    .endDelimiter()
                    .append(");");
            getCurrentMethod().addStatement(statement);
        }

        private MethodBuilder getCurrentMethod() {
            for (Code method : environment.lookupClass(currentTopLevelId).getMethods()) {
                if (method instanceof MethodBuilder mb && mb.getIdentifier().toCode().equals(currentMethod)) {
                    return mb;
                }
            }
            throw new RuntimeException("Method not found: " + currentMethod);
        }
    }


    ////////////////////////// Code Builders for publishers //////////////////////////

    private Code createHandlerField(String handlerType, String handlerId) {
        return new CodeBuilder()
                .beginDelimiter(" ")
                .append(PRIVATE)
                .append(FINAL)
                .append(handlerType)
                .append(handlerId)
                .append("=", "new")
                .append(handlerType)
                .endDelimiter()
                .append("();");
    }

    private MethodBuilder createPublishMethod(String eventType, String handlerId) {
        return new MethodBuilder()
                .addModifier(PRIVATE)
                .setReturnType(VOID)
                .setIdentifier(PUBLISH)
                .addParameter(eventType, "event")
                .addStatement(new CodeBuilder().append(handlerId).append(".publish(event);"));
    }

    private MethodBuilder createAddSubscriberMethod(String callbackType, String handlerId) {
        return new MethodBuilder()
                .addModifier(PUBLIC)
                .setReturnType(VOID)
                .setIdentifier(ADD_SUBSCRIBER)
                .addParameter(OBJECT, "subscriber")
                .addParameter(STRING, "callbackName")
                .addParameter(callbackType, "callback")
                .addStatement(new CodeBuilder()
                        .append(handlerId)
                        .append(".addSubscriber(subscriber, callbackName, callback);"));
    }

    private MethodBuilder createRemoveSubscriberMethod(String callbackType, String handlerId) {
        return new MethodBuilder()
                .addModifier(PUBLIC)
                .setReturnType(VOID)
                .setIdentifier(REMOVE_SUBSCRIBER)
                .addParameter(OBJECT, "subscriber")
                .addParameter(STRING, "callbackName")
                .addParameter(callbackType, "ignored") // parameter needed for type checking only
                .addStatement(new CodeBuilder()
                        .append(handlerId)
                        .append(".removeSubscriber(subscriber, callbackName);"));
    }

    private void hardCodeClasses(Environment env) {
        env.lookupClass("Test").addModifier(PUBLIC).addMethod(new MethodBuilder()
                .addModifier(PUBLIC)
                .setReturnType(VOID)
                .setIdentifier("runTest")
                .addStatement("TrafficController tc = new TrafficController();")
                .addStatement("GPS gps = new GPS();")
        );

        env.lookupClass("TrafficController").addModifier(PUBLIC).addMethod(new MethodBuilder()
                .addModifier(PUBLIC)
                .setReturnType(VOID)
                .setIdentifier("redirectTraffic")
        );
    }
}
