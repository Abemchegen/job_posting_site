package sample.project.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import sample.project.DTO.response.AgentJobpostResponse;
import sample.project.DTO.response.JobApplicationResponse;
import sample.project.DTO.response.ServiceResponse;
import sample.project.DTO.response.UserResponse;
import sample.project.Model.Agent;
import sample.project.Model.JobApplication;
import sample.project.Model.JobPost;
import sample.project.Model.Status;
import sample.project.Model.User;
import sample.project.Repo.JobApplicationRepo;

@Service
@RequiredArgsConstructor
public class JobApplicationService {

    private final JobApplicationRepo jobApplicationRepo;
    private final AgentService agentService;
    private final JobPostService jobPostService;
    private final CloudinaryService cloudinaryService;

    public ServiceResponse<JobApplicationResponse> apply(String coverLetter, MultipartFile file, long agentId,
            long jobPostID) {

        Optional<Agent> agent = agentService.findAgentById(agentId);
        if (!agent.isPresent()) {
            return new ServiceResponse<JobApplicationResponse>(false, "Agent not found", null);
        }

        ServiceResponse<JobPost> jobPost = jobPostService.getJobPostObjectById(jobPostID);

        if (!jobPost.isSuccess()) {
            return new ServiceResponse<JobApplicationResponse>(false, "Job post not found", null);
        }

        JobApplication jobApplication = new JobApplication();
        jobApplication.setAgent(agent.get());
        jobApplication.setJobPost(jobPost.getData());
        jobApplication.setCoverLetter(coverLetter);
        jobApplication.setStatus(Status.PENDING);
        jobApplication.setAppliedAt(LocalDate.now());

        if (file != null) {
            if (!"application/pdf".equalsIgnoreCase(file.getContentType())) {
                return new ServiceResponse<JobApplicationResponse>(false, "Cv must be pdf", null);
            }
            // Optional: Check file extension
            String filename = file.getOriginalFilename();
            if (filename == null || !filename.toLowerCase().endsWith(".pdf")) {
                return new ServiceResponse<JobApplicationResponse>(false, "Cv extention must be .pdf", null);
            }
            String url = "";
            try {
                url = cloudinaryService.uploadFile(file, false);
            } catch (IOException e) {
                return new ServiceResponse<JobApplicationResponse>(false, "Cv upload failed", null);
            }
            jobApplication.setCvURL(url);
        }
        JobApplication app = jobApplicationRepo.save(jobApplication);
        User user = agent.get().getUser();
        UserResponse userInfo = new UserResponse(user.getId(), user.getName(), user.getEmail(),
                user.getPhonenumber(), user.getBirthdate(), user.getRole(), user.getPfpUrl());

        JobApplicationResponse res = new JobApplicationResponse(app.getId(), userInfo, agent.get().getCv(),
                app.getJobPost().getId(),
                app.getAppliedAt(), app.getCoverLetter(), String.valueOf(app.getStatus()), app.getCvURL(),
                jobPost.getData().getJobName(), null, jobPost.getData().getCompany().getName());

        if (jobPost.getData().getSubcatagory() != null) {
            res.setSubcatName(jobPost.getData().getSubcatagory().getName());
        }

        return new ServiceResponse<JobApplicationResponse>(true, "", res);
    }

    public ServiceResponse<String> delete(Long jobApplicationID) {

        Optional<JobApplication> app = jobApplicationRepo.findById(jobApplicationID);

        if (app.isPresent()) {
            try {
                cloudinaryService.deleteFile(app.get().getCvURL(), false);
            } catch (Exception e) {
                return new ServiceResponse<String>(false, "CV delete failed, application not deleted",
                        null);

            }
        }
        jobApplicationRepo.deleteById(jobApplicationID);
        return new ServiceResponse<String>(true, "Job application deleted",
                null);

    }

    public ServiceResponse<JobApplicationResponse> findById(Long jobApplicationID) {
        Optional<JobApplication> jobApplication = jobApplicationRepo.findById(jobApplicationID);
        if (!jobApplication.isPresent()) {
            return new ServiceResponse<JobApplicationResponse>(false, "job application not found", null);
        }

        JobApplication app = jobApplication.get();
        Agent agent = app.getAgent();
        User user = agent.getUser();
        UserResponse userInfo = new UserResponse(user.getId(), user.getName(), user.getEmail(),
                user.getPhonenumber(), user.getBirthdate(), user.getRole(), user.getPfpUrl());

        JobApplicationResponse res = new JobApplicationResponse(app.getId(), userInfo, agent.getCv(),
                app.getJobPost().getId(),
                app.getAppliedAt(), app.getCoverLetter(), String.valueOf(app.getStatus()), app.getCvURL(),
                app.getJobPost().getJobName(), null,
                app.getJobPost().getCompany().getName());

        if (app.getJobPost().getSubcatagory() != null) {
            res.setSubcatName(app.getJobPost().getSubcatagory().getName());
        }
        return new ServiceResponse<JobApplicationResponse>(true, "", res);

    }

