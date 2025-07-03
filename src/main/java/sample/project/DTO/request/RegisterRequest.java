package sample.project.DTO.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotEmpty;

public record RegisterRequest(
                @NotEmpty String name,
                @NotEmpty String username,
                @NotEmpty String email,
                @NotEmpty String role,
                @NotEmpty String phonenumber,
                @NotEmpty LocalDate birthdate,
                @NotEmpty String password,
                @NotEmpty String companyName,
                @NotEmpty String companyPhonenumber) {

}
