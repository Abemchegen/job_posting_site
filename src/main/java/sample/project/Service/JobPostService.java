package sample.project.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import sample.project.DTO.request.JobPostRequest;
import sample.project.DTO.request.UpdateJobPost;
import sample.project.ErrorHandling.Exception.ObjectNotFound;
import sample.project.Model.Company;
import sample.project.Model.Job;
import sample.project.Model.JobApplication;
import sample.project.Model.JobPost;
import sample.project.Repo.JobPostRepo;

@Service
@RequiredArgsConstructor
public class JobPostService {

    private final JobPostRepo jobPostRepo;
    private final CompanyService companyService;
    private final JobService jobService;

    public JobPost postJob(JobPostRequest req) {
        Optional<Company> optionalCompany = companyService.getCompany(req.companyName());
        if (!optionalCompany.isPresent()) {
            throw new ObjectNotFound("Company", "name");
        }

        Optional<Job> optionalJob = jobService.getJob(req.jobName());
        if (!optionalJob.isPresent()) {
            throw new ObjectNotFound("Job", "name");
        }

        JobPost jobPost = new JobPost();
        jobPost.setDescription(req.description());
        jobPost.setPeopleNeeded(req.peopleNeeded());
        jobPost.setSalary(req.salary());
        jobPost.setJob(optionalJob.get());
        jobPost.setCompany(optionalCompany.get());

        return jobPostRepo.save(jobPost);

    }

    @Transactional
    public JobPost updateJobPost(UpdateJobPost req, Long jobPostID) {
        Optional<JobPost> optionalJobPost = jobPostRepo.findById(jobPostID);
        if (!optionalJobPost.isPresent()) {
            throw new ObjectNotFound("Job post", "id");
        }

        Optional<Job> optionalJob = jobService.getJob(req.jobName());
        if (!optionalJob.isPresent()) {
            throw new ObjectNotFound("Job post", "name");
        }

        JobPost jobPost = optionalJobPost.get();
        jobPost.setJob(optionalJob.get());
        if (req.description() != null) {
            jobPost.setDescription(req.description());

        }
        if (req.peopleNeeded() != null) {
            jobPost.setPeopleNeeded(req.peopleNeeded());

        }
        if (req.salary() != null) {
            jobPost.setSalary(req.salary());

        }

        return jobPost;

    }

    public void deleteJobPost(Long id) {
        Optional<JobPost> optionalJobPost = jobPostRepo.findById(id);
        if (!optionalJobPost.isPresent()) {
            throw new ObjectNotFound("Job post", "id");
        }

        jobPostRepo.deleteById(id);
    }

    public List<JobPost> getAllJobPosts() {
        return jobPostRepo.findAll();
    }

    public JobPost getJobPostById(Long id) {
        Optional<JobPost> optionalJobPost = jobPostRepo.findById(id);
        if (!optionalJobPost.isPresent()) {
            throw new ObjectNotFound("Job post", "id");
        }

        return optionalJobPost.get();
    }

    public List<JobApplication> getJobApplications(Long id) {

        Optional<JobPost> optionalJobPost = jobPostRepo.findById(id);
        if (!optionalJobPost.isPresent()) {
            throw new ObjectNotFound("Job post", "id");
        }

        return optionalJobPost.get().getJobApplications();

    }

}
