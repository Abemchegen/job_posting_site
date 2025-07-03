package sample.project.Repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import sample.project.Model.Agent;

@Repository
public interface AgentRepo extends JpaRepository<Agent, Long> {

}
