package sample.project.Repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import sample.project.Model.Company;
import sample.project.Model.JobPost;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobPostRepo extends JpaRepository<JobPost, Long> {
    Optional<List<JobPost>> findByCompany(Company company);
}
