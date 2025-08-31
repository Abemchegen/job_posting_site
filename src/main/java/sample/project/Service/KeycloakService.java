package sample.project.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import sample.project.DTO.response.KeycloakTokenResponse;
import sample.project.ErrorHandling.Exception.AccessDenied;

import java.util.List;
import java.util.Map;

@Service
public class KeycloakService {

    @Value("${keycloak.auth-server-url}")
    private String keycloakUrl;
    @Value("${keycloak.realm}")
    private String realm;
    @Value("${keycloak.clientId}")
    private String clientId;
    @Value("${keycloak.credentials.secret}")
    private String clientSecret;
    private final RestTemplate restTemplate = new RestTemplate();

    // 1. Create user in Keycloak
    public void createUserInKeycloak(String email, String name, String password, String role) {
        // You need admin access token to create users
        String adminToken = getAdminAccessToken();
        String firstName = "";
        String lastName = "";
        if (name != null && !name.isBlank()) {
            String[] parts = name.trim().split("\s+", 2);
            firstName = parts[0];
            lastName = (parts.length > 1) ? parts[1] : "-";
        }

        String url = keycloakUrl + "/admin/realms/" + realm + "/users";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> user = Map.of(
                "username", email,
                "email", email,
                "firstName", firstName,
                "lastName", lastName,
                "enabled", true,
                "credentials", new Object[] {
                        Map.of("type", "password", "value", password, "temporary", false)
                });

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(user, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        // Get the user ID from Keycloak (search by email)

        String searchUrl = keycloakUrl + "/admin/realms/" + realm + "/users?email=" + email;
        HttpEntity<?> searchEntity = new HttpEntity<>(headers);
        ResponseEntity<Object[]> searchResponse = restTemplate.exchange(searchUrl, HttpMethod.GET, searchEntity,
                Object[].class);
        if (searchResponse.getBody() == null || searchResponse.getBody().length == 0) {
            throw new RuntimeException("User not found in Keycloak after creation");
        }

        Map userObj = (Map) searchResponse.getBody()[0];
        String userId = (String) userObj.get("id");

        // Get the role ID from Keycloak
        String rolesUrl = keycloakUrl + "/admin/realms/" + realm + "/roles/" + role;
        ResponseEntity<Map> roleResponse = restTemplate.exchange(rolesUrl, HttpMethod.GET, searchEntity, Map.class);
        Map roleObj = roleResponse.getBody();
        if (roleObj == null || !roleObj.containsKey("id")) {
            throw new RuntimeException("Role not found in Keycloak: " + role);
        }
        String roleId = (String) roleObj.get("id");
        String roleName = (String) roleObj.get("name");

        // Assign the role to the user
        String assignRoleUrl = keycloakUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm";
        List<Map<String, Object>> rolesToAssign = List.of(
                Map.of("id", roleId, "name", roleName));
        HttpEntity<List<Map<String, Object>>> assignEntity = new HttpEntity<>(rolesToAssign, headers);
        restTemplate.postForEntity(assignRoleUrl, assignEntity, String.class);
    }

    // 2. Authenticate user (get token by password)
    public KeycloakTokenResponse getTokenByPassword(String email, String password) {
        String url = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "grant_type=password"
                + "&client_id=" + clientId
                + "&username=" + email
                + "&client_secret=" + clientSecret // <-- Add this line if needed
                + "&password=" + password;

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        ResponseEntity<KeycloakTokenResponse> response = restTemplate.postForEntity(url, entity,
                KeycloakTokenResponse.class);
        return response.getBody();
    }

    // 3. Get admin access token
    public String getAdminAccessToken() {
        String url = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "grant_type=client_credentials"
                + "&client_id=" + clientId + "&client_secret=" + clientSecret; // <-- Add this line
        ;

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

        Map res = response.getBody();
        if (res != null) {
            return (String) res.get("access_token");
        }
        return null;
    }

    // 4. Mark email as verified
    public void markEmailVerified(String email) {
        // Find user by email
        String adminToken = getAdminAccessToken();
        String searchUrl = keycloakUrl + "/admin/realms/" + realm + "/users?email=" + email;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        ResponseEntity<Object[]> searchResponse = restTemplate.exchange(searchUrl, HttpMethod.GET, entity,
                Object[].class);
        if (searchResponse.getBody() == null || searchResponse.getBody().length == 0)
            return;
        Map user = (Map) searchResponse.getBody()[0];
        String userId = (String) user.get("id");

        // Update user: set emailVerified=true
        String updateUrl = keycloakUrl + "/admin/realms/" + realm + "/users/" + userId;
        Map<String, Object> update = Map.of("emailVerified", true);
        HttpEntity<Map<String, Object>> updateEntity = new HttpEntity<>(update, headers);
        restTemplate.put(updateUrl, updateEntity);
    }

    public void updateUserPasswordInKeycloak(String email, String newPassword) {
        String adminToken = getAdminAccessToken();

        // Find user by email
        String searchUrl = keycloakUrl + "/admin/realms/" + realm + "/users?email=" + email;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        ResponseEntity<Object[]> searchResponse = restTemplate.exchange(searchUrl, HttpMethod.GET, entity,
                Object[].class);
        if (searchResponse.getBody() == null || searchResponse.getBody().length == 0) {
            throw new RuntimeException("User not found in Keycloak");
        }
        Map user = (Map) searchResponse.getBody()[0];
        String userId = (String) user.get("id");

        // Update password
        String passwordUrl = keycloakUrl + "/admin/realms/" + realm + "/users/" + userId + "/reset-password";
        Map<String, Object> passwordPayload = Map.of(
                "type", "password",
                "value", newPassword,
                "temporary", false);
        HttpEntity<Map<String, Object>> passwordEntity = new HttpEntity<>(passwordPayload, headers);
        restTemplate.put(passwordUrl, passwordEntity);
    }

    public KeycloakTokenResponse refreshAccessToken(HttpServletRequest request) {
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshtoken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }
        if (refreshToken == null) {
            throw new AccessDenied();
        }
        String url = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "grant_type=refresh_token"
                + "&client_id=" + clientId
                + "&client_secret=" + clientSecret
                + "&refresh_token=" + refreshToken;

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        ResponseEntity<KeycloakTokenResponse> response = restTemplate.postForEntity(url, entity,
                KeycloakTokenResponse.class);
        return response.getBody();

    }

}