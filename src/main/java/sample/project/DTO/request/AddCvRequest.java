package sample.project.DTO.request;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AddCvRequest {
    private List<ExperianceDTO> experiance;
    private List<EducationDTO> education;
    private List<ProjectDTO> project;
    private List<AwardDTO> award;
}
