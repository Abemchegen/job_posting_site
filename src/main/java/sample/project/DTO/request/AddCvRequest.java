package sample.project.DTO.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
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
    @NotEmpty
    private String imageUrl;
    @NotEmpty
    private List<Experiance> experiance;
    @NotEmpty
    private List<Education> education;
    @NotEmpty
    private List<Project> project;
    private List<Award> award;
}
