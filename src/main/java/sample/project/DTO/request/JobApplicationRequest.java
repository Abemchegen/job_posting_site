package sample.project.DTO.request;

import jakarta.validation.constraints.NotEmpty;

public record JobApplicationRequest(@NotEmpty Long jobPostID, @NotEmpty String coverLetter) {

}
