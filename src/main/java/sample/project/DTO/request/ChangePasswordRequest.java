package sample.project.DTO.request;

public record ChangePasswordRequest(String email, String oldPassword, String newPassword) {
}
