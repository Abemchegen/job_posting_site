package sample.project.DTO.request;

import java.time.LocalDate;

public record RegisterRequest(
        String name,
        String username,
        String email,
        String role,
        String phonenumber,
        LocalDate birthdate,
        String password,
        String companyName,
        String companyPhonenumber) {

}
