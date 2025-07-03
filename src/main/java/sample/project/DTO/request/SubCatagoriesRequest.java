package sample.project.DTO.request;

import java.util.List;

import sample.project.Model.Subcatagory;

public record SubCatagoriesRequest(List<Subcatagory> subcatagories, String jobName) {

}
