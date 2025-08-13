package sample.project.Service;

import java.time.LocalDate;
import java.util.ArrayList;
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
import sample.project.Model.Agent;
import sample.project.Model.Company;
import sample.project.Model.Job;
import sample.project.Model.JobApplication;
import sample.project.Model.JobPost;
import sample.project.Model.Subcatagory;
import sample.project.Model.User;
import sample.project.Repo.JobApplicationRepo;
import sample.project.Repo.JobPostRepo;

@Service
@RequiredArgsConstructor
public class JobPostService {

    private final JobPostRepo jobPostRepo;
    private final CompanyService companyService;
    private final JobService jobService;
    private final EmailService emailService;

    public JobpostResponse postJob(JobPostRequest req) {
        Optional<Company> optionalCompany = companyService.getCompany(req.getCompanyName());
        if (!optionalCompany.isPresent()) {
            throw new ObjectNotFound("Company", "name");
        }

        Optional<Job> optionalJob = jobService.getJob(req.getJobName());
        System.err.println(req.getJobName());
        if (!optionalJob.isPresent()) {
            throw new ObjectNotFound("Job", "name");
        }
        Job job = optionalJob.get();
        Subcatagory subcatagory = null;

        if (req.getSubcatagoryName() != null) {
            List<Subcatagory> subcatagories = job.getSubcatagory();

            for (Subcatagory sub : subcatagories) {
                if (sub.getName().equalsIgnoreCase(req.getSubcatagoryName())) {
                    subcatagory = sub;
                    break;
                }
            }

        }

        JobPost jobPost = new JobPost();
        jobPost.setDescription(req.getDescription());
        jobPost.setPeopleNeeded(req.getPeopleNeeded());
        jobPost.setSalary(req.getSalary());
        jobPost.setJobName(req.getJobName());
        jobPost.setDate(LocalDate.now());
        jobPost.setSubcatagory(subcatagory);
        jobPost.setCompany(optionalCompany.get());

        JobPost post = jobPostRepo.save(jobPost);

        JobpostResponse res = new JobpostResponse(post.getId(), post.getDescription(), post.getCompany().getName(),
                post.getCompany().getPhoneNumber(), post.getJobName(),
                null, null,
                post.getPeopleNeeded(), post.getSalary(), jobPost.getDate());

        if (post.getSubcatagory() != null) {
            res.setSubcatName(post.getSubcatagory().getName());
            res.setSubcatDesc(post.getSubcatagory().getDescription());
        }

        return res;

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

        JobpostResponse response = new JobpostResponse(jobPost.getId(), jobPost.getDescription(),
                jobPost.getCompany().getName(),
                jobPost.getCompany().getPhoneNumber(),
                jobPost.getJobName(), null, null,
                jobPost.getPeopleNeeded(), jobPost.getSalary(),
                jobPost.getDate());

        if (jobPost.getSubcatagory() != null) {
            response.setSubcatName(jobPost.getSubcatagory().getName());
            response.setSubcatDesc(jobPost.getSubcatagory().getDescription());
        }
        return response;
    }

    public void deleteJobPost(Long id) {
        Optional<JobPost> optionalJobPost = jobPostRepo.findById(id);
        if (!optionalJobPost.isPresent()) {
            throw new ObjectNotFound("Job post", "id");
        }

        jobPostRepo.deleteById(id);
    }

