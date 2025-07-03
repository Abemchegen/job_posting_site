package sample.project.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import sample.project.DTO.request.JobApplicationRequest;
import sample.project.Model.JobApplication;
import sample.project.Model.User;
import sample.project.Service.JobApplicationService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("jobApplication")
@RequiredArgsConstructor
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;

    @PostMapping("/apply")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<JobApplication> apply(@Valid @RequestBody JobApplicationRequest req,
            @AuthenticationPrincipal User currentUser) {

        JobApplication application = jobApplicationService.apply(req, currentUser.getId());

        return ResponseEntity.ok().body(application);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<String> delete(@PathVariable Long applicationid,
            @AuthenticationPrincipal User currentUser) {

        JobApplication application = jobApplicationService.findById(applicationid);

        if (!application.getAgent().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");

        }

        jobApplicationService.delete(applicationid);

        return ResponseEntity.ok().body("Job application successfully deleted.");
    }

    @PostMapping("/update/{id}")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<JobApplication> update(@RequestBody JobApplicationRequest req,
            @PathVariable Long applicationID,
            @AuthenticationPrincipal User currentUser) {

        JobApplication existingapplication = jobApplicationService.findById(applicationID);

        if (!existingapplication.getAgent().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");

        }

        JobApplication application = jobApplicationService.update(currentUser.getId(), req);

        return ResponseEntity.ok().body(application);
    }
}
