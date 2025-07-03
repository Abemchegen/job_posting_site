package sample.project.ErrorHandling.Advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import sample.project.ErrorHandling.Exception.CompanyInformationRequired;

@RestControllerAdvice
public class CompanyInformationRequiredAdvice {
    @ExceptionHandler(CompanyInformationRequired.class)
    public ResponseEntity<String> companyInformationRequiredHandler(CompanyInformationRequired ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
