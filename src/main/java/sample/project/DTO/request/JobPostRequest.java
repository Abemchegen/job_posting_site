package sample.project.DTO.request;

import jakarta.validation.constraints.NotEmpty;

public record JobPostRequest(@NotEmpty String companyName, @NotEmpty String description, @NotEmpty String jobName,
                @NotEmpty Integer peopleNeeded,
                @NotEmpty Float salary) {

}
