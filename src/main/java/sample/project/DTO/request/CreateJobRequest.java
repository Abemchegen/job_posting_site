package sample.project.DTO.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

public record CreateJobRequest(@NotEmpty String name, @NotEmpty String description, List<Subcat> subcatagories) {

}
