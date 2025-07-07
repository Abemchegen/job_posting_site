package sample.project.DTO.request;

import jakarta.validation.constraints.NotEmpty;

public record JobApplicationRequest(@NotEmpty String coverLetter) {

}
