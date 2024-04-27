package runtime.observers;

public final class CompositeSubscriberTag implements SubscriberTag {
    private SubscriberTag[] subscriberTags;

    public CompositeSubscriberTag(SubscriberTag... tags) {
        subscriberTags = tags;
    }

    @Override
    public void unsubscribe() {
        if (subscriberTags != null) {
            for (SubscriberTag tag : subscriberTags) {
                tag.unsubscribe();
            }
            subscriberTags = null;
        }
    }
}
