
package sample.project.DTO.response;

import java.time.LocalDate;

import sample.project.Model.Role;

public record UserResponse(Long id, String name, String username, String email, String phonenumber,
        LocalDate birthdate,
        Role role) {
}
