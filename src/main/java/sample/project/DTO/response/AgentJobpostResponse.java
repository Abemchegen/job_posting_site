package sample.project.DTO.response;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AgentJobpostResponse {
    private long id;
    private String description;
    private String companyName;
    private String companyPhonenumber;
    private String jobName;
    private String subcatName;
    private String subcatDesc;
    private int peopleNeeded;
    private long salary;
    private LocalDate date;
    private boolean applied;
    private long applicationid;
}
