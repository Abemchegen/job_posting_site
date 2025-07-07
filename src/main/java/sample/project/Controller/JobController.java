package sample.project.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import sample.project.DTO.request.CreateJobRequest;
import sample.project.DTO.request.SubCatagoriesRequest;
import sample.project.DTO.request.SubCatagoryRequest;
import sample.project.DTO.request.UpdateJobRequest;
import sample.project.Model.Job;
import sample.project.Service.JobService;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("job")
@RequiredArgsConstructor
public class JobController {

    private final JobService adminService;

    @PostMapping("/addJob")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Job> addJob(@Valid @RequestBody CreateJobRequest request) {
        Job job = adminService.addJob(request);
        return ResponseEntity.ok().body(job);
    }

    @PostMapping("/addSubcatagories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Job> addSubCatagories(@Valid @RequestBody SubCatagoriesRequest request) {
        Job job = adminService.addSubCatagories(request);
        return ResponseEntity.ok().body(job);
    }

    @PostMapping("/removeSubcatagories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Job> removeSubCatagories(@Valid @RequestBody SubCatagoriesRequest request) {
        Job job = adminService.removeSubCatagories(request);
        return ResponseEntity.ok().body(job);
    }

    @PostMapping("/updateSubcatagories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Job> updateSubCatagories(@Valid @RequestBody SubCatagoriesRequest request) {
        Job job = adminService.updateSubCatagories(request);
        return ResponseEntity.ok().body(job);
    }

    @PostMapping("/updateSubcatagory")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Job> updateSubCatagory(@Valid @RequestBody SubCatagoryRequest request) {
        Job job = adminService.updateSubCatagory(request);
        return ResponseEntity.ok().body(job);
    }

    @PostMapping("/updateJobDetails")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Job> updateJobDetails(@RequestBody UpdateJobRequest request) {
        Job job = adminService.updateJobDetails(request);
        return ResponseEntity.ok().body(job);
    }

    @GetMapping("/{jobid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Job> getJobById(@PathVariable Long jobid) {
        Job job = adminService.getJobById(jobid);
        return ResponseEntity.ok().body(job);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Job>> getAllJob() {
        List<Job> jobs = adminService.getAllJob();
        return ResponseEntity.ok().body(jobs);
    }

    @DeleteMapping("/{jobid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteJobById(@PathVariable Long jobid) {
        adminService.deleteJobById(jobid);
        return ResponseEntity.ok().body("Job deleted successfully.");
    }

}