    public List<JobApplicationResponse> findAllApplications(
            Integer salaryMin,
            Integer salaryMax,
            String date,
            String sort,
            String search,
            String status,
            long agentID) {
        List<JobApplication> applications = jobApplicationRepo.findAll();

        if (search != null) {
            System.out.println(search);
            applications.removeIf(app -> !(app.getJobPost().getJobName().toLowerCase().contains(search.toLowerCase()) ||
                    app.getJobPost().getCompany().getName().toLowerCase().contains(search.toLowerCase()) ||
                    app.getJobPost().getSubcatagory().getName().toLowerCase().contains(search.toLowerCase())));
        }
        if (salaryMin != null && salaryMax != null) {
            System.out.println("salary");
            applications.removeIf(
                    app -> !(app.getJobPost().getSalary() < salaryMin || app.getJobPost().getSalary() > salaryMax));
        }
        if (status != null) {
            System.out.println("status");
            applications.removeIf(
                    app -> !(app.getStatus().toString().equals(status)));
        }

        if (date != null) {
            System.out.println(date);
            LocalDate now = LocalDate.now();
            switch (date) {
                case "Past 24 hours":
                    applications.removeIf(app -> app.getAppliedAt().isBefore(now.minusDays(1)));
                    System.out.println("AS");

                    break;
                case "Past Week":
                    applications.removeIf(app -> app.getAppliedAt().isBefore(now.minusWeeks(1)));
                    System.out.println("jk");

                    break;
                case "Past Month":
                    applications.removeIf(app -> app.getAppliedAt().isBefore(now.minusMonths(1)));
                    System.out.println("hjh");

                    break;
            }
        }

        if ("Latest Applications".equalsIgnoreCase(sort)) {
            System.out.println(sort + "ghh");
            applications.sort(
                    java.util.Comparator.comparing((JobApplication app) -> app.getJobPost().getDate()).reversed());
        } else if ("Highest Salary".equalsIgnoreCase(sort)) {
            System.out.println(sort);
            applications.sort(
                    java.util.Comparator.comparing((JobApplication app) -> app.getJobPost().getSalary()).reversed());
        }

        List<JobApplicationResponse> response = new ArrayList<JobApplicationResponse>();

        for (JobApplication app : applications) {
            if (app.getAgent().getId() == agentID) {
                Agent agent = app.getAgent();
                User user = agent.getUser();
                UserResponse userInfo = new UserResponse(user.getId(), user.getName(),
                        user.getEmail(),
                        user.getPhonenumber(), user.getBirthdate(), user.getRole(), user.getPfpUrl());
                JobApplicationResponse res = new JobApplicationResponse(app.getId(), userInfo, agent.getCv(),
                        app.getJobPost().getId(),
                        app.getAppliedAt(), app.getCoverLetter(), String.valueOf(app.getStatus()), app.getCvURL(),
                        app.getJobPost().getJobName(), null,
                        app.getJobPost().getCompany().getName());

                if (app.getJobPost().getSubcatagory() != null) {
                    res.setSubcatName(app.getJobPost().getSubcatagory().getName());

                }
                response.add(res);

            }

        }
        return response;

    }

    @Transactional
    public ServiceResponse<JobApplicationResponse> update(Long jobApplicationID, String coverLetter,
            MultipartFile file) {
        Optional<JobApplication> optionalJobApplication = jobApplicationRepo.findById(jobApplicationID);
        if (!optionalJobApplication.isPresent()) {
            return new ServiceResponse<JobApplicationResponse>(false, "Job application not found", null);
        }

        JobApplication jobApplication = optionalJobApplication.get();

        if (coverLetter != null && jobApplication.getStatus().equals(Status.valueOf("PENDING"))) {
            jobApplication.setCoverLetter(coverLetter);
        }
        Agent agent = jobApplication.getAgent();
        User user = agent.getUser();
        UserResponse userInfo = new UserResponse(user.getId(), user.getName(),
                user.getEmail(),
                user.getPhonenumber(), user.getBirthdate(), user.getRole(), user.getPfpUrl());

        JobApplicationResponse res = new JobApplicationResponse(jobApplication.getId(), userInfo, agent.getCv(),
                jobApplication.getJobPost().getId(),
                jobApplication.getAppliedAt(), jobApplication.getCoverLetter(),
                String.valueOf(jobApplication.getStatus()), jobApplication.getCvURL(),
                jobApplication.getJobPost().getJobName(), null,
                jobApplication.getJobPost().getCompany().getName());

        if (jobApplication.getJobPost().getSubcatagory() != null) {
            res.setSubcatName(jobApplication.getJobPost().getSubcatagory().getName());
        }

        return new ServiceResponse<JobApplicationResponse>(true, "", res);

    }

