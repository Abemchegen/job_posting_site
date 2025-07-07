package sample.project.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sample.project.Model.Company;
import sample.project.Model.Subcatagory;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobpostResponse {
    private long id;
    private String description;
    private Company company;
    private String jobName;
    private Subcatagory subcat;
    private int peopleNeeded;
    private long salary;

}
