package runtime.decorators;

public final class DecoratorTag {
    private Runnable deleteAction;

    DecoratorTag(Runnable deleteAction) {
        this.deleteAction = deleteAction;
    }

    public void deleteDecorator() {
        if (deleteAction != null) { // only run delete the first time
            deleteAction.run();
            deleteAction = null;
        }
    }
}
