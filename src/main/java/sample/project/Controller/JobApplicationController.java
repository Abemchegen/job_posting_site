package sample.project.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import sample.project.DTO.response.AgentJobpostResponse;
import sample.project.DTO.response.JobApplicationResponse;
import sample.project.DTO.response.ServiceResponse;
import sample.project.Model.User;
import sample.project.Service.JobApplicationService;
import sample.project.Service.UserService;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/jobApplication")
@RequiredArgsConstructor
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;
    private final UserService userService;

    @GetMapping("/jobpost")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<?> getJobposts(
            @RequestParam(required = false) Integer salaryMin,
            @RequestParam(required = false) Integer salaryMax,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String applied,
            @AuthenticationPrincipal Jwt jwt) {

        Optional<User> user = userService.getUserByEmail(jwt.getClaim("email"));

        if (!user.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        ServiceResponse<List<AgentJobpostResponse>> posts = jobApplicationService.getJobposts(salaryMin, salaryMax,
                date, sort, search,
                applied, user.get().getId());

        if (!posts.isSuccess()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(posts.getMessage());
        }
        return ResponseEntity.ok().body(posts.getData());
    }

    @GetMapping("/jobpost/{id}")
    @PreAuthorize("hasRole('AGENT')")

    public ResponseEntity<?> getJobPostById(@PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        Optional<User> user = userService.getUserByEmail(jwt.getClaim("email"));

        if (!user.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        ServiceResponse<AgentJobpostResponse> jobPost = jobApplicationService.getJobPostById(id, user.get().getId());
        if (!jobPost.isSuccess()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(jobPost.getMessage());
        }

        return ResponseEntity.ok().body(jobPost.getData());

    }

    @PostMapping("/{jobPostID}/apply")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<?> apply(@RequestParam("coverLetter") String coverLetter,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @PathVariable long jobPostID,
            @AuthenticationPrincipal Jwt jwt) {

        Optional<User> user = userService.getUserByEmail(jwt.getClaim("email"));
        if (!user.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        ServiceResponse<JobApplicationResponse> application = jobApplicationService.apply(coverLetter, file,
                user.get().getId(),
                jobPostID);

        if (!application.isSuccess()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(application.getMessage());
        }
        return ResponseEntity.ok().body(application.getData());
    }

    @DeleteMapping("/{applicationid}")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<String> delete(@PathVariable long applicationid,
            @AuthenticationPrincipal Jwt jwt) {

        Optional<User> user = userService.getUserByEmail(jwt.getClaim("email"));

        if (!user.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        ServiceResponse<JobApplicationResponse> application = jobApplicationService.findById(applicationid);

        if (!application.isSuccess()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Job application not found");
        }
        if (application.getData().getUserInfo().getId() != user.get().getId()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied");
        }
        jobApplicationService.delete(applicationid);

        return ResponseEntity.ok().body("Job application successfully deleted.");
    }

    @PostMapping("/update/{applicationid}")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<?> update(@RequestPart("coverLetter") String coverLetter,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @PathVariable long applicationid,
            @AuthenticationPrincipal Jwt jwt) {

        Optional<User> user = userService.getUserByEmail(jwt.getClaim("email"));

        if (!user.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        ServiceResponse<JobApplicationResponse> existingapplication = jobApplicationService.findById(applicationid);

        if (!existingapplication.isSuccess()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Job application not found");
        }

        if (existingapplication.getData().getUserInfo().getId() != user.get().getId()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied");
        }

        ServiceResponse<JobApplicationResponse> application = jobApplicationService.update(applicationid, coverLetter,
                file);
        if (!application.isSuccess()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(application.getMessage());
        }
        return ResponseEntity.ok().body(application.getData());
    }

    @GetMapping
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<?> getJobApplications(
            @RequestParam(required = false) Integer salaryMin,
            @RequestParam(required = false) Integer salaryMax,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sort,
            @AuthenticationPrincipal Jwt jwt) {

        Optional<User> user = userService.getUserByEmail(jwt.getClaim("email"));
        if (!user.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        List<JobApplicationResponse> application = jobApplicationService.findAllApplications(salaryMin, salaryMax, date,
                sort, search, status,
                user.get().getId());

        return ResponseEntity.ok().body(application);
    }

    @GetMapping("/{applicationID}")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<?> getAJobApplicationById(@PathVariable long applicationID,
            @AuthenticationPrincipal Jwt jwt) {

        Optional<User> user = userService.getUserByEmail(jwt.getClaim("email"));
        if (!user.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        ServiceResponse<JobApplicationResponse> application = jobApplicationService.findById(applicationID);
        if (!application.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(application.getMessage());
        }
        if (application.getData().getUserInfo().getId() != user.get().getId()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied");
        }
        return ResponseEntity.ok().body(application.getData());
    }
}
