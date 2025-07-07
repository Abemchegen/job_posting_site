package sample.project.ErrorHandling.Advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import sample.project.ErrorHandling.Exception.RequiredFieldsEmpty;

@RestControllerAdvice
public class RequiredFieldsEmptyAdvice {
    @ExceptionHandler(RequiredFieldsEmpty.class)
    public ResponseEntity<String> requiredFieldsEmptyHandler(RequiredFieldsEmpty ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
