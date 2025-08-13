package sample.project.DTO.response;

import java.util.List;

import sample.project.DTO.request.AwardDTO;
import sample.project.DTO.request.EducationDTO;
import sample.project.DTO.request.ExperianceDTO;
import sample.project.DTO.request.ProjectDTO;

public record CvResponse(List<ExperianceDTO> experiance,
        List<EducationDTO> education,
        List<ProjectDTO> project,
        List<AwardDTO> award) {

}
