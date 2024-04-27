package runtime.observers;

public sealed interface SubscriberTag permits SingleSubscriberTag, CompositeSubscriberTag {
    void unsubscribe();
}
