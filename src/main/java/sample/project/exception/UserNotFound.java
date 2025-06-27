package sample.project.exception;

public class UserNotFound extends RuntimeException {
    public UserNotFound(String identifier) {
        super("User with this " + identifier + " doesn't exsist");
    }
}
