package sample.project.DTO.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

public record RegisterRequest(
        @NotEmpty String name,
        @NotEmpty String username,
        @NotEmpty String email,
        @NotEmpty String role,
        @NotEmpty String phonenumber,
        @Past @NotNull LocalDate birthdate,
        @NotEmpty String password,
        String companyName,
        String companyPhonenumber) {

}
