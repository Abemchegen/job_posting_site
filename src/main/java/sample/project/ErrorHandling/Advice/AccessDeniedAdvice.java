package sample.project.ErrorHandling.Advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import sample.project.ErrorHandling.Exception.AccessDenied;

@RestControllerAdvice
public class AccessDeniedAdvice {

    @ExceptionHandler(AccessDenied.class)
    public ResponseEntity<String> accessDeniedHandler(AccessDenied ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }
}
