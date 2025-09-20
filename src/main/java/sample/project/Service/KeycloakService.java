package sample.project.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import sample.project.DTO.response.KeycloakTokenResponse;
import sample.project.DTO.response.ServiceResponse;

import java.util.List;
import java.util.Map;

@Service
public class KeycloakService {

    @Value("${keycloak.auth-server-url}")
    private String keycloakUrl;
    @Value("${keycloak.realm}")
    private String realm;
    @Value("${keycloak.client}")
    private String clientId;
    @Value("${keycloak.credentials.secret}")
    private String clientSecret;
    private final RestTemplate restTemplate = new RestTemplate();

    public ServiceResponse<String> createUserInKeycloak(String email, String name, String password, String role) {
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
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        } catch (org.springframework.web.client.HttpClientErrorException.Conflict ex) {
            String errorMsg = ex.getResponseBodyAsString();
            String userMsg = errorMsg.contains("errorMessage")
                    ? errorMsg.replaceAll(".*\"errorMessage\"\\s*:\\s*\"([^\"]+)\".*", "$1")
                    : "User already exists";
            return new ServiceResponse<String>(false, userMsg, null);
        }

        String searchUrl = keycloakUrl + "/admin/realms/" + realm + "/users?email=" + email;
        HttpEntity<?> searchEntity = new HttpEntity<>(headers);
        ResponseEntity<Object[]> searchResponse = restTemplate.exchange(searchUrl, HttpMethod.GET, searchEntity,
                Object[].class);
        if (searchResponse.getBody() == null || searchResponse.getBody().length == 0) {
            return new ServiceResponse<String>(false, "User not found", null);
        }

        Map userObj = (Map) searchResponse.getBody()[0];
        String userId = (String) userObj.get("id");

        String rolesUrl = keycloakUrl + "/admin/realms/" + realm + "/roles/" + role;
        ResponseEntity<Map> roleResponse = restTemplate.exchange(rolesUrl, HttpMethod.GET, searchEntity, Map.class);
        Map roleObj = roleResponse.getBody();
        if (roleObj == null || !roleObj.containsKey("id")) {
            return new ServiceResponse<String>(false, "Role not found in Keycloak: " + role, null);
        }
        String roleId = (String) roleObj.get("id");
        String roleName = (String) roleObj.get("name");

        String assignRoleUrl = keycloakUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm";
        List<Map<String, Object>> rolesToAssign = List.of(
                Map.of("id", roleId, "name", roleName));
        HttpEntity<List<Map<String, Object>>> assignEntity = new HttpEntity<>(rolesToAssign, headers);
        restTemplate.postForEntity(assignRoleUrl, assignEntity, String.class);

        return new ServiceResponse<String>(true, "User created", null);

    }

    public KeycloakTokenResponse getTokenByPassword(String email, String password) {
        String url = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "grant_type=password"
                + "&client_id=" + clientId
                + "&username=" + email
                + "&client_secret=" + clientSecret
                + "&password=" + password;

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        ResponseEntity<KeycloakTokenResponse> response = restTemplate.postForEntity(url, entity,
                KeycloakTokenResponse.class);
        return response.getBody();
    }

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

    public void markEmailVerified(String email) {
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

        String updateUrl = keycloakUrl + "/admin/realms/" + realm + "/users/" + userId;
        Map<String, Object> update = Map.of("emailVerified", true);
        HttpEntity<Map<String, Object>> updateEntity = new HttpEntity<>(update, headers);
        restTemplate.put(updateUrl, updateEntity);
    }

    public ServiceResponse<String> updateUserPasswordInKeycloak(String email, String newPassword) {
        String adminToken = getAdminAccessToken();

        String searchUrl = keycloakUrl + "/admin/realms/" + realm + "/users?email=" + email;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        ResponseEntity<Object[]> searchResponse = restTemplate.exchange(searchUrl, HttpMethod.GET, entity,
                Object[].class);
        if (searchResponse.getBody() == null || searchResponse.getBody().length == 0) {
            return new ServiceResponse<String>(false, "User not found in Keycloak", null);
        }
        Map user = (Map) searchResponse.getBody()[0];
        String userId = (String) user.get("id");

        String passwordUrl = keycloakUrl + "/admin/realms/" + realm + "/users/" + userId + "/reset-password";
        Map<String, Object> passwordPayload = Map.of(
                "type", "password",
                "value", newPassword,
                "temporary", false);
        HttpEntity<Map<String, Object>> passwordEntity = new HttpEntity<>(passwordPayload, headers);
        restTemplate.put(passwordUrl, passwordEntity);

        return new ServiceResponse<String>(true, "Password Updated", null);
    }

    public ServiceResponse<KeycloakTokenResponse> refreshAccessToken(HttpServletRequest request) {
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
            return new ServiceResponse<>(false, "Access Denied", null);
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
        return new ServiceResponse<>(true, "", response.getBody());

    }

    public void sendVerificationEmail(String email) {
        String adminToken = getAdminAccessToken();
        String searchUrl = keycloakUrl + "/admin/realms/" + realm + "/users?email=" + email;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        ResponseEntity<Object[]> searchResponse = restTemplate.exchange(searchUrl, HttpMethod.GET, entity,
                Object[].class);
        if (searchResponse.getBody() == null || searchResponse.getBody().length == 0)
            return;
        Map user = (Map) searchResponse.getBody()[0];
        String userId = (String) user.get("id");

        List<String> actions = List.of("VERIFY_EMAIL");
        HttpEntity<List<String>> verifyEntity = new HttpEntity<>(actions, headers);
        String verifyUrl = keycloakUrl + "/admin/realms/" + realm + "/users/" + userId
                + "/execute-actions-email?client_id=" + clientId + "&redirect_uri=http://localhost:5173/login";
        restTemplate.put(verifyUrl, verifyEntity);
    }

    public boolean isEmailVerifiedInKeycloak(String email) {
        String adminToken = getAdminAccessToken();
        String searchUrl = keycloakUrl + "/admin/realms/" + realm + "/users?email=" + email;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<Object[]> searchResponse = restTemplate.exchange(
                searchUrl, HttpMethod.GET, entity, Object[].class);

        if (searchResponse.getBody() == null || searchResponse.getBody().length == 0)
            return false;

        Map<String, Object> user = (Map<String, Object>) searchResponse.getBody()[0];
        Boolean emailVerified = (Boolean) user.get("emailVerified");
        return emailVerified != null && emailVerified;
    }

    public void deleteUserFromKeycloak(String email) {
        String adminToken = getAdminAccessToken();
        String searchUrl = keycloakUrl + "/admin/realms/" + realm + "/users?email=" + email;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<Object[]> searchResponse = restTemplate.exchange(searchUrl, HttpMethod.GET, entity,
                Object[].class);
        if (searchResponse.getBody() == null || searchResponse.getBody().length == 0)
            return;
        Map user = (Map) searchResponse.getBody()[0];
        String userId = (String) user.get("id");

        String deleteUrl = keycloakUrl + "/admin/realms/" + realm + "/users/" + userId;
        restTemplate.exchange(deleteUrl, HttpMethod.DELETE, entity, Void.class);
    }

}