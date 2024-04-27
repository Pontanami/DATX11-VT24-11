package runtime.observers;

public final class SingleSubscriberTag implements SubscriberTag {
    private Runnable unsubscribeAction;

    SingleSubscriberTag(Runnable unsubscribeAction) {
        this.unsubscribeAction = unsubscribeAction;
    }

    @Override
    public void unsubscribe() {
        if (unsubscribeAction != null) { // only unsubscribe the first time
            unsubscribeAction.run();
            unsubscribeAction = null;
        }
    }
}
