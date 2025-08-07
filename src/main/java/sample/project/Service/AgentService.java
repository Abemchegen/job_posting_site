package sample.project.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import sample.project.DTO.request.AddCvRequest;
import sample.project.DTO.response.AgentResponse;
import sample.project.ErrorHandling.Exception.ObjectNotFound;
import sample.project.Model.Agent;
import sample.project.Model.Award;
import sample.project.Model.Cv;
import sample.project.Model.Education;
import sample.project.Model.Experiance;
import sample.project.Model.JobApplication;
import sample.project.Model.Project;
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
    public AgentResponse addCv(AddCvRequest req, Long agentId) {
        Cv cv = new Cv();
        Resume resume = new Resume();
        for (Education edu : req.getEducation()) {
            edu.setResume(resume);
        }
        for (Experiance ex : req.getExperiance()) {
            ex.setResume(resume);
        }
        for (Project pro : req.getProject()) {
            pro.setResume(resume);
        }

        if (req.getAward() != null) {
            for (Award awa : req.getAward()) {
                awa.setResume(resume);
            }
        }
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
        return AgentResponse.builder()
                .id(agentId)
                .birthdate(user.getBirthdate())
                .email(user.getEmail())
                .phonenumber(user.getPhonenumber())
                .name(user.getName())
                .role(user.getRole())
                .cv(cv)
                .build();

    }

    @Transactional
    public AgentResponse updateCv(AddCvRequest req, Long agentId) {

        Optional<Agent> optionalAgent = agentRepo.findById(agentId);

        if (!optionalAgent.isPresent()) {
            throw new ObjectNotFound("User", "id");
        }

        Agent agent = optionalAgent.get();

        Cv cv = agent.getCv();
        Resume resume = cv.getResume();

        if (req.getAward() != null) {
            for (Award awa : req.getAward()) {
                awa.setResume(resume);
            }
            resume.getAward().clear();
            resume.getAward().addAll(req.getAward());
        }
        if (req.getEducation() != null) {
            for (Education edu : req.getEducation()) {
                edu.setResume(resume);
            }
            resume.getEducation().clear();
            resume.getEducation().addAll(req.getEducation());
        }
        if (req.getExperiance() != null) {

            for (Experiance ex : req.getExperiance()) {
                ex.setResume(resume);
            }
            resume.getExperiance().clear();
            resume.getExperiance().addAll(req.getExperiance());
        }
        if (req.getProject() != null) {
            for (Project pro : req.getProject()) {
                pro.setResume(resume);
            }
            resume.getProject().clear();
            resume.getProject().addAll(req.getProject());
        }
        if (req.getImageUrl() != null) {
            cv.setImageUrl(req.getImageUrl());
        }

        if (resume != null) {
            cv.setResume(resume);
        }

        agent.setCv(cv);
        User user = agent.getUser();
        return AgentResponse.builder()
                .id(agentId)
                .birthdate(user.getBirthdate())
                .email(user.getEmail())
                .phonenumber(user.getPhonenumber())
                .name(user.getName())
                .role(user.getRole())
                .cv(cv)
                .build();
    }

    public void deleteAgent(Long id) {
        Optional<Agent> agent = agentRepo.findById(id);
        if (!agent.isPresent()) {
            throw new ObjectNotFound("User", "id");
        }
        agentRepo.deleteById(id);
    }

    public List<JobApplication> getMyJobApplications(Long id) {
        Optional<Agent> agent = agentRepo.findById(id);
        if (!agent.isPresent()) {
            throw new ObjectNotFound("User", "id");
        }

        return agent.get().getJobApplications();

    }

    public Optional<Agent> findAgentById(Long agentId) {
        return agentRepo.findById(agentId);
    }

}
