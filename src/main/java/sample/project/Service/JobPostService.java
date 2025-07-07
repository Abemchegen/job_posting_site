package sample.project.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import sample.project.DTO.request.JobApplicationUpdateRequest;
import sample.project.DTO.request.JobPostRequest;
import sample.project.DTO.request.UpdateJobPost;
import sample.project.DTO.response.JobApplicationResponse;
import sample.project.DTO.response.JobpostResponse;
import sample.project.DTO.response.UserResponse;
import sample.project.ErrorHandling.Exception.ObjectNotFound;
import sample.project.ErrorHandling.Exception.RequiredFieldsEmpty;
import sample.project.Model.Agent;
import sample.project.Model.Company;
import sample.project.Model.Job;
import sample.project.Model.JobApplication;
import sample.project.Model.JobPost;
import sample.project.Model.Subcatagory;
import sample.project.Model.User;
import sample.project.Repo.JobPostRepo;

@Service
@RequiredArgsConstructor
public class JobPostService {

    private final JobPostRepo jobPostRepo;
    private final CompanyService companyService;
    private final JobService jobService;

    public JobpostResponse postJob(JobPostRequest req) {
        Optional<Company> optionalCompany = companyService.getCompany(req.getCompanyName());
        if (!optionalCompany.isPresent()) {
            throw new ObjectNotFound("Company", "name");
        }

        Optional<Job> optionalJob = jobService.getJob(req.getJobName());
        if (!optionalJob.isPresent()) {
            throw new ObjectNotFound("Job", "name");
        }
        Job job = optionalJob.get();
        Subcatagory subcatagory = null;
        if (req.getSubcatagoryName() == null && (job.getSubcatagory() != null && !job.getSubcatagory().isEmpty())) {
            throw new RequiredFieldsEmpty("Job post", Collections.singletonList("Job subcatagory"));

        }

        if (req.getSubcatagoryName() != null) {
            List<Subcatagory> subcatagories = job.getSubcatagory();

            for (Subcatagory sub : subcatagories) {
                if (sub.getName().equalsIgnoreCase(req.getSubcatagoryName())) {
                    subcatagory = sub;
                    break;
                }
            }
            if (subcatagory == null) {
                throw new ObjectNotFound("subcatagory", "name");
            }
        }

        JobPost jobPost = new JobPost();
        jobPost.setDescription(req.getDescription());
        jobPost.setPeopleNeeded(req.getPeopleNeeded());
        jobPost.setSalary(req.getSalary());
        jobPost.setJobName(req.getJobName());
        jobPost.setSubcatagory(subcatagory);
        jobPost.setCompany(optionalCompany.get());

        JobPost post = jobPostRepo.save(jobPost);

        return new JobpostResponse(post.getId(), post.getDescription(), post.getCompany(), post.getJobName(),
                post.getSubcatagory(),
                post.getPeopleNeeded(), post.getSalary());

    }

    @Transactional
    public JobpostResponse updateJobPost(UpdateJobPost req, Long jobPostID) {
        Optional<JobPost> optionalJobPost = jobPostRepo.findById(jobPostID);
        if (!optionalJobPost.isPresent()) {
            throw new ObjectNotFound("Job post", "id");
        }

        Optional<Job> optionalJob = jobService.getJob(req.getJobName());
        if (!optionalJob.isPresent()) {
            throw new ObjectNotFound("Job", "name");
        }
        Subcatagory subcatagory = null;
        if (req.getSubcatagoryName() != null) {
            List<Subcatagory> subcatagories = optionalJob.get().getSubcatagory();

            for (Subcatagory sub : subcatagories) {
                if (sub.getName().equalsIgnoreCase(req.getSubcatagoryName())) {
                    subcatagory = sub;
                    break;
                }
            }

            if (subcatagory == null) {
                throw new ObjectNotFound("subcatagory", "name");
            }
        }
        JobPost jobPost = optionalJobPost.get();
        jobPost.setSubcatagory(subcatagory);
        if (req.getDescription() != null) {
            jobPost.setDescription(req.getDescription());

        }
        if (Long.valueOf(req.getPeopleNeeded()) != null) {
            jobPost.setPeopleNeeded(req.getPeopleNeeded());

        }
        if (Long.valueOf(req.getSalary()) != null) {
            jobPost.setSalary(req.getSalary());

        }

        return new JobpostResponse(jobPost.getId(), jobPost.getDescription(), jobPost.getCompany(),
                jobPost.getJobName(), jobPost.getSubcatagory(), jobPost.getPeopleNeeded(), jobPost.getSalary());

    }

