package runtime.decorators;

public final class DecoratorTag implements Tag {
    private Runnable deleteAction;

    DecoratorTag(Runnable deleteAction) {
        this.deleteAction = deleteAction;
    }

    @Override
    public void delete() {
        if (deleteAction != null) { // only run delete the first time
            deleteAction.run();
            deleteAction = null;
        }
    }
}
