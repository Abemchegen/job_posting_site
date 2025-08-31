package sample.project.DTO.response;

public record RegisterResponse(Long id, String access_token, String refresh_token, UserResponse response) {
}
