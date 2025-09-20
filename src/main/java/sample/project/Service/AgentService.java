package sample.project.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import sample.project.DTO.request.AddCvRequest;
import sample.project.DTO.request.AwardDTO;
import sample.project.DTO.request.EducationDTO;
import sample.project.DTO.request.ExperianceDTO;
import sample.project.DTO.request.ProjectDTO;
import sample.project.DTO.request.UpdateCvRequest;
import sample.project.DTO.response.AgentResponse;
import sample.project.DTO.response.ServiceResponse;
import sample.project.Model.Agent;
import sample.project.Model.Award;
import sample.project.Model.Cv;
import sample.project.Model.Education;
import sample.project.Model.EducationLevel;
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
    public ServiceResponse<AgentResponse> addCv(AddCvRequest req, Long agentId) {
        Optional<Agent> optionalAgent = agentRepo.findById(agentId);

        if (!optionalAgent.isPresent()) {
            return new ServiceResponse<>(false, "User not found", null);
        }

        // Ensure all lists are initialized

        Agent agent = optionalAgent.get();
        Cv cv = agent.getCv();
        if (cv == null) {
            cv = new Cv();
        }
        Resume resume = cv.getResume();
        if (resume == null) {
            resume = new Resume();
        }
        if (req.getEducation() != null) {
            for (EducationDTO edu : req.getEducation()) {
                Education education = new Education();
                education.setInstitution(edu.institution());
                education.setLevel(EducationLevel.valueOf(edu.level()));
                education.setGpa(Float.parseFloat(edu.gpa()));
                education.setResume(resume);
                if (resume.getEducation() == null) {
                    resume.setEducation(new java.util.ArrayList<>());
                }
                resume.getEducation().add(education);
            }
        }
        if (req.getExperiance() != null) {
            for (ExperianceDTO ex : req.getExperiance()) {
                Experiance experiance = new Experiance();
                experiance.setDescription(ex.description());
                experiance.setName(ex.name());
                experiance.setYear(Integer.parseInt(ex.year()));
                experiance.setResume(resume);
                if (resume.getExperiance() == null) {
                    resume.setExperiance(new java.util.ArrayList<>());
                }
                resume.getExperiance().add(experiance);
            }
        }
        if (req.getProject() != null) {
            for (ProjectDTO pro : req.getProject()) {
                Project project = new Project();
                project.setDescription(pro.description());
                project.setName(pro.name());
                project.setUrl(pro.url());
                project.setResume(resume);
                if (resume.getProject() == null) {
                    resume.setProject(new java.util.ArrayList<>());
                }
                resume.getProject().add(project);
            }
        }
        if (req.getAward() != null) {
            for (AwardDTO awa : req.getAward()) {
                Award award = new Award();
                award.setDescription(awa.description());
                award.setName(awa.name());
                award.setUrl(awa.url());
                award.setResume(resume);

                if (resume.getAward() == null) {
                    resume.setAward(new java.util.ArrayList<>());
                }
                resume.getAward().add(award);
            }
        }
        if (agent.getUser().getPfpUrl() != null) {
            cv.setImageUrl(agent.getUser().getPfpUrl());
        }
        cv.setResume(resume);

        agent.setCv(cv);

        User user = agent.getUser();
        AgentResponse res = AgentResponse.builder()
                .id(agentId)
                .birthdate(user.getBirthdate())
                .email(user.getEmail())
                .phonenumber(user.getPhonenumber())
                .name(user.getName())
                .pfp(user.getPfpUrl())
                .role(user.getRole())
                .cv(cv)
                .build();

        return new ServiceResponse<>(true, "", res);

    }

    @Transactional
    public ServiceResponse<AgentResponse> updateCv(UpdateCvRequest req, Long agentId) {

        Optional<Agent> optionalAgent = agentRepo.findById(agentId);

        if (!optionalAgent.isPresent()) {
            return new ServiceResponse<>(false, "User not found", null);
        }

        Agent agent = optionalAgent.get();

        Cv cv = agent.getCv();
        Resume resume = cv.getResume();

        if (req.getExperiance() != null) {
            List<Experiance> experiance = resume.getExperiance();
            for (Experiance ex : experiance) {
                if (ex.getId() == req.getId()) {
                    ex.setName(req.getExperiance().name());
                    ex.setYear(Integer.parseInt(req.getExperiance().year()));
                    ex.setDescription(req.getExperiance().description());
                    ex.setResume(resume);
                    break;

                }
            }
            resume.setExperiance(experiance);

        }

        if (req.getEducation() != null) {
            List<Education> educations = resume.getEducation();
            for (Education edu : educations) {
                if (edu.getId() == req.getId()) {
                    edu.setGpa(Float.parseFloat(req.getEducation().gpa()));
                    edu.setLevel(EducationLevel.valueOf(req.getEducation().level()));
                    edu.setInstitution(req.getEducation().institution());
                    edu.setResume(resume);
                    break;

                }
            }
            resume.setEducation(educations);

        }
        if (req.getProject() != null) {
            List<Project> project = resume.getProject();
            for (Project pro : project) {
                if (pro.getId() == req.getId()) {
                    pro.setName(req.getProject().name());
                    pro.setDescription(req.getProject().description());
                    pro.setUrl(req.getProject().url());
                    pro.setResume(resume);
                    break;

                }
            }
            resume.setProject(project);

        }
        if (req.getAward() != null) {
            List<Award> award = resume.getAward();
            for (Award awa : award) {
                if (awa.getId() == req.getId()) {
                    awa.setName(req.getAward().name());
                    awa.setUrl(req.getAward().url());
                    awa.setDescription(req.getAward().description());
                    awa.setResume(resume);
                    break;

                }
            }
            resume.setAward(award);

        }

        cv.setResume(resume);

        agent.setCv(cv);
        User user = agent.getUser();
        AgentResponse res = AgentResponse.builder()
                .id(agentId)
                .birthdate(user.getBirthdate())
                .email(user.getEmail())
                .phonenumber(user.getPhonenumber())
                .name(user.getName())
                .pfp(user.getPfpUrl())
                .role(user.getRole())
                .cv(cv)
                .build();

        return new ServiceResponse<>(true, "", res);

    }

    public ServiceResponse<String> deleteAgent(Long id) {
        Optional<Agent> agent = agentRepo.findById(id);
        if (!agent.isPresent()) {
            return new ServiceResponse<>(false, "User not found", null);
        }
        agentRepo.deleteById(id);
        return new ServiceResponse<>(true, "", "");

    }

    public ServiceResponse<List<JobApplication>> getMyJobApplications(Long id) {
        Optional<Agent> agent = agentRepo.findById(id);
        if (!agent.isPresent()) {
            return new ServiceResponse<>(false, "User not found", null);
        }
        List<JobApplication> data = agent.get().getJobApplications();
        return new ServiceResponse<>(true, "", data);

    }

    public Optional<Agent> findAgentById(Long agentId) {
        return agentRepo.findById(agentId);
    }

    @Transactional
    public ServiceResponse<AgentResponse> deleteCv(String name, long deleteid, long agentid) {
        System.out.println("DeleteCv called with name: '" + name + "' and id: " + deleteid);
        Optional<Agent> optionalAgent = agentRepo.findById(agentid);

        if (!optionalAgent.isPresent()) {
            return new ServiceResponse<>(false, "User not found", null);
        }

        Agent agent = optionalAgent.get();

        Cv cv = agent.getCv();
        Resume resume = cv.getResume();
        if ("experiance".equals(name)) {
            List<Experiance> experiance = resume.getExperiance();
            for (Experiance ex : experiance) {
                if (ex.getId() == deleteid) {
                    experiance.remove(ex);
                    break;
                }
            }
            resume.setExperiance(experiance);

        } else if ("education".equals(name)) {
            System.out.println("here!!!");

            List<Education> educations = resume.getEducation();
            for (Education edu : educations) {
                if (edu.getId() == deleteid) {
                    educations.remove(edu);
                    System.out.println("deleted!!!");
                    break;

                }
            }
            resume.setEducation(educations);

        } else if ("project".equals(name)) {
            List<Project> project = resume.getProject();
            for (Project pro : project) {
                if (pro.getId() == deleteid) {
                    project.remove(pro);
                    break;

                }
            }
            resume.setProject(project);

        } else if ("award".equals(name)) {
            List<Award> award = resume.getAward();
            for (Award awa : award) {
                if (awa.getId() == deleteid) {
                    award.remove(awa);
                    break;

                }
            }
            resume.setAward(award);

        }

        cv.setResume(resume);

        agent.setCv(cv);
        User user = agent.getUser();
        AgentResponse res = AgentResponse.builder()
                .id(agentid)
                .birthdate(user.getBirthdate())
                .email(user.getEmail())
                .phonenumber(user.getPhonenumber())
                .name(user.getName())
                .pfp(user.getPfpUrl())
                .role(user.getRole())
                .cv(cv)
                .build();

        return new ServiceResponse<>(true, "", res);

    }

}
