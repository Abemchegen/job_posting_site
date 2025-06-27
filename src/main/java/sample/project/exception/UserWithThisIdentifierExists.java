package sample.project.exception;

public class UserWithThisIdentifierExists extends RuntimeException {

    public UserWithThisIdentifierExists(String identifier) {
        super("User with this " + identifier + " already exists.");
    }

}
