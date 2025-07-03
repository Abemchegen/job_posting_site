package sample.project.Repo;

import org.springframework.data.jpa.repository.JpaRepository;

import sample.project.Model.Agent;

public interface AgentRepo extends JpaRepository<Agent, Long> {

}
