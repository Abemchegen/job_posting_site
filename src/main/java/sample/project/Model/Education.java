package sample.project.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Education {
    private EducationLevel educationLevel;
    private String educationInstitution;
    private Integer gpa;
}
