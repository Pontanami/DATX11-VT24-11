package runtime.observers;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

// The EventHandler class is used by transpiled publisher classes. It manages a collection of subscribers and handles
// publishing events. The order in which subscribers are notified is undefined.
public final class EventHandler<E> {
    private final Map<Subscriber, Consumer<E>> subscribers;

    public EventHandler() {
        subscribers = new HashMap<>();
    }

    public SubscriberTag _addSubscriber(Object subInstance, String callbackName, Consumer<E> callback) {
        Subscriber key = new Subscriber(subInstance, callbackName);
        subscribers.put(key, callback);
        return new SingleSubscriberTag(() -> subscribers.remove(key));
    }

    public void _publish(E event) {
        for (Consumer<E> callback : subscribers.values()) {
            callback.accept(event);
        }
    }

    public int subscriberCount() {
        return subscribers.size();
    }

    private record Subscriber(Object instance, String callbackName) {
        private Subscriber(Object instance, String callbackName) {
            this.instance = Objects.requireNonNull(instance);
            this.callbackName = Objects.requireNonNull(callbackName);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Subscriber that = (Subscriber) o;
            return instance == that.instance && callbackName.equals(that.callbackName);
        }
        @Override
        public int hashCode() {
            return Objects.hash(System.identityHashCode(instance), callbackName);
        }
    }
}
