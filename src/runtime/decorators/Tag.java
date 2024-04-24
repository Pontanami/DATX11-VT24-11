package runtime.decorators;

public sealed interface Tag permits DecoratorTag {
    /**
     * Delete the object associated with this tag from the object it's registered to
     */
    void delete();
}
