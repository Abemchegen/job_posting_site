package sample.project.DTO.response;

public record LoginResponse(Long id, String access_token, String refresh_token, UserResponse response,
                String statusDesc) {

}
