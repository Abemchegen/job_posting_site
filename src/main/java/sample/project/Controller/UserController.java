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
import sample.project.DTO.response.RegisterResponse;
import sample.project.DTO.response.ServiceResponse;
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
        ServiceResponse<String> res = userService.createUser(req);
        if (!res.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res.getMessage());
        }
        return ResponseEntity.ok().body(res.getMessage());
    }

    @PostMapping("/public/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req, HttpServletResponse response) {
        ServiceResponse<LoginResponse> res = userService.login(req);
        if (!res.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res.getMessage());
        }
        String cookie = "refreshtoken=" + res.getData().refresh_token() + "; Max-Age=86400; Path=/; HttpOnly; ";
        response.setHeader("Set-Cookie", cookie);
        LoginResponseUser resp = new LoginResponseUser(res.getData().access_token(), res.getData().response(),
                res.getData().statusDesc());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/public/verifyEmail")
    public ResponseEntity<?> verifyEmail(@RequestBody VerifyEmailRequest req,
            HttpServletResponse response) {
        ServiceResponse<RegisterResponse> res = userService.verifyEmail(req.code(), req.email());
        if (!res.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res.getMessage());
        }
        RegisterResponse resp = res.getData();
        String cookie = "jwt=" + resp.refreshToken()
                + "; Max-Age=86400; Path=/; HttpOnly; ";
        response.setHeader("Set-Cookie", cookie);

        return ResponseEntity.ok().body(resp.response());

    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@AuthenticationPrincipal User user, HttpServletResponse response) {
        ServiceResponse<LoginResponse> res = userService.refreshToken(user);
        if (!res.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res.getMessage());
        }
        String cookie = "refreshtoken=" + res.getData().refresh_token() + "; Max-Age=86400; Path=/; HttpOnly; ";
        response.setHeader("Set-Cookie", cookie);
        LoginResponseUser userres = new LoginResponseUser(res.getData().access_token(), res.getData().response(),
                res.getData().statusDesc());
        return ResponseEntity.ok().body(userres);
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
    public ResponseEntity<?> getUser(@PathVariable long userid, @AuthenticationPrincipal User user) {

        ServiceResponse<UserResponse> res = userService.getUser(userid);
        if (!res.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res.getMessage());
        }
        return ResponseEntity.ok().body(res.getData());
    }

    @GetMapping("/auth/me")
    public ResponseEntity<?> getMe(@AuthenticationPrincipal User user) {

        ServiceResponse<UserResponse> res = userService.getUser(user.getEmail());
        if (!res.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res.getMessage());
        }
        return ResponseEntity.ok().body(res.getData());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUsers(@RequestParam(required = false) String role,
            @RequestParam(required = false) String search,
            @AuthenticationPrincipal User currentUser) {

        ServiceResponse<UserResponseList> res = userService.getAllUser(role, search);
        if (!res.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res.getMessage());
        }
        return ResponseEntity.ok().body(res.getData());

    }

    @PutMapping("/{userid}")
    @PreAuthorize("hasAnyRole('AGENT', 'COMPANY', 'ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable long userid, @RequestBody RegisterRequest req,
            @AuthenticationPrincipal User user) {

        if (user.getId() != userid) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied");
        }

        ServiceResponse<UserResponse> res = userService.updateUser(req, userid);
        if (!res.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res.getMessage());
        }
        return ResponseEntity.ok().body(res.getData());
    }

    @PutMapping("updatePas/{userid}")
    @PreAuthorize("hasAnyRole('AGENT', 'COMPANY', 'ADMIN')")
    public ResponseEntity<String> updateUserPassword(@PathVariable long userid,
            @RequestBody ChangePasswordRequest req,
            @AuthenticationPrincipal User user) {

        if (user.getId() != userid) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied");
        }

        ServiceResponse<String> res = userService.updateUserPassword(req, userid);
        if (!res.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res.getMessage());
        }
        return ResponseEntity.ok().body(res.getMessage());
    }

    @DeleteMapping("/{userid}")
    @PreAuthorize("hasAnyRole('AGENT', 'COMPANY', 'ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable long userid, @AuthenticationPrincipal User user) {

        if (user.getId() != userid) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied");
        }

        ServiceResponse<String> res = userService.deleteUser(userid);
        if (!res.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res.getMessage());
        }

        return ResponseEntity.ok().body(res.getMessage());
    }

    @PostMapping("/uploadImage/{userid}")
    @PreAuthorize("hasAnyRole('AGENT', 'COMPANY', 'ADMIN')")
    public ResponseEntity<String> uploadProfileImage(@RequestParam("file") MultipartFile file,
            @PathVariable long userid,
            @AuthenticationPrincipal User user) {

        if (user.getId() != userid) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied");

        }

        ServiceResponse<String> pfpurl = userService.uploadProfileImage(userid, file);
        if (!pfpurl.isSuccess()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(pfpurl.getMessage());
        }
        return ResponseEntity.ok().body(pfpurl.getData());
    }

    @DeleteMapping("deletePfp/{userid}")
    @PreAuthorize("hasAnyRole('AGENT', 'COMPANY', 'ADMIN')")
    public ResponseEntity<String> deletePfp(@PathVariable long userid, @AuthenticationPrincipal User user) {

        if (user.getId() != userid) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied");

        }
        ServiceResponse<String> res = userService.deletePfp(userid);
        if (!res.isSuccess()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res.getMessage());
        }
        return ResponseEntity.ok().body(res.getMessage());
    }
}
