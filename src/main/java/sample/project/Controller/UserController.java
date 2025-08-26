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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import sample.project.DTO.request.ChangePasswordRequest;
import sample.project.DTO.request.LoginRequest;
import sample.project.DTO.request.RegisterRequest;
import sample.project.DTO.request.VerifyEmailRequest;
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
    public ResponseEntity<String> createUser(@Valid @RequestBody RegisterRequest req) {
        userService.createUser(req);

        return ResponseEntity.ok().body("Register Successful");
    }

    @PostMapping("/public/login")
    public ResponseEntity<UserResponse> login(@RequestBody LoginRequest req, HttpServletResponse response) {
        LoginResponse loginResponse = userService.login(req);
        String cookie = "jwt=" + loginResponse.token()
                + "; Max-Age=86400; Path=/; HttpOnly; ";
        response.setHeader("Set-Cookie", cookie);
        return ResponseEntity.ok(loginResponse.response());
    }

    @GetMapping("/logout")
    @PreAuthorize("hasAnyRole('AGENT', 'COMPANY', 'ADMIN')")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        String cookie = "jwt=; Max-Age=0; Path=/; HttpOnly;";
        response.setHeader("Set-Cookie", cookie);
        return ResponseEntity.ok("Logout Successful");
    }

    @GetMapping("/{userid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUser(@PathVariable long userid, @AuthenticationPrincipal User currentUser) {

        UserResponse response = userService.getUser(userid);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/auth/me")
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        UserResponse response = userService.getUser(currentUser.getId());
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/public/verifyEmail")
    public ResponseEntity<UserResponse> verifyEmail(@RequestBody VerifyEmailRequest req,
            HttpServletResponse response) {
        RegisterResponse registerResponse = userService.verifyEmail(req.code(), req.email());

        String cookie = "jwt=" + registerResponse.token()
                + "; Max-Age=86400; Path=/; HttpOnly; ";
        response.setHeader("Set-Cookie", cookie);

        return ResponseEntity.ok().body(registerResponse.response());

    }

    @GetMapping("/public/resendCode")
    public ResponseEntity<String> resendCode(@RequestParam String email) {
        userService.resendCode(email);
        return ResponseEntity.ok().body("Code resent to email account");
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseList> getUsers(@RequestParam(required = false) String role,
            @RequestParam(required = false) String search,
            @AuthenticationPrincipal User currentUser) {

        UserResponseList response = userService.getAllUser(role, search);
        return ResponseEntity.ok().body(response);

    }

    @PutMapping("/{userid}")
    @PreAuthorize("hasAnyRole('AGENT', 'COMPANY', 'ADMIN')")
    public ResponseEntity<UserResponse> updateUser(@PathVariable long userid, @RequestBody RegisterRequest req,
            @AuthenticationPrincipal User currentUser) {
        if (currentUser.getId() != userid) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Access denied You can only update your own account");
        }

        UserResponse response = userService.updateUser(req, userid);
        return ResponseEntity.ok().body(response);
    }

    @PutMapping("updatePas/{userid}")
    @PreAuthorize("hasAnyRole('AGENT', 'COMPANY', 'ADMIN')")
    public ResponseEntity<String> updateUserPassword(@PathVariable long userid,
            @RequestBody ChangePasswordRequest req,
            @AuthenticationPrincipal User currentUser) {
        if (currentUser.getId() != userid) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Access denied You can only update your own account");
        }

        userService.updateUserPassword(req, userid);
        return ResponseEntity.ok().body("Password updated successfully");
    }

    @DeleteMapping("/{userid}")
    @PreAuthorize("hasAnyRole('AGENT', 'COMPANY', 'ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable long userid, @AuthenticationPrincipal User currentUser) {

        if (currentUser.getId() != userid) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Access denied You can only delete your own account");
        }

        userService.deleteUser(userid);

        return ResponseEntity.ok().body("User deleted successfully");
    }

    @PostMapping("/uploadImage/{userid}")
    @PreAuthorize("hasAnyRole('AGENT', 'COMPANY', 'ADMIN')")
    public ResponseEntity<String> uploadProfileImage(@RequestParam("file") MultipartFile file,
            @PathVariable long userid,
            @AuthenticationPrincipal User currentUser) {

        if (currentUser.getId() != userid) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Access denied You can only update your own profile picture");
        }

        String pfpurl = userService.uploadProfileImage(userid, file);
        if (pfpurl == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Upload failed");
        }
        return ResponseEntity.ok().body(pfpurl);
    }

    @DeleteMapping("deletePfp/{userid}")
    @PreAuthorize("hasAnyRole('AGENT', 'COMPANY', 'ADMIN')")
    public ResponseEntity<String> deletePfp(@PathVariable long userid, @AuthenticationPrincipal User currentUser) {

        if (currentUser.getId() != userid) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Access denied You can only update your own profile picture");
        }

        userService.deletePfp(userid);
        return ResponseEntity.ok().body("Deleted pfp");
    }

}
