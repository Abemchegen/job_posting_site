package sample.project.DTO.request;

public record JobPostRequest(String companyName, String description, String jobName, Integer peopleNeeded,
        Float salary) {

}
