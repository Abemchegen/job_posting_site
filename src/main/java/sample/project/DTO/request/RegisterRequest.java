package sample.project.DTO.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
        @NotEmpty
        private String name;

        @NotEmpty
        private String username;

        @NotEmpty
        private String email;

        @NotEmpty
        private String role;

        @NotEmpty
        private String phonenumber;

        @Past
        @NotNull
        private LocalDate birthdate;

        @NotEmpty
        private String password;

        private String companyName;

        private String companyPhonenumber;
}
