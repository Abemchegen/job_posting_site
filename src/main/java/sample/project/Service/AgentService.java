package sample.project.Service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import sample.project.DTO.request.AddCvRequest;
import sample.project.DTO.response.UserResponse;
import sample.project.ErrorHandling.Exception.ObjectNotFound;
import sample.project.Model.Agent;
import sample.project.Model.Cv;
import sample.project.Model.Resume;
import sample.project.Model.User;
import sample.project.Repo.AgentRepo;

@Service
@RequiredArgsConstructor
public class AgentService {

    private final AgentRepo agentRepo;

    public Agent addAgent(User user) {
        Agent agent = new Agent();
        agent.setUser(user);
        return agentRepo.save(agent);
    }

    @Transactional
    public UserResponse addCv(AddCvRequest req, Long agentId) {
        Cv cv = new Cv();
        Resume resume = new Resume();
        resume.setEducation(req.getEducation());
        resume.setExperiance(req.getExperiance());
        resume.setProject(req.getProject());
        resume.setAward(req.getAward());
        cv.setImageUrl(req.getImageUrl());
        cv.setResume(resume);

        Optional<Agent> optionalAgent = agentRepo.findById(agentId);

        if (!optionalAgent.isPresent()) {
            throw new ObjectNotFound("User", "id");
        }

        Agent agent = optionalAgent.get();
        agent.setCv(cv);

        User user = agent.getUser();
        return new UserResponse(user.getId(), user.getName(), user.getUsername(), null, null, null, agent.getCv(),
                user.getEmail(), user.getPhonenumber(), user.getBirthdate(),
                user.getRole());

    }

    @Transactional
    public UserResponse updateCv(AddCvRequest req, Long agentId) {

        Optional<Agent> optionalAgent = agentRepo.findById(agentId);

        if (!optionalAgent.isPresent()) {
            throw new ObjectNotFound("User", "id");
        }

        Agent agent = optionalAgent.get();

        Cv cv = agent.getCv();
        Resume resume = new Resume();

        if (req.getAward() != null) {
            resume.setAward(req.getAward());
        }
        if (req.getEducation() != null) {
            resume.setEducation(req.getEducation());
        }
        if (req.getExperiance() != null) {
            resume.setExperiance(req.getExperiance());
        }
        if (req.getProject() != null) {
            resume.setProject(req.getProject());
        }
        if (req.getImageUrl() != null) {
            cv.setImageUrl(req.getImageUrl());
        }

        if (resume != null) {
            cv.setResume(resume);
        }

        agent.setCv(cv);
        User user = agent.getUser();
        return new UserResponse(user.getId(), user.getName(), user.getUsername(), null, null, null, agent.getCv(),
                user.getEmail(), user.getPhonenumber(), user.getBirthdate(),
                user.getRole());
    }

    public void deleteAgent(Long id) {
        Optional<Agent> agent = agentRepo.findById(id);
        if (!agent.isPresent()) {
            throw new ObjectNotFound("User", "id");
        }
        agentRepo.deleteById(id);
    }

}
