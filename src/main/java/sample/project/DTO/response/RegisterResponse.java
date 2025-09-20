package sample.project.DTO.response;

public record RegisterResponse(Long id, String accessToken, String refreshToken, UserResponse response) {
}
