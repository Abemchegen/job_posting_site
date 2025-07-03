package sample.project.ErrorHandling.Exception;

public class ObjectAlreadyExists extends RuntimeException {
    public ObjectAlreadyExists(String object, String identifier) {
        super(object + " with this " + identifier + " already exsists");
    }
}
