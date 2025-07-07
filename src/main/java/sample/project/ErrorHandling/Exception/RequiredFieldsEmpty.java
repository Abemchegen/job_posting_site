package sample.project.ErrorHandling.Exception;

import java.util.List;

public class RequiredFieldsEmpty extends RuntimeException {
    public RequiredFieldsEmpty(String object, List<String> fields) {
        super("Required field(s) for " + object + ": " + String.join(", ", fields) + " not filled.");
    }
}
