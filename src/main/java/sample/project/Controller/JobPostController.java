package sample.project.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import sample.project.DTO.request.JobApplicationUpdateRequest;
import sample.project.DTO.request.JobPostRequest;
import sample.project.DTO.request.UpdateJobPost;
import sample.project.DTO.response.JobApplicationResponse;
import sample.project.DTO.response.JobpostResponse;
import sample.project.DTO.response.ServiceResponse;
import sample.project.Model.Company;
import sample.project.Model.User;
import sample.project.Service.JobPostService;
import sample.project.Service.UserService;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/jobpost")
@RequiredArgsConstructor
public class JobPostController {

    private final JobPostService jobPostService;
    private final UserService userService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<?> createJobPost(@Valid @RequestBody JobPostRequest req,
            @AuthenticationPrincipal Jwt jwt) {

        Optional<User> user = userService.getUserByEmail(jwt.getClaim("email"));
        if (!user.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        Company company = user.get().getCompany();
        if (!company.getName().equals(req.getCompanyName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied , you can only create post for your company");
        }
        ServiceResponse<JobpostResponse> jobPost = jobPostService.postJob(req);
        if (!jobPost.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(jobPost.getMessage());
        }
        return ResponseEntity.ok().body(jobPost.getData());
    }

    @PostMapping("/update/{jobpostID}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<?> updateJobPost(@RequestBody UpdateJobPost req, @PathVariable Long jobpostID,
            @AuthenticationPrincipal Jwt jwt) {

        Optional<User> user = userService.getUserByEmail(jwt.getClaim("email"));

        if (!user.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        ServiceResponse<JobpostResponse> existingjobPost = jobPostService.getJobPostById(jobpostID);
        if (!existingjobPost.isSuccess()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(existingjobPost.getMessage());
        }
        Company company = user.get().getCompany();

        boolean isSameCompany = company.getName().equals(existingjobPost.getData().getCompanyName());
        if (!isSameCompany) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied");
        }

        ServiceResponse<JobpostResponse> jobPost = jobPostService.updateJobPost(req, jobpostID);
        if (!jobPost.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(jobPost.getMessage());
        }
        return ResponseEntity.ok().body(jobPost.getData());
    }

    @DeleteMapping("/{jobpostID}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<String> deleteJobPost(@PathVariable Long jobpostID,
            @AuthenticationPrincipal Jwt jwt) {

        Optional<User> user = userService.getUserByEmail(jwt.getClaim("email"));

        if (!user.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        ServiceResponse<JobpostResponse> existingjobPost = jobPostService.getJobPostById(jobpostID);
        if (!existingjobPost.isSuccess()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(existingjobPost.getMessage());
        }
        Company company = user.get().getCompany();

        boolean isSameCompany = company.getName().equals(existingjobPost.getData().getCompanyName());
        if (!isSameCompany) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied");
        }
        jobPostService.deleteJobPost(jobpostID);

        return ResponseEntity.ok().body("Post deleted Successfully.");
    }

    @GetMapping("/{jobpostID}/jobApplication")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<?> getAllJobApplications(@PathVariable Long jobpostID,
            @AuthenticationPrincipal Jwt jwt) {

        Optional<User> user = userService.getUserByEmail(jwt.getClaim("email"));

        if (!user.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        ServiceResponse<JobpostResponse> existingjobPost = jobPostService.getJobPostById(jobpostID);
        if (!existingjobPost.isSuccess()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(existingjobPost.getMessage());
        }
        Company company = user.get().getCompany();

        boolean isSameCompany = company.getName().equals(existingjobPost.getData().getCompanyName());
        if (!isSameCompany) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied");
        }
        ServiceResponse<List<JobApplicationResponse>> jobApplications = jobPostService.getJobApplications(jobpostID);
        if (!jobApplications.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(jobApplications.getMessage());
        }

        return ResponseEntity.ok().body(jobApplications.getData());

    }

    @GetMapping("/{jobPostID}/jobApplication/{jobApplicationID}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<?> getJobApplication(@PathVariable Long jobPostID,
            @PathVariable Long jobApplicationID,
            @AuthenticationPrincipal Jwt jwt) {

        Optional<User> user = userService.getUserByEmail(jwt.getClaim("email"));

        if (!user.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        ServiceResponse<JobpostResponse> existingjobPost = jobPostService.getJobPostById(jobPostID);
        if (!existingjobPost.isSuccess()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(existingjobPost.getMessage());
        }
        Company company = user.get().getCompany();

        boolean isSameCompany = company.getName().equals(existingjobPost.getData().getCompanyName());
        if (!isSameCompany) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied");
        }
        ServiceResponse<JobApplicationResponse> jobApplications = jobPostService.getJobApplication(jobPostID,
                jobApplicationID);

        if (!jobApplications.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(jobApplications.getMessage());
        }

        return ResponseEntity.ok().body(jobApplications.getData());

    }

    @PutMapping("/{jobPostID}/jobApplication/{jobApplicationID}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<?> updateJobApplicationStatus(@PathVariable Long jobPostID,
            @PathVariable Long jobApplicationID,
            @RequestBody JobApplicationUpdateRequest req,
            @AuthenticationPrincipal Jwt jwt) {

        Optional<User> user = userService.getUserByEmail(jwt.getClaim("email"));

        if (!user.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        ServiceResponse<JobpostResponse> existingjobPost = jobPostService.getJobPostById(jobPostID);
        if (!existingjobPost.isSuccess()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(existingjobPost.getMessage());
        }
        Company company = user.get().getCompany();

        boolean isSameCompany = company.getName().equals(existingjobPost.getData().getCompanyName());
        if (!isSameCompany) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied");
        }
        ServiceResponse<JobApplicationResponse> jobApplications = jobPostService.updateJobApplication(jobPostID,
                jobApplicationID, req);
        if (!jobApplications.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(jobApplications.getMessage());
        }
        return ResponseEntity.ok().body(jobApplications.getData());

    }

    @GetMapping("/company")
    public ResponseEntity<?> getAllJobPostsCompany(@AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) Integer salaryMin,
            @RequestParam(required = false) Integer salaryMax,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort) {

        Optional<User> user = userService.getUserByEmail(jwt.getClaim("email"));

        if (!user.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        ServiceResponse<List<JobpostResponse>> jobPosts = jobPostService.getAllJobPostsCompany(
                user.get().getCompany().getName(),
                salaryMin, salaryMax, date, sort, search);

        if (!jobPosts.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(jobPosts.getMessage());
        }
        return ResponseEntity.ok().body(jobPosts.getData());

    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getJobPostById(@PathVariable Long id) {
        ServiceResponse<JobpostResponse> jobPost = jobPostService.getJobPostById(id);
        if (!jobPost.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(jobPost.getMessage());
        }
        return ResponseEntity.ok().body(jobPost.getData());

    }

}
