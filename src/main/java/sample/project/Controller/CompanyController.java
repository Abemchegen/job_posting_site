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

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/company")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @PutMapping("/updateDetails")
    @PreAuthorize("hasRole('COMPANY')")

    public ResponseEntity<?> changeCompanyDetails(@RequestBody CompanyUpdateRequest req,
            @AuthenticationPrincipal User currentUser) {

        ServiceResponse<CompanyUpdateResponse> response = companyService.changeCompanyDetails(req,
                currentUser.getCompany().getId());

        if (!response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response.getData());
        }
        return ResponseEntity.ok().body(response.getData());
    }

    @GetMapping("getJobPosts")
    public ResponseEntity<?> getAllJobPostsFromACompany(@AuthenticationPrincipal User currentUser) {

        ServiceResponse<List<JobpostResponse>> response = companyService
                .getAllJobPostsFromACompany(currentUser.getCompany().getId());

        if (!response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response.getData());
        }

        return ResponseEntity.ok().body(response.getData());

    }

}
