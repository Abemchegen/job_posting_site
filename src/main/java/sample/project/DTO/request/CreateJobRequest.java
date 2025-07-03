package sample.project.DTO.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import sample.project.Model.Subcatagory;

public record CreateJobRequest(@NotEmpty String name, @NotEmpty String description, List<Subcatagory> subcatagories) {

}
