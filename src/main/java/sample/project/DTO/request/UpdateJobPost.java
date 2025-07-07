
package sample.project.DTO.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateJobPost {
    private String jobName;
    private String description;
    private String subcatagoryName;
    private int peopleNeeded;
    private long salary;
}