    public void deleteJobPost(Long id) {
        Optional<JobPost> optionalJobPost = jobPostRepo.findById(id);
        if (!optionalJobPost.isPresent()) {
            throw new ObjectNotFound("Job post", "id");
        }

        jobPostRepo.deleteById(id);
    }

    public List<JobpostResponse> getAllJobPosts() {
        List<JobPost> posts = jobPostRepo.findAll();
        List<JobpostResponse> responses = new ArrayList<JobpostResponse>();

        for (JobPost post : posts) {
            JobpostResponse response = new JobpostResponse(post.getId(), post.getDescription(), post.getCompany(),
                    post.getJobName(), post.getSubcatagory(), post.getPeopleNeeded(), post.getSalary());
            responses.add(response);
        }

        return responses;
    }

    public JobpostResponse getJobPostById(Long id) {
        Optional<JobPost> optionalJobPost = jobPostRepo.findById(id);
        if (!optionalJobPost.isPresent()) {
            throw new ObjectNotFound("Job post", "id");
        }

        JobPost post = optionalJobPost.get();

        return new JobpostResponse(post.getId(), post.getDescription(), post.getCompany(), post.getJobName(),
                post.getSubcatagory(), post.getPeopleNeeded(), post.getSalary());
    }

    public JobPost getJobPostObjectById(Long id) {
        Optional<JobPost> optionalJobPost = jobPostRepo.findById(id);
        if (!optionalJobPost.isPresent()) {
            throw new ObjectNotFound("Job post", "id");
        }

        return optionalJobPost.get();
    }

    public List<JobApplicationResponse> getJobApplications(Long id) {

        Optional<JobPost> optionalJobPost = jobPostRepo.findById(id);
        if (!optionalJobPost.isPresent()) {
            throw new ObjectNotFound("Job post", "id");
        }

        List<JobApplication> applications = optionalJobPost.get().getJobApplications();
        List<JobApplicationResponse> responses = new ArrayList<JobApplicationResponse>();

        for (JobApplication app : applications) {
            Agent agent = app.getAgent();
            User user = agent.getUser();
            UserResponse userInfo = new UserResponse(user.getId(), user.getName(), user.getUsername(),
                    user.getEmail(),
                    user.getPhonenumber(), user.getBirthdate(), user.getRole());

            JobApplicationResponse response = new JobApplicationResponse(app.getId(), userInfo,
                    agent.getCv(),
                    app.getJobPost().getId(),
                    app.getAppliedAt(), app.getCoverLetter(),
                    String.valueOf(app.getStatus()));
            responses.add(response);
        }
        return responses;
    }

    public JobApplicationResponse getJobApplication(Long jobPostID, Long jobApplicationID) {
        List<JobApplicationResponse> applications = getJobApplications(jobPostID);

        for (JobApplicationResponse app : applications) {
            if (app.jobApplicationID() == jobApplicationID) {
                return app;
            }
        }

        throw new ObjectNotFound("Job application", "id");

    }

    @Transactional
    public JobApplicationResponse updateJobApplication(Long jobPostID, Long jobApplicationID,
            JobApplicationUpdateRequest req) {
        JobApplication app = getJobApplicationObject(jobPostID, jobApplicationID);
        app.setStatus(req.getStatus());
        Agent agent = app.getAgent();
        User user = agent.getUser();
        UserResponse userInfo = new UserResponse(user.getId(), user.getName(), user.getUsername(),
                user.getEmail(),
                user.getPhonenumber(), user.getBirthdate(), user.getRole());

        return new JobApplicationResponse(app.getId(), userInfo,
                agent.getCv(),
                app.getJobPost().getId(),
                app.getAppliedAt(), app.getCoverLetter(),
                String.valueOf(app.getStatus()));
    }

    private JobApplication getJobApplicationObject(Long jobPostID, Long jobApplicationID) {
        Optional<JobPost> optionalJobPost = jobPostRepo.findById(jobPostID);
        if (!optionalJobPost.isPresent()) {
            throw new ObjectNotFound("Job post", "id");
        }

        List<JobApplication> applications = optionalJobPost.get().getJobApplications();

        for (JobApplication app : applications) {
            if (app.getId() == jobApplicationID) {
                return app;
            }
        }

        throw new ObjectNotFound("Job Application", "id");
    }

}
