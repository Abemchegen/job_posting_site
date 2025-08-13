
package sample.project.DTO.response;

import java.time.LocalDate;

import sample.project.Model.Cv;

public record JobApplicationResponse(long jobApplicationID, UserResponse userInfo, Cv cv, long jobPostID,
                LocalDate appliedAt,
                String coverLetter, String status, String cvURL, String jobName, String subcatName,
                String CompanyName) {
}