package sample.project.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import sample.project.DTO.request.JobApplicationRequest;
import sample.project.DTO.response.JobApplicationResponse;
import sample.project.DTO.response.UserResponse;
import sample.project.ErrorHandling.Exception.ObjectNotFound;
import sample.project.ErrorHandling.Exception.RequiredFieldsEmpty;
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

    public JobApplicationResponse apply(JobApplicationRequest req, long agentId, long jobPostID) {

        Optional<Agent> agent = agentService.findAgentById(agentId);
        if (!agent.isPresent()) {
            throw new ObjectNotFound("agent", "id");
        }
        if (agent.get().getCv() == null) {
            throw new RequiredFieldsEmpty("Job application", Collections.singletonList("CV"));
        }
        JobPost jobPost = jobPostService.getJobPostObjectById(jobPostID);

        JobApplication jobApplication = new JobApplication();
        jobApplication.setAgent(agent.get());
        jobApplication.setJobPost(jobPost);
        jobApplication.setCoverLetter(req.coverLetter());
        jobApplication.setStatus(Status.PENDING);
        jobApplication.setAppliedAt(LocalDate.now());

        JobApplication app = jobApplicationRepo.save(jobApplication);
        User user = agent.get().getUser();
        UserResponse userInfo = new UserResponse(user.getId(), user.getName(), user.getUsername(), user.getEmail(),
                user.getPhonenumber(), user.getBirthdate(), user.getRole());

        return new JobApplicationResponse(app.getId(), userInfo, agent.get().getCv(), app.getJobPost().getId(),
                app.getAppliedAt(), app.getCoverLetter(), String.valueOf(app.getStatus()));
    }

    public void delete(Long jobApplicationID) {
        jobApplicationRepo.deleteById(jobApplicationID);
    }

    public JobApplicationResponse findById(Long jobApplicationID) {
        Optional<JobApplication> jobApplication = jobApplicationRepo.findById(jobApplicationID);
        if (!jobApplication.isPresent()) {
            throw new ObjectNotFound("Job APplication", "id");
        }

        JobApplication app = jobApplication.get();
        Agent agent = app.getAgent();
        User user = agent.getUser();
        UserResponse userInfo = new UserResponse(user.getId(), user.getName(), user.getUsername(), user.getEmail(),
                user.getPhonenumber(), user.getBirthdate(), user.getRole());

        return new JobApplicationResponse(app.getId(), userInfo, agent.getCv(), app.getJobPost().getId(),
                app.getAppliedAt(), app.getCoverLetter(), String.valueOf(app.getStatus()));
    }

    public List<JobApplicationResponse> findAllApplications(long agentID) {
        List<JobApplication> applications = jobApplicationRepo.findAll();
        List<JobApplicationResponse> response = new ArrayList<JobApplicationResponse>();

        for (JobApplication app : applications) {
            if (app.getAgent().getId() == agentID) {
                Agent agent = app.getAgent();
                User user = agent.getUser();
                UserResponse userInfo = new UserResponse(user.getId(), user.getName(), user.getUsername(),
                        user.getEmail(),
                        user.getPhonenumber(), user.getBirthdate(), user.getRole());
                response.add(new JobApplicationResponse(app.getId(), userInfo, agent.getCv(), app.getJobPost().getId(),
                        app.getAppliedAt(), app.getCoverLetter(), String.valueOf(app.getStatus())));
            }
        }
        return response;

    }

    @Transactional
    public JobApplicationResponse update(Long jobApplicationID, JobApplicationRequest req) {
        Optional<JobApplication> optionalJobApplication = jobApplicationRepo.findById(jobApplicationID);
        if (!optionalJobApplication.isPresent()) {
            throw new ObjectNotFound("Job Application", "id");
        }

        JobApplication jobApplication = optionalJobApplication.get();

        if (req.coverLetter() != null && jobApplication.getStatus().equals(Status.valueOf("PENDING"))) {
            jobApplication.setCoverLetter(req.coverLetter());
        }
        Agent agent = jobApplication.getAgent();
        User user = agent.getUser();
        UserResponse userInfo = new UserResponse(user.getId(), user.getName(), user.getUsername(),
                user.getEmail(),
                user.getPhonenumber(), user.getBirthdate(), user.getRole());

        return new JobApplicationResponse(jobApplication.getId(), userInfo, agent.getCv(),
                jobApplication.getJobPost().getId(),
                jobApplication.getAppliedAt(), jobApplication.getCoverLetter(),
                String.valueOf(jobApplication.getStatus()));
    }

}
