package sample.project.ErrorHandling.Exception;

public class AccessDenied extends RuntimeException {

    public AccessDenied() {
        super("Access Denied");
    }
}
