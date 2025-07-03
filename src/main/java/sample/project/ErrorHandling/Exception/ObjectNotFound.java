package sample.project.ErrorHandling.Exception;

public class ObjectNotFound extends RuntimeException {
    public ObjectNotFound(String object, String identifier) {
        super(object + " with this " + identifier + " doesn't exist");
    }
}
