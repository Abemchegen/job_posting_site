package sample.project.Repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import sample.project.Model.JobApplication;

@Repository
public interface JobApplicationRepo extends JpaRepository<JobApplication, Long> {
    JobApplication findByAgentIdAndJobPostId(long agentId, long jobPostId);

    JobApplication findByAgentId(long agentId);

}
