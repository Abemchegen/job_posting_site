package sample.project.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class UserWithThisIdentifierExistsAdvice {

    @ExceptionHandler(UserWithThisIdentifierExists.class)
    public ResponseEntity<String> userWithThisEmailExistsHandler(UserWithThisIdentifierExists ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
