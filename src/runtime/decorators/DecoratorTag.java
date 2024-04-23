package runtime.decorators;

public interface DecoratorTag {
    // delete the associated decorator from the object it decorates
    void delete();
}
