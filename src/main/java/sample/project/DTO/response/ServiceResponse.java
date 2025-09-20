package sample.project.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ServiceResponse<T> {
    public boolean success;
    public String message;
    public T data;
}
