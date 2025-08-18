package sample.project.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import sample.project.DTO.response.AgentJobpostResponse;
import sample.project.DTO.response.JobApplicationResponse;
import sample.project.Model.User;
import sample.project.Service.JobApplicationService;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/jobApplication")
@RequiredArgsConstructor
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;

    @GetMapping("/jobpost")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<List<AgentJobpostResponse>> getJobposts(
            @RequestParam(required = false) Integer salaryMin,
            @RequestParam(required = false) Integer salaryMax,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String applied,
            @AuthenticationPrincipal User currentUser) {
        List<AgentJobpostResponse> posts = jobApplicationService.getJobposts(salaryMin, salaryMax, date, sort, search,
                applied, currentUser.getId());
        return ResponseEntity.ok().body(posts);
    }

    @GetMapping("/jobpost/{id}")
    @PreAuthorize("hasRole('AGENT')")

    public ResponseEntity<AgentJobpostResponse> getJobPostById(@PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        AgentJobpostResponse jobPost = jobApplicationService.getJobPostById(id, currentUser.getId());

        return ResponseEntity.ok().body(jobPost);

    }

    @PostMapping("/{jobPostID}/apply")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<JobApplicationResponse> apply(@RequestParam("coverLetter") String coverLetter,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @PathVariable long jobPostID,
            @AuthenticationPrincipal User currentUser) {

        JobApplicationResponse application = jobApplicationService.apply(coverLetter, file, currentUser.getId(),
                jobPostID);

        return ResponseEntity.ok().body(application);
    }

    @DeleteMapping("/{applicationid}")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<String> delete(@PathVariable long applicationid,
            @AuthenticationPrincipal User currentUser) {

        JobApplicationResponse application = jobApplicationService.findById(applicationid);

        if (application.getUserInfo().getId() != currentUser.getId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");

        }

        jobApplicationService.delete(applicationid);

        return ResponseEntity.ok().body("Job application successfully deleted.");
    }

    @PostMapping("/update/{applicationid}")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<JobApplicationResponse> update(@RequestPart("coverLetter") String coverLetter,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @PathVariable long applicationid,
            @AuthenticationPrincipal User currentUser) {

        JobApplicationResponse existingapplication = jobApplicationService.findById(applicationid);

        if (existingapplication.getUserInfo().getId() != currentUser.getId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");

        }

        JobApplicationResponse application = jobApplicationService.update(applicationid, coverLetter, file);

        return ResponseEntity.ok().body(application);
    }

    @GetMapping
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<List<JobApplicationResponse>> getJobApplications(
            @RequestParam(required = false) Integer salaryMin,
            @RequestParam(required = false) Integer salaryMax,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sort,
            @AuthenticationPrincipal User currentUser) {

        List<JobApplicationResponse> application = jobApplicationService.findAllApplications(salaryMin, salaryMax, date,
                sort, search, status,
                currentUser.getId());

        return ResponseEntity.ok().body(application);
    }

    @GetMapping("/{applicationID}")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<JobApplicationResponse> getAJobApplicationById(@PathVariable long applicationID,
            @AuthenticationPrincipal User currentUser) {

        JobApplicationResponse application = jobApplicationService.findById(applicationID);

        if (application.getUserInfo().getId() != currentUser.getId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        return ResponseEntity.ok().body(application);
    }
}