    public ServiceResponse<List<AgentJobpostResponse>> getJobposts(Integer salaryMin, Integer salaryMax,
            String date, String sort,
            String search,
            String applied, long userId) {

        List<JobPost> posts = jobPostService.getAllJobpostObjects();

        if (search != null) {
            posts.removeIf(post -> !(post.getJobName().toLowerCase().contains(search.toLowerCase()) ||
                    post.getCompany().getName().toLowerCase().contains(search.toLowerCase()) ||
                    post.getSubcatagory().getName().toLowerCase().contains(search.toLowerCase())));
        }
        if (salaryMin != null && salaryMax != null) {
            System.out.println("salary");
            posts.removeIf(post -> !(post.getSalary() < salaryMin || post.getSalary() > salaryMax));
        }

        if (date != null) {
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
        if (applied != null) {
            if ("Applied".equalsIgnoreCase(applied)) {
                posts.removeIf(post -> jobApplicationRepo.findByAgentIdAndJobPostId(userId, post.getId()) == null);
            } else if ("Not Applied".equalsIgnoreCase(applied)) {
                posts.removeIf(post -> jobApplicationRepo.findByAgentIdAndJobPostId(userId, post.getId()) != null);
            }
        }

        if ("Latest Posts".equalsIgnoreCase(sort)) {
            posts.sort(java.util.Comparator.comparing(JobPost::getDate).reversed());
        } else if ("Highest Salary".equalsIgnoreCase(sort)) {
            posts.sort(java.util.Comparator.comparing(JobPost::getSalary).reversed());
        }

        List<AgentJobpostResponse> responses = new ArrayList<>();
        Optional<Agent> agent = agentService.findAgentById(userId);
        if (!agent.isPresent()) {
            return new ServiceResponse<>(false, "Agent not found", null);
        }
        for (JobPost post : posts) {
            JobApplication app = jobApplicationRepo.findByAgentIdAndJobPostId(userId, post.getId());
            if (app == null) {
                AgentJobpostResponse response = new AgentJobpostResponse(post.getId(), post.getDescription(),
                        post.getCompany().getName(),
                        post.getCompany().getPhoneNumber(),
                        post.getJobName(), null, null,
                        post.getPeopleNeeded(), post.getSalary(), post.getDate(), false, -1);

                if (post.getSubcatagory() != null) {
                    response.setSubcatName(post.getSubcatagory().getName());
                    response.setSubcatDesc(post.getSubcatagory().getDescription());
                }
                responses.add(response);
            }

            if (app != null) {
                AgentJobpostResponse response = new AgentJobpostResponse(post.getId(), post.getDescription(),
                        post.getCompany().getName(),
                        post.getCompany().getPhoneNumber(),
                        post.getJobName(), null, null,
                        post.getPeopleNeeded(), post.getSalary(), post.getDate(), true, app.getId());
                if (post.getSubcatagory() != null) {
                    response.setSubcatName(post.getSubcatagory().getName());
                    response.setSubcatDesc(post.getSubcatagory().getDescription());
                }
                responses.add(response);
            }
        }

        return new ServiceResponse<>(true, "", responses);

    }

    public ServiceResponse<AgentJobpostResponse> getJobPostById(Long id, long userId) {
        JobPost post = jobPostService.getJobpostObject(id);
        if (post == null) {
            return new ServiceResponse<>(false, "Post not found", null);
        }
        JobApplication app = jobApplicationRepo.findByAgentIdAndJobPostId(userId, post.getId());
        AgentJobpostResponse res;
        if (app == null) {
            res = new AgentJobpostResponse(post.getId(), post.getDescription(),
                    post.getCompany().getName(),
                    post.getCompany().getPhoneNumber(),
                    post.getJobName(), null, null,
                    post.getPeopleNeeded(), post.getSalary(), post.getDate(), false, -1);
        } else {
            res = new AgentJobpostResponse(post.getId(), post.getDescription(),
                    post.getCompany().getName(),
                    post.getCompany().getPhoneNumber(),
                    post.getJobName(), null, null,
                    post.getPeopleNeeded(), post.getSalary(), post.getDate(), true, app.getId());
        }

        return new ServiceResponse<>(true, "", res);
    }

}
