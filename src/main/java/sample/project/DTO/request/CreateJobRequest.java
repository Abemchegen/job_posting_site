package sample.project.DTO.request;

import java.util.List;

import sample.project.Model.Subcatagory;

public record CreateJobRequest(String name, String description, List<Subcatagory> subcatagories) {

}
