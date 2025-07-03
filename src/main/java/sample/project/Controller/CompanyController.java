package sample.project.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import sample.project.DTO.request.CompanyUpdateRequest;
import sample.project.DTO.response.CompanyUpdateResponse;
import sample.project.Model.Company;
import sample.project.Model.User;
import sample.project.Service.CompanyService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("users/company")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping("/updateDetails")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<CompanyUpdateResponse> changeCompanyDetails(@RequestBody CompanyUpdateRequest req,
            @AuthenticationPrincipal User currentUser) {

        Company company = currentUser.getCompany();

        if (!company.getId().equals(req.companyID())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        CompanyUpdateResponse response = companyService.changeCompanyDetails(req);
        return ResponseEntity.ok().body(response);
    }

}
