package sample.project.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import sample.project.DTO.request.CompanyUpdateRequest;
import sample.project.DTO.response.CompanyUpdateResponse;
import sample.project.DTO.response.JobpostResponse;
import sample.project.DTO.response.ServiceResponse;
import sample.project.Model.User;
import sample.project.Service.CompanyService;
import sample.project.Service.UserService;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/company")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;
    private final UserService userService;

    @PutMapping("/updateDetails")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<?> changeCompanyDetails(@RequestBody CompanyUpdateRequest req,
            @AuthenticationPrincipal Jwt jwt) {

        Optional<User> user = userService.getUserByEmail(jwt.getClaim("email"));

        if (!user.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        ServiceResponse<CompanyUpdateResponse> response = companyService.changeCompanyDetails(req,
                user.get().getCompany().getId());

        if (!response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response.getData());
        }
        return ResponseEntity.ok().body(response.getData());
    }

    @GetMapping("getJobPosts")
    public ResponseEntity<?> getAllJobPostsFromACompany(@AuthenticationPrincipal Jwt jwt) {
        Optional<User> user = userService.getUserByEmail(jwt.getClaim("email"));

        if (!user.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        ServiceResponse<List<JobpostResponse>> response = companyService
                .getAllJobPostsFromACompany(user.get().getCompany().getId());

        if (!response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response.getData());
        }

        return ResponseEntity.ok().body(response.getData());

    }

}
