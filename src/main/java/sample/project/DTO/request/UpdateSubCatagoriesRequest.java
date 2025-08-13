package sample.project.DTO.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

public record UpdateSubCatagoriesRequest(@NotEmpty List<UpdateSubcat> subcatagories, @NotEmpty String jobName) {

}
