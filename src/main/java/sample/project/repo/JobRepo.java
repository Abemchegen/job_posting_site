package sample.project.Repo;

import org.springframework.data.jpa.repository.JpaRepository;

import sample.project.Model.Job;
import java.util.Optional;

public interface JobRepo extends JpaRepository<Job, Long> {

    Optional<Job> findByName(String name);

}
