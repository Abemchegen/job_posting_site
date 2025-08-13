package sample.project.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import sample.project.DTO.request.CompanyUpdateRequest;
import sample.project.DTO.response.CompanyUpdateResponse;
import sample.project.DTO.response.JobpostResponse;
import sample.project.Model.User;
import sample.project.Service.CompanyService;

import java.util.List;

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
    public ResponseEntity<CompanyUpdateResponse> changeCompanyDetails(@RequestBody CompanyUpdateRequest req,
            @AuthenticationPrincipal User currentUser) {
        CompanyUpdateResponse response = companyService.changeCompanyDetails(req, currentUser.getCompany().getId());
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("getJobPosts")
    public ResponseEntity<List<JobpostResponse>> getAllJobPostsFromACompany(@AuthenticationPrincipal User currentUser) {
        List<JobpostResponse> jobPosts = companyService.getAllJobPostsFromACompany(currentUser.getCompany().getId());

        return ResponseEntity.ok().body(jobPosts);

    }

}
