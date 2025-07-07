package sample.project.Controller;

import java.net.URI;

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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import sample.project.DTO.request.LoginRequest;
import sample.project.DTO.request.RegisterRequest;
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
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<RegisterResponse> createUser(@Valid @RequestBody RegisterRequest req) {
        RegisterResponse response = userService.createUser(req);

        URI url = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(response.id()).toUri();

        return ResponseEntity.created(url).body(response);
    }

    @PostMapping("/public/login")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {
        LoginResponse response = userService.login(req);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userid}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('AGENT', 'COMPANY')")
    public ResponseEntity<UserResponse> getUser(@PathVariable long userid, @AuthenticationPrincipal User currentUser) {

        if (currentUser.getId() != userid) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        UserResponse response = userService.getUser(userid);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseList> getUsers() {

        UserResponseList response = userService.getAllUser();
        return ResponseEntity.ok().body(response);

    }

    @PutMapping("/{userid}")
    @ResponseStatus(HttpStatus.OK)
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
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('AGENT', 'COMPANY')")
    public ResponseEntity<String> deleteUser(@PathVariable long userid, @AuthenticationPrincipal User currentUser) {

        if (currentUser.getId() != userid) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own account");
        }

        userService.deleteUser(userid);

        return ResponseEntity.ok().body("User deleted successfully");
    }

}
