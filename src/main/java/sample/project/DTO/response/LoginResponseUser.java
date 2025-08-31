package sample.project.DTO.response;

public record LoginResponseUser(String token, UserResponse response,
        String statusDesc) {

}
