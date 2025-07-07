package sample.project.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import sample.project.DTO.request.JobApplicationRequest;
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
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("jobApplication")
@RequiredArgsConstructor
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;

    @PostMapping("/{jobPostID}/apply")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<JobApplicationResponse> apply(@Valid @RequestBody JobApplicationRequest req,
            @PathVariable long jobPostID,
            @AuthenticationPrincipal User currentUser) {

        JobApplicationResponse application = jobApplicationService.apply(req, currentUser.getId(), jobPostID);

        return ResponseEntity.ok().body(application);
    }

    @DeleteMapping("/{applicationid}")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<String> delete(@PathVariable long applicationid,
            @AuthenticationPrincipal User currentUser) {

        JobApplicationResponse application = jobApplicationService.findById(applicationid);

        if (application.userInfo().getId() != currentUser.getId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");

        }

        jobApplicationService.delete(applicationid);

        return ResponseEntity.ok().body("Job application successfully deleted.");
    }

    @PostMapping("/update/{applicationid}")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<JobApplicationResponse> update(@RequestBody JobApplicationRequest req,
            @PathVariable long applicationid,
            @AuthenticationPrincipal User currentUser) {

        JobApplicationResponse existingapplication = jobApplicationService.findById(applicationid);

        if (existingapplication.userInfo().getId() != currentUser.getId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");

        }

        JobApplicationResponse application = jobApplicationService.update(applicationid, req);

        return ResponseEntity.ok().body(application);
    }

    @GetMapping
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<List<JobApplicationResponse>> getJobApplications(
            @AuthenticationPrincipal User currentUser) {

        List<JobApplicationResponse> application = jobApplicationService.findAllApplications(currentUser.getId());

        return ResponseEntity.ok().body(application);
    }

    @GetMapping("/{applicationID}")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<JobApplicationResponse> getAJobApplicationById(@PathVariable long applicationID,
            @AuthenticationPrincipal User currentUser) {

        JobApplicationResponse application = jobApplicationService.findById(applicationID);

        if (application.userInfo().getId() != currentUser.getId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        return ResponseEntity.ok().body(application);
    }
}
