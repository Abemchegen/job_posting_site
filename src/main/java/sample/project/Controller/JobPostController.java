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
import sample.project.Model.Company;
import sample.project.Model.User;
import sample.project.Service.JobPostService;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @PostMapping("/create")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<JobpostResponse> createJobPost(@Valid @RequestBody JobPostRequest req,
            @AuthenticationPrincipal User currentUser) {

        Company company = currentUser.getCompany();
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
            @AuthenticationPrincipal User currentUser) {

        JobpostResponse existingjobPost = jobPostService.getJobPostById(jobpostID);
        Company company = currentUser.getCompany();

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
            @AuthenticationPrincipal User currentUser) {
        JobpostResponse existingjobPost = jobPostService.getJobPostById(jobpostID);
        Company company = currentUser.getCompany();

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
            @AuthenticationPrincipal User currentUser) {
        JobpostResponse existingjobPost = jobPostService.getJobPostById(jobpostID);
        Company company = currentUser.getCompany();

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
            @AuthenticationPrincipal User currentUser) {
        JobpostResponse existingjobPost = jobPostService.getJobPostById(jobPostID);
        Company company = currentUser.getCompany();

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
            @AuthenticationPrincipal User currentUser) {
        JobpostResponse existingjobPost = jobPostService.getJobPostById(jobPostID);
        Company company = currentUser.getCompany();

        boolean isSameCompany = company.getName().equals(existingjobPost.getCompanyName());
        if (!isSameCompany) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        JobApplicationResponse jobApplications = jobPostService.updateJobApplication(jobPostID, jobApplicationID, req);

        return ResponseEntity.ok().body(jobApplications);

    }

    @GetMapping("/company")
    public ResponseEntity<List<JobpostResponse>> getAllJobPostsCompany(@AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) Integer salaryMin,
            @RequestParam(required = false) Integer salaryMax,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort) {

        List<JobpostResponse> jobPosts = jobPostService.getAllJobPostsCompany(currentUser.getCompany().getName(),
                salaryMin, salaryMax, date, sort, search);
        return ResponseEntity.ok().body(jobPosts);

    }

    @GetMapping("/{id}")
    public ResponseEntity<JobpostResponse> getJobPostById(@PathVariable Long id) {
        JobpostResponse jobPost = jobPostService.getJobPostById(id);

        return ResponseEntity.ok().body(jobPost);

    }

}