    public JobpostResponse getJobPostById(Long id) {
        Optional<JobPost> optionalJobPost = jobPostRepo.findById(id);
        if (!optionalJobPost.isPresent()) {
            throw new ObjectNotFound("Job post", "id");
        }

        JobPost post = optionalJobPost.get();

        JobpostResponse res = new JobpostResponse(post.getId(), post.getDescription(), post.getCompany().getName(),
                post.getCompany().getPhoneNumber(), post.getJobName(),
                null, null, post.getPeopleNeeded(),
                post.getSalary(), post.getDate());

        if (post.getSubcatagory() != null) {
            res.setSubcatName(post.getSubcatagory().getName());
            res.setSubcatDesc(post.getSubcatagory().getDescription());
        }

        return res;
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
            UserResponse userInfo = new UserResponse(user.getId(), user.getName(),
                    user.getEmail(),
                    user.getPhonenumber(), user.getBirthdate(), user.getRole(), user.getPfpUrl());

            JobApplicationResponse response = new JobApplicationResponse(app.getId(), userInfo,
                    agent.getCv(),
                    app.getJobPost().getId(),
                    app.getAppliedAt(), app.getCoverLetter(),
                    String.valueOf(app.getStatus()), app.getCvURL(), app.getJobPost().getJobName(),
                    null,
                    app.getJobPost().getCompany().getName());

            if (app.getJobPost().getSubcatagory() != null) {
                response.setSubcatName(app.getJobPost().getSubcatagory().getName());
            }
            responses.add(response);
        }
        return responses;
    }

    public JobApplicationResponse getJobApplication(Long jobPostID, Long jobApplicationID) {
        List<JobApplicationResponse> applications = getJobApplications(jobPostID);

        for (JobApplicationResponse app : applications) {
            if (app.getJobApplicationID() == jobApplicationID) {
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

        emailService.sendMessage(
                user.getEmail(),
                "Application status update",
                "Your application to company " + app.getJobPost().getCompany().getName() +
                        " for the position " + app.getJobPost().getJobName() +
                        " has changed status to: " + app.getStatus().toString());

        UserResponse userInfo = new UserResponse(user.getId(), user.getName(),
                user.getEmail(),
                user.getPhonenumber(), user.getBirthdate(), user.getRole(), user.getPfpUrl());

        JobApplicationResponse res = new JobApplicationResponse(app.getId(), userInfo,
                agent.getCv(),
                app.getJobPost().getId(),
                app.getAppliedAt(), app.getCoverLetter(),
                String.valueOf(app.getStatus()), app.getCvURL(), app.getJobPost().getJobName(),
<<<<<<< HEAD
                null,
                app.getJobPost().getCompany().getName());

        if (app.getJobPost().getSubcatagory() != null) {
            res.setSubcatName(app.getJobPost().getSubcatagory().getName());
        }

        return res;

=======
                app.getJobPost().getSubcatagory().getName(),
                app.getJobPost().getCompany().getName());
>>>>>>> 837cf3d (debugging from integration, fixed many dto errors, added url search params)
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

    public List<JobpostResponse> getAllJobPostsCompany(String name, Integer salaryMin, Integer salaryMax, String date,
            String sort, String search) {
        Optional<Company> optionalCompany = companyService.getCompany(name);
        if (!optionalCompany.isPresent()) {
            throw new ObjectNotFound("Company", "name");
        }

        Company company = optionalCompany.get();
        Optional<List<JobPost>> optionalResponses = jobPostRepo.findByCompany(company);

        if (!optionalResponses.isPresent()) {
            return new ArrayList<JobpostResponse>();
        }
        List<JobPost> posts = optionalResponses.get();

        if (search != null) {
            System.out.println(search);
            posts.removeIf(post -> !(post.getJobName().toLowerCase().contains(search.toLowerCase()) ||
<<<<<<< HEAD
                    ((post.getSubcatagory() != null)
                            && post.getSubcatagory().getName().toLowerCase().contains(search.toLowerCase()))));
        }
        if (salaryMin != null && salaryMax != null) {
            System.out.println("salary");
            posts.removeIf(post -> (post.getSalary() < salaryMin || post.getSalary() > salaryMax));
=======
                    post.getSubcatagory().getName().toLowerCase().contains(search.toLowerCase())));
        }
        if (salaryMin != null && salaryMax != null) {
            System.out.println("salary");
            posts.removeIf(post -> !(post.getSalary() < salaryMin || post.getSalary() > salaryMax));
>>>>>>> 837cf3d (debugging from integration, fixed many dto errors, added url search params)
        }

        if (date != null) {
            System.out.println(date);
            LocalDate now = LocalDate.now();
            switch (date) {
                case "Past 24 hours":
                    posts.removeIf(post -> post.getDate().isBefore(now.minusDays(1)));
                    System.out.println("AS");

                    break;
                case "Past Week":
                    posts.removeIf(post -> post.getDate().isBefore(now.minusWeeks(1)));
                    System.out.println("jk");

                    break;
                case "Past Month":
                    posts.removeIf(post -> post.getDate().isBefore(now.minusMonths(1)));
                    System.out.println("hjh");
                    break;
            }
        }

        if ("Latest Posts".equalsIgnoreCase(sort)) {
            System.out.println(sort + "ghh");
            posts.sort(java.util.Comparator.comparing(JobPost::getDate).reversed());
        } else if ("Highest Salary".equalsIgnoreCase(sort)) {
            System.out.println(sort);

            posts.sort(java.util.Comparator.comparing(JobPost::getSalary).reversed());
        }

        List<JobpostResponse> response = new ArrayList<>();

        for (JobPost post : posts) {
            JobpostResponse res = new JobpostResponse(post.getId(), post.getDescription(), post.getCompany().getName(),
                    post.getCompany().getPhoneNumber(),
                    post.getJobName(), null, null,
                    post.getPeopleNeeded(), post.getSalary(), post.getDate());
            if (post.getSubcatagory() != null) {
                res.setSubcatName(post.getSubcatagory().getName());
                res.setSubcatDesc(post.getSubcatagory().getDescription());
            }

            response.add(res);
        }
<<<<<<< HEAD
        System.out.println(response);
=======
>>>>>>> 837cf3d (debugging from integration, fixed many dto errors, added url search params)
        return response;
    }

    public List<JobPost> getAllJobpostObjects() {
        return jobPostRepo.findAll();
    }

    public JobPost getJobpostObject(long id) {
        return jobPostRepo.findById(id).get();
    }

}
