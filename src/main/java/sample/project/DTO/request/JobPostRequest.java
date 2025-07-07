package sample.project.DTO.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobPostRequest {
    @NotEmpty
    private String companyName;

    @NotEmpty
    private String description;

    @NotEmpty
    private String jobName;

    private String subcatagoryName;

    @NotNull
    @Positive
    private int peopleNeeded;

    @NotNull
    @Positive
    private long salary;
}
