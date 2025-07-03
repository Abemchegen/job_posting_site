package sample.project.ErrorHandling.Advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import sample.project.ErrorHandling.Exception.ObjectAlreadyExists;

@RestControllerAdvice
public class ObjectAlreadyExistsAdvice {

    @ExceptionHandler(ObjectAlreadyExists.class)
    public ResponseEntity<String> objectAlreadyExistsHandler(ObjectAlreadyExists ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}
