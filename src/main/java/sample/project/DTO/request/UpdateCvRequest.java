package sample.project.DTO.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdateCvRequest {
    private long id;
    private ExperianceDTO experiance;
    private EducationDTO education;
    private ProjectDTO project;
    private AwardDTO award;
}
