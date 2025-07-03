package sample.project.Model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Resume {
    private List<Experiance> experiance;
    private List<Education> education;
    private List<Project> project;
    private List<Award> award;
}
