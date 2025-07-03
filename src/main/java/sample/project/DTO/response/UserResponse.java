
package sample.project.DTO.response;

import java.time.LocalDate;

import sample.project.Model.Cv;
import sample.project.Model.Role;

public record UserResponse(Long id, String name, String username, Long companyId, String companyName,
                String companyPhonenumber,
                Cv cv, String email,
                String phonenumber,
                LocalDate birthdate,
                Role role) {
}
