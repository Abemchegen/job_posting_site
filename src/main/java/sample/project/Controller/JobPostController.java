package sample.project.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import sample.project.DTO.request.JobPostRequest;
import sample.project.DTO.request.UpdateJobPost;
import sample.project.Model.Company;
import sample.project.Model.JobApplication;
import sample.project.Model.JobPost;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("jobpost")
@RequiredArgsConstructor
public class JobPostController {

    private final JobPostService jobPostService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<JobPost> createJobPost(@Valid @RequestBody JobPostRequest req,
            @AuthenticationPrincipal User currentUser) {

        Company company = currentUser.getCompany();
        if (!company.getName().equals(req.companyName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        JobPost jobPost = jobPostService.postJob(req);

        return ResponseEntity.ok().body(jobPost);
    }

    @PostMapping("/update/{id}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<JobPost> updateJobPost(@RequestBody UpdateJobPost req, @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {

        JobPost existingjobPost = jobPostService.getJobPostById(id);
        Company company = currentUser.getCompany();

        boolean isSameCompany = company.getName().equals(existingjobPost.getCompany().getName());
        if (!isSameCompany) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        JobPost jobPost = jobPostService.updateJobPost(req, id);

        return ResponseEntity.ok().body(jobPost);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<String> deleteJobPost(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        JobPost existingjobPost = jobPostService.getJobPostById(id);
        Company company = currentUser.getCompany();

        boolean isSameCompany = company.getName().equals(existingjobPost.getCompany().getName());
        if (!isSameCompany) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        jobPostService.deleteJobPost(id);

        return ResponseEntity.ok().body("Post deleted Successfully.");
    }

    @GetMapping("/jobAplications/{id}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<List<JobApplication>> getAllJobApplications(@PathVariable Long jobPostID,
            @AuthenticationPrincipal User currentUser) {
        JobPost existingjobPost = jobPostService.getJobPostById(jobPostID);
        Company company = currentUser.getCompany();

        boolean isSameCompany = company.getName().equals(existingjobPost.getCompany().getName());
        if (!isSameCompany) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        List<JobApplication> jobApplications = jobPostService.getJobApplications(jobPostID);

        return ResponseEntity.ok().body(jobApplications);

    }

    @GetMapping
    public ResponseEntity<List<JobPost>> getAllJobPosts() {
        List<JobPost> jobPosts = jobPostService.getAllJobPosts();

        return ResponseEntity.ok().body(jobPosts);

    }

    @GetMapping("/{id}")
    public ResponseEntity<JobPost> getJobPostsById(@PathVariable Long id) {
        JobPost jobPost = jobPostService.getJobPostById(id);

        return ResponseEntity.ok().body(jobPost);

    }

}
