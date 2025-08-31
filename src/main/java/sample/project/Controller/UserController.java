package sample.project.Controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import sample.project.DTO.request.ChangePasswordRequest;
import sample.project.DTO.request.LoginRequest;
import sample.project.DTO.request.RegisterRequest;
import sample.project.DTO.request.VerifyEmailRequest;
import sample.project.DTO.response.LoginResponse;
import sample.project.DTO.response.LoginResponseUser;
import sample.project.DTO.response.UserResponse;
import sample.project.DTO.response.UserResponseList;
import sample.project.ErrorHandling.Exception.ObjectNotFound;
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
    public ResponseEntity<LoginResponseUser> login(@RequestBody LoginRequest req, HttpServletResponse response) {
        LoginResponse loginResponse = userService.login(req);
        if (loginResponse.access_token() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String cookie = "refreshtoken=" + loginResponse.refresh_token() + "; Max-Age=86400; Path=/; HttpOnly; ";
        response.setHeader("Set-Cookie", cookie);
        LoginResponseUser res = new LoginResponseUser(loginResponse.access_token(), loginResponse.response(),
                loginResponse.statusDesc());
        return ResponseEntity.ok(res);
    }

    @PostMapping("/public/refresh")
    public ResponseEntity<LoginResponseUser> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        LoginResponse res = userService.refreshToken(request);
        String cookie = "refreshtoken=" + res.refresh_token() + "; Max-Age=86400; Path=/; HttpOnly; ";
        response.setHeader("Set-Cookie", cookie);
        LoginResponseUser userres = new LoginResponseUser(res.access_token(), res.response(), res.statusDesc());
        return ResponseEntity.ok().body(userres);
    }

    @GetMapping("/logout")
    @PreAuthorize("hasAnyRole('AGENT', 'COMPANY', 'ADMIN')")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        String cookie = "refreshtoken=; Max-Age=0; Path=/; HttpOnly;";
        response.setHeader("Set-Cookie", cookie);
        return ResponseEntity.ok("Logout Successful");
    }

    @GetMapping("/testAuth")
    public Map<String, Object> testAuth(@AuthenticationPrincipal Jwt jwt, Authentication auth) {
        return Map.of(
                "jwt_roles", jwt.getClaimAsMap("realm_access"),
                "authorities", auth.getAuthorities());
    }

    @GetMapping("/{userid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUser(@PathVariable long userid, @AuthenticationPrincipal Jwt jwt) {

        UserResponse response = userService.getUser(jwt.getClaim("email"));
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/auth/me")
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        UserResponse response = userService.getUser(jwt.getClaim("email"));
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/public/verifyEmail")
    public ResponseEntity<String> verifyEmail(@RequestBody VerifyEmailRequest req,
            HttpServletResponse response) {
        userService.verifyEmail(req.code(), req.email());

        return ResponseEntity.ok().body("Email Verified");

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
            @AuthenticationPrincipal Jwt jwt) {

        UserResponseList response = userService.getAllUser(role, search);
        return ResponseEntity.ok().body(response);

    }

    @PutMapping("/{userid}")
    @PreAuthorize("hasAnyRole('AGENT', 'COMPANY', 'ADMIN')")
    public ResponseEntity<UserResponse> updateUser(@PathVariable long userid, @RequestBody RegisterRequest req,
            @AuthenticationPrincipal Jwt jwt) {

        Optional<User> user = userService.getUserByEmail(jwt.getClaim("email"));

        if (!user.isPresent()) {
            throw new ObjectNotFound("user", "email");
        }

        if (user.get().getId() != userid) {
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
            @AuthenticationPrincipal Jwt jwt) {

        Optional<User> user = userService.getUserByEmail(jwt.getClaim("email"));

        if (!user.isPresent()) {
            throw new ObjectNotFound("user", "email");
        }

        if (user.get().getId() != userid) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Access denied You can only update your own account");
        }

        userService.updateUserPassword(req, userid);
        return ResponseEntity.ok().body("Password updated successfully");
    }

    @DeleteMapping("/{userid}")
    @PreAuthorize("hasAnyRole('AGENT', 'COMPANY', 'ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable long userid, @AuthenticationPrincipal Jwt jwt) {

        Optional<User> opuser = userService.getUserByEmail(jwt.getClaim("email"));
        if (!opuser.isPresent()) {
            throw new ObjectNotFound("User", "email");
        }

        if (opuser.get().getId() != userid) {
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
            @AuthenticationPrincipal Jwt jwt) {

        Optional<User> user = userService.getUserByEmail(jwt.getClaim("email"));

        if (!user.isPresent()) {
            throw new ObjectNotFound("user", "email");
        }

        if (user.get().getId() != userid) {
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
    public ResponseEntity<String> deletePfp(@PathVariable long userid, @AuthenticationPrincipal Jwt jwt) {

        Optional<User> opuser = userService.getUserByEmail(jwt.getClaim("email"));
        if (!opuser.isPresent()) {
            throw new ObjectNotFound("User", "email");
        }

        if (opuser.get().getId() != userid) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Access denied You can only update your own profile picture");
        }

        userService.deletePfp(userid);
        return ResponseEntity.ok().body("Deleted pfp");
    }

}
