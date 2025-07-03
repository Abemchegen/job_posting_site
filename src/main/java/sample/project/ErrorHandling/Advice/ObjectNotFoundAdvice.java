package sample.project.ErrorHandling.Advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import sample.project.ErrorHandling.Exception.ObjectNotFound;

@RestControllerAdvice

public class ObjectNotFoundAdvice {
    @ExceptionHandler(ObjectNotFound.class)
    public ResponseEntity<String> objectNotFound(ObjectNotFound ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}
