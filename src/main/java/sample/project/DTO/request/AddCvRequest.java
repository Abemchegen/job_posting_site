package sample.project.DTO.request;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sample.project.Model.Award;
import sample.project.Model.Education;
import sample.project.Model.Experiance;
import sample.project.Model.Project;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AddCvRequest {
    private String imageUrl;
    private List<Experiance> experiance;
    private List<Education> education;
    private List<Project> project;
    private List<Award> award;
}
