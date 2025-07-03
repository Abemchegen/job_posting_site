package sample.project.Repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import sample.project.Model.JobPost;

@Repository
public interface JobPostRepo extends JpaRepository<JobPost, Long> {

}
