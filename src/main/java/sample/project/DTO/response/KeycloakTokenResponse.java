package sample.project.DTO.response;

import lombok.Data;

@Data
public class KeycloakTokenResponse {
    private String access_token;
    private String refresh_token;

}