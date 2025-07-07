package sample.project.DTO.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sample.project.Model.Status;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobApplicationUpdateRequest {
    private Status status;
}
