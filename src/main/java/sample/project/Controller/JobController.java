package sample.project.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import sample.project.DTO.request.CreateJobRequest;
import sample.project.DTO.request.SubCatagoriesRequest;
import sample.project.DTO.request.UpdateJobRequest;
import sample.project.Model.Job;
import sample.project.Service.JobService;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("job")
@RequiredArgsConstructor
public class JobController {

    private final JobService adminSerice;

    @PostMapping("/addJob")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Job> addJob(@Valid @RequestBody CreateJobRequest request) {
        Job job = adminSerice.addJob(request);
        return ResponseEntity.ok().body(job);
    }

    @PostMapping("/addSubcatagories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Job> addSubCatagories(@Valid @RequestBody SubCatagoriesRequest request) {
        Job job = adminSerice.addSubCatagories(request);
        return ResponseEntity.ok().body(job);
    }

    @PostMapping("/removeSubcatagories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Job> removeSubCatagories(@RequestBody SubCatagoriesRequest request) {
        Job job = adminSerice.removeSubCatagories(request);
        return ResponseEntity.ok().body(job);
    }

    @PostMapping("/updateJobDetails")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Job> updateJobDetails(@RequestBody UpdateJobRequest request) {
        Job job = adminSerice.updateJobDetails(request);
        return ResponseEntity.ok().body(job);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Job> getJobById(@RequestParam Long id) {
        Job job = adminSerice.getJobById(id);
        return ResponseEntity.ok().body(job);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Job>> getAllJob() {
        List<Job> jobs = adminSerice.getAllJob();
        return ResponseEntity.ok().body(jobs);
    }

}
