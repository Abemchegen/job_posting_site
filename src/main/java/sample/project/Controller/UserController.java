package sample.project.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import sample.project.DTO.request.LoginRequest;
import sample.project.DTO.request.RegisterRequest;
import sample.project.DTO.response.AgentResponse;
import sample.project.DTO.response.CompanyResponse;
import sample.project.DTO.response.LoginResponse;
import sample.project.DTO.response.RegisterResponse;
import sample.project.DTO.response.UserResponse;
import sample.project.DTO.response.UserResponseList;
import sample.project.Model.User;
import sample.project.Service.UserService;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/public")
    public ResponseEntity<String> createUser(@Valid @RequestBody RegisterRequest req,
            HttpServletResponse response) {
        System.err.println("here");
        RegisterResponse registerResponse = userService.createUser(req);

        String cookie = "jwt=" + registerResponse.token()
                + "; Max-Age=86400; Path=/; HttpOnly; ";
        response.setHeader("Set-Cookie", cookie);

        return ResponseEntity.ok().body("User Created!");
    }

    @PostMapping("/public/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest req, HttpServletResponse response) {
        LoginResponse loginResponse = userService.login(req);
        String cookie = "jwt=" + loginResponse.token()
                + "; Max-Age=86400; Path=/; HttpOnly; ";
        response.setHeader("Set-Cookie", cookie);
        return ResponseEntity.ok("Login Successful");
    }

    @GetMapping("/{userid}")
    @PreAuthorize("hasAnyRole('AGENT', 'COMPANY')")
    public ResponseEntity<UserResponse> getUser(@PathVariable long userid, @AuthenticationPrincipal User currentUser) {

        if (currentUser.getId() != userid) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        UserResponse response = userService.getUser(userid);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/auth/me")
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal User currentUser) {
        UserResponse resp;

        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        if (currentUser.getRole().toString().equals("AGENT")) {
            resp = AgentResponse.builder()
                    .id(currentUser.getId())
                    .name(currentUser.getName())
                    .email(currentUser.getEmail())
                    .phonenumber(currentUser.getPhonenumber())
                    .birthdate(currentUser.getBirthdate())
                    .role(currentUser.getRole())
                    .cv(currentUser.getAgent() != null ? currentUser.getAgent().getCv() : null)
                    .build();
        } else if (currentUser.getRole().toString().equals("COMPANY")) {
            resp = CompanyResponse.builder()
                    .id(currentUser.getId())
                    .name(currentUser.getName())
                    .email(currentUser.getEmail())
                    .phonenumber(currentUser.getPhonenumber())
                    .birthdate(currentUser.getBirthdate())
                    .role(currentUser.getRole())
                    .companyId(currentUser.getCompany() != null ? currentUser.getCompany().getId() : null)
                    .companyName(currentUser.getCompany() != null ? currentUser.getCompany().getName() : null)
                    .companyPhonenumber(
                            currentUser.getCompany() != null ? currentUser.getCompany().getPhoneNumber() : null)
                    .build();
        } else {
            resp = UserResponse.builder()
                    .id(currentUser.getId())
                    .name(currentUser.getName())
                    .email(currentUser.getEmail())
                    .phonenumber(currentUser.getPhonenumber())
                    .birthdate(currentUser.getBirthdate())
                    .role(currentUser.getRole())
                    .build();
        }

        return ResponseEntity.ok().body(resp);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseList> getUsers() {

        UserResponseList response = userService.getAllUser();
        return ResponseEntity.ok().body(response);

    }

    @PutMapping("/{userid}")
    @PreAuthorize("hasAnyRole('AGENT', 'COMPANY')")
    public ResponseEntity<UserResponse> updateUser(@PathVariable long userid, @RequestBody RegisterRequest req,
            @AuthenticationPrincipal User currentUser) {
        if (currentUser.getId() != userid) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        UserResponse response = userService.updateUser(req, userid);
        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/{userid}")
    @PreAuthorize("hasAnyRole('AGENT', 'COMPANY')")
    public ResponseEntity<String> deleteUser(@PathVariable long userid, @AuthenticationPrincipal User currentUser) {

        if (currentUser.getId() != userid) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own account");
        }

        userService.deleteUser(userid);

        return ResponseEntity.ok().body("User deleted successfully");
    }

}
