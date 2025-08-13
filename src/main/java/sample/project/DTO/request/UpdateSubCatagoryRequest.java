package sample.project.DTO.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record UpdateSubCatagoryRequest(@NotNull UpdateSubcat subcatagory, @NotEmpty String jobName) {
}