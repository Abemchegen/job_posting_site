package sample.project.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import sample.project.DTO.request.CreateJobRequest;
import sample.project.DTO.request.SubCatagoriesRequest;
import sample.project.DTO.request.UpdateSubCatagoryRequest;
import sample.project.DTO.response.ServiceResponse;
import sample.project.DTO.request.UpdateJobRequest;
import sample.project.DTO.request.UpdateSubCatagoriesRequest;
import sample.project.Model.Job;
import sample.project.Service.JobService;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/job")
@RequiredArgsConstructor
public class JobController {

    private final JobService adminService;

    @PostMapping("/addJob")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY')")
    public ResponseEntity<?> addJob(@Valid @RequestBody CreateJobRequest request) {
        ServiceResponse<Job> job = adminService.addJob(request);
        if (!job.isSuccess()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(job.getMessage());
        }
        return ResponseEntity.ok().body(job.getData());
    }

    @PostMapping("/addSubcatagories")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY')")
    public ResponseEntity<?> addSubCatagories(@Valid @RequestBody SubCatagoriesRequest request) {
        ServiceResponse<Job> job = adminService.addSubCatagories(request);
        if (!job.isSuccess()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(job.getMessage());
        }
        return ResponseEntity.ok().body(job.getData());
    }

    @PostMapping("/removeSubcatagories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> removeSubCatagories(@Valid @RequestBody SubCatagoriesRequest request) {
        ServiceResponse<Job> job = adminService.removeSubCatagories(request);
        if (!job.isSuccess()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(job.getMessage());
        }
        return ResponseEntity.ok().body(job.getData());
    }

    @PostMapping("/updateSubcatagories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateSubCatagories(@Valid @RequestBody UpdateSubCatagoriesRequest request) {
        ServiceResponse<Job> job = adminService.updateSubCatagories(request);
        if (!job.isSuccess()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(job.getMessage());
        }
        return ResponseEntity.ok().body(job.getData());
    }

    @PostMapping("/updateSubcatagory")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateSubCatagory(@Valid @RequestBody UpdateSubCatagoryRequest request) {
        ServiceResponse<Job> job = adminService.updateSubCatagory(request);
        if (!job.isSuccess()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(job.getMessage());
        }
        return ResponseEntity.ok().body(job.getData());
    }

    @PostMapping("/updateJobDetails")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateJobDetails(@RequestBody UpdateJobRequest request) {
        ServiceResponse<Job> job = adminService.updateJobDetails(request);
        if (!job.isSuccess()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(job.getMessage());
        }
        return ResponseEntity.ok().body(job.getData());
    }

    @GetMapping("/{jobid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getJobById(@PathVariable Long jobid) {
        ServiceResponse<Job> job = adminService.getJobById(jobid);

        if (!job.isSuccess()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(job.getMessage());
        }
        return ResponseEntity.ok().body(job.getData());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('COMPANY', 'ADMIN')")
    public ResponseEntity<List<Job>> getAllJob(@RequestParam(required = false) String search) {
        List<Job> jobs = adminService.getAllJob(search);
        return ResponseEntity.ok().body(jobs);
    }

    @DeleteMapping("/{jobid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteJobById(@PathVariable Long jobid) {
        adminService.deleteJobById(jobid);
        return ResponseEntity.ok().body("Job deleted successfully.");
    }

}
