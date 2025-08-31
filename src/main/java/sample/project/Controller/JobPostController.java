package sample.project.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import sample.project.DTO.request.JobApplicationUpdateRequest;
import sample.project.DTO.request.JobPostRequest;
import sample.project.DTO.request.UpdateJobPost;
import sample.project.DTO.response.JobApplicationResponse;
import sample.project.DTO.response.JobpostResponse;
import sample.project.ErrorHandling.Exception.AccessDenied;
import sample.project.ErrorHandling.Exception.ObjectNotFound;
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
    public ResponseEntity<JobpostResponse> createJobPost(@Valid @RequestBody JobPostRequest req,
            @AuthenticationPrincipal Jwt jwt) {

        Optional<User> user = userService.getUserByEmail(jwt.getClaim("email"));
        if (!user.isPresent()) {
            throw new AccessDenied();
        }

        Company company = user.get().getCompany();
        if (!company.getName().equals(req.getCompanyName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Access denied , you can only create post for your company");
        }
        JobpostResponse jobPost = jobPostService.postJob(req);

        return ResponseEntity.ok().body(jobPost);
    }

    @PostMapping("/update/{jobpostID}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<JobpostResponse> updateJobPost(@RequestBody UpdateJobPost req, @PathVariable Long jobpostID,
            @AuthenticationPrincipal Jwt jwt) {

        Optional<User> user = userService.getUserByEmail(jwt.getClaim("email"));

        if (!user.isPresent()) {
            throw new ObjectNotFound("user", "email");
        }

        JobpostResponse existingjobPost = jobPostService.getJobPostById(jobpostID);
        Company company = user.get().getCompany();

        boolean isSameCompany = company.getName().equals(existingjobPost.getCompanyName());
        if (!isSameCompany) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        JobpostResponse jobPost = jobPostService.updateJobPost(req, jobpostID);

        return ResponseEntity.ok().body(jobPost);
    }

    @DeleteMapping("/{jobpostID}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<String> deleteJobPost(@PathVariable Long jobpostID,
            @AuthenticationPrincipal Jwt jwt) {

        Optional<User> user = userService.getUserByEmail(jwt.getClaim("email"));

        if (!user.isPresent()) {
            throw new ObjectNotFound("user", "email");
        }

        JobpostResponse existingjobPost = jobPostService.getJobPostById(jobpostID);
        Company company = user.get().getCompany();

        boolean isSameCompany = company.getName().equals(existingjobPost.getCompanyName());
        if (!isSameCompany) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        jobPostService.deleteJobPost(jobpostID);

        return ResponseEntity.ok().body("Post deleted Successfully.");
    }

    @GetMapping("/{jobpostID}/jobApplication")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<List<JobApplicationResponse>> getAllJobApplications(@PathVariable Long jobpostID,
            @AuthenticationPrincipal Jwt jwt) {

        Optional<User> user = userService.getUserByEmail(jwt.getClaim("email"));

        if (!user.isPresent()) {
            throw new ObjectNotFound("user", "email");
        }

        JobpostResponse existingjobPost = jobPostService.getJobPostById(jobpostID);
        Company company = user.get().getCompany();

        boolean isSameCompany = company.getName().equals(existingjobPost.getCompanyName());
        if (!isSameCompany) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        List<JobApplicationResponse> jobApplications = jobPostService.getJobApplications(jobpostID);

        return ResponseEntity.ok().body(jobApplications);

    }

    @GetMapping("/{jobPostID}/jobApplication/{jobApplicationID}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<JobApplicationResponse> getJobApplication(@PathVariable Long jobPostID,
            @PathVariable Long jobApplicationID,
            @AuthenticationPrincipal Jwt jwt) {

        Optional<User> user = userService.getUserByEmail(jwt.getClaim("email"));

        if (!user.isPresent()) {
            throw new ObjectNotFound("user", "email");
        }

        JobpostResponse existingjobPost = jobPostService.getJobPostById(jobPostID);
        Company company = user.get().getCompany();

        boolean isSameCompany = company.getName().equals(existingjobPost.getCompanyName());
        if (!isSameCompany) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        JobApplicationResponse jobApplications = jobPostService.getJobApplication(jobPostID, jobApplicationID);

        return ResponseEntity.ok().body(jobApplications);

    }

    @PutMapping("/{jobPostID}/jobApplication/{jobApplicationID}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<JobApplicationResponse> updateJobApplicationStatus(@PathVariable Long jobPostID,
            @PathVariable Long jobApplicationID,
            @RequestBody JobApplicationUpdateRequest req,
            @AuthenticationPrincipal Jwt jwt) {

        Optional<User> user = userService.getUserByEmail(jwt.getClaim("email"));

        if (!user.isPresent()) {
            throw new ObjectNotFound("user", "email");
        }

        JobpostResponse existingjobPost = jobPostService.getJobPostById(jobPostID);
        Company company = user.get().getCompany();

        boolean isSameCompany = company.getName().equals(existingjobPost.getCompanyName());
        if (!isSameCompany) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        JobApplicationResponse jobApplications = jobPostService.updateJobApplication(jobPostID, jobApplicationID, req);

        return ResponseEntity.ok().body(jobApplications);

    }

    @GetMapping("/company")
    public ResponseEntity<List<JobpostResponse>> getAllJobPostsCompany(@AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) Integer salaryMin,
            @RequestParam(required = false) Integer salaryMax,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort) {

        Optional<User> user = userService.getUserByEmail(jwt.getClaim("email"));

        if (!user.isPresent()) {
            throw new ObjectNotFound("user", "email");
        }

        List<JobpostResponse> jobPosts = jobPostService.getAllJobPostsCompany(user.get().getCompany().getName(),
                salaryMin, salaryMax, date, sort, search);

        return ResponseEntity.ok().body(jobPosts);

    }

    @GetMapping("/{id}")
    public ResponseEntity<JobpostResponse> getJobPostById(@PathVariable Long id) {
        JobpostResponse jobPost = jobPostService.getJobPostById(id);

        return ResponseEntity.ok().body(jobPost);

    }

}
