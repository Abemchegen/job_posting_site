package sample.project.DTO.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import sample.project.Model.Subcatagory;

public record SubCatagoriesRequest(@NotEmpty List<Subcatagory> subcatagories, @NotEmpty String jobName) {

}
