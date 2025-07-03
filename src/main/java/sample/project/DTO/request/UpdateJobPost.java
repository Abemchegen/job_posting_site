package sample.project.DTO.request;

public record UpdateJobPost(String description, String jobName, Integer peopleNeeded,
        Float salary) {
}
