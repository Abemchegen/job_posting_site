package sample.project.DTO.response;

import java.time.LocalDate;
import sample.project.Model.Cv;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobApplicationResponse {
    private long jobApplicationID;
    private UserResponse userInfo;
    private Cv cv;
    private long jobPostID;
    private LocalDate appliedAt;
    private String coverLetter;
    private String status;
    private String cvURL;
    private String jobName;
    private String subcatName;
    private String companyName;
}