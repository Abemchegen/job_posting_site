package sample.project.DTO.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import sample.project.Model.Subcatagory;

public record SubCatagoryRequest(@NotNull Subcatagory subcatagory, @NotEmpty String jobName) {
}