package sample.project.Service;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import sample.project.DTO.request.JobApplicationRequest;
import sample.project.ErrorHandling.Exception.ObjectNotFound;
import sample.project.Model.Agent;
import sample.project.Model.JobApplication;
import sample.project.Model.JobPost;
import sample.project.Model.Status;
import sample.project.Repo.JobApplicationRepo;

@Service
@RequiredArgsConstructor
public class JobApplicationService {

    private final JobApplicationRepo jobApplicationRepo;
    private final AgentService agentService;
    private final JobPostService jobPostService;

    public JobApplication apply(JobApplicationRequest req, Long agentId) {

        Optional<Agent> agent = agentService.findAgentById(agentId);
        if (!agent.isPresent()) {
            throw new ObjectNotFound("agent", "id");
        }
        JobPost jobPost = jobPostService.getJobPostById(req.jobPostID());

        JobApplication jobApplication = new JobApplication();
        jobApplication.setAgent(agent.get());
        jobApplication.setJobPost(jobPost);
        jobApplication.setCoverLetter(req.coverLetter());
        jobApplication.setStatus(Status.PENDING);
        jobApplication.setAppliedAt(LocalDate.from(java.time.Instant.now()));

        return jobApplicationRepo.save(jobApplication);

    }

    public void delete(Long jobApplicationID) {
        jobApplicationRepo.deleteById(jobApplicationID);
    }

    public JobApplication findById(Long jobApplicationID) {
        Optional<JobApplication> jobApplication = jobApplicationRepo.findById(jobApplicationID);
        if (!jobApplication.isPresent()) {
            throw new ObjectNotFound("Job APplication", "id");
        }

        return jobApplication.get();
    }

    @Transactional
    public JobApplication update(Long jobApplicationID, JobApplicationRequest req) {
        Optional<JobApplication> optionalJobApplication = jobApplicationRepo.findById(jobApplicationID);
        if (!optionalJobApplication.isPresent()) {
            throw new ObjectNotFound("Job APplication", "id");
        }

        JobApplication jobApplication = optionalJobApplication.get();

        if (req.coverLetter() != null) {
            jobApplication.setCoverLetter(req.coverLetter());
        }
        return jobApplication;
    }

}
