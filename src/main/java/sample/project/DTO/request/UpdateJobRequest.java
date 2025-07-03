package sample.project.DTO.request;

public record UpdateJobRequest(String existingJobname, String updatedJobName, String description) {

}
