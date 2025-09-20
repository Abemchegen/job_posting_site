package sample.project.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import sample.project.DTO.request.ChangePasswordRequest;
import sample.project.DTO.request.LoginRequest;
import sample.project.DTO.request.RegisterRequest;
import sample.project.DTO.response.AgentResponse;
import sample.project.DTO.response.CompanyResponse;
import sample.project.DTO.response.KeycloakTokenResponse;
import sample.project.DTO.response.LoginResponse;
import sample.project.DTO.response.ServiceResponse;
import sample.project.DTO.response.UserResponse;
import sample.project.DTO.response.UserResponseList;
import sample.project.Model.Agent;
import sample.project.Model.Company;
import sample.project.Model.Role;
import sample.project.Model.User;
import sample.project.Repo.UserRepo;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepo userRepo;
    private final KeycloakService keycloakService;
    private final CompanyService companyService;
    private final AgentService agentService;
    private final CloudinaryService cloudinaryService;
    private final JwtDecoder jwtDecoder;

    public static ServiceResponse<UserResponse> generateResponse(User user) {
        if (user.getCompany() != null) {
            Company company = user.getCompany();
            CompanyResponse res = CompanyResponse.builder()
                    .id(user.getId())
                    .birthdate(user.getBirthdate())
                    .email(user.getEmail())
                    .phonenumber(user.getPhonenumber())
                    .name(user.getName())
                    .role(user.getRole())
                    .pfp(user.getPfpUrl())
                    .companyId(company.getId())
                    .companyName(company.getName())
                    .companyPhonenumber(company.getPhoneNumber())
                    .build();

            return new ServiceResponse<UserResponse>(true, null, res);

        } else if (user.getAgent() != null) {
            Agent agent = user.getAgent();

            AgentResponse res = AgentResponse.builder()
                    .id(agent.getId())
                    .birthdate(user.getBirthdate())
                    .email(user.getEmail())
                    .phonenumber(user.getPhonenumber())
                    .name(user.getName())
                    .role(user.getRole())
                    .pfp(user.getPfpUrl())
                    .cv(agent.getCv())
                    .build();
            return new ServiceResponse<UserResponse>(true, null, res);

        } else if (user.getRole().toString().equals("ADMIN")) {
            UserResponse res = UserResponse.builder().id(user.getId()).birthdate(user.getBirthdate())
                    .email(user.getEmail())
                    .phonenumber(user.getPhonenumber()).name(user.getName()).role(user.getRole()).build();
            return new ServiceResponse<UserResponse>(true, null, res);

        } else {
            return new ServiceResponse<UserResponse>(false, "Access Denied", null);

        }
    }

    @Transactional
    public ServiceResponse<String> createUser(RegisterRequest req) {
        String role = req.getRole();

        if (!List.of("COMPANY", "AGENT").contains(role)) {
            return new ServiceResponse<>(false, "Access Denied", null);
        }

        Optional<User> euser = getUserByEmail(req.getEmail());

        if (euser.isPresent()) {
            return new ServiceResponse<>(false, "User by this email already exists", null);
        }
        Optional<User> puser = getUserByPhonenumber(req.getPhonenumber());
        if (puser.isPresent()) {
            return new ServiceResponse<>(false, "User by this phone number already exists", null);
        }

        ServiceResponse<String> res = keycloakService.createUserInKeycloak(req.getEmail(), req.getName(),
                req.getPassword(), req.getRole());

        if (!res.isSuccess()) {
            return new ServiceResponse<String>(false, res.getMessage(), null);
        }

        keycloakService.sendVerificationEmail(req.getEmail());

        User user = new User();
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPhonenumber(req.getPhonenumber());
        user.setBirthdate(req.getBirthdate());
        user.setRole(Role.valueOf(req.getRole()));

        if ("COMPANY".equals(req.getRole())) {
            Company company = companyService.findOrCreateCompany(req.getCompanyName(), req.getCompanyPhonenumber());
            user.setCompany(company);
        }
        // String code = String.format("%06d", new Random().nextInt(999999));
        // user.setVerificationCode(code);
        // user.setEmailVerificationExpiry(LocalDateTime.now().plusMinutes(15));

        user.setEmailVerified(false);

        // emailService.sendMessage(user.getEmail(), "Sira website verification code",
        // "Your verification code: " + code);

        User savedUser = userRepo.save(user);

        if ("AGENT".equals(role)) {
            agentService.addAgent(savedUser);
        }
        return new ServiceResponse<String>(true, null, "Register Successful");
    }

    public ServiceResponse<UserResponse> getUser(String email) {
        Optional<User> user = userRepo.findByEmail(email);

        if (!user.isPresent()) {
            return new ServiceResponse<>(false, "User not found", null);
        }

        ServiceResponse<UserResponse> res = generateResponse(user.get());

        if (!res.isSuccess()) {
            return new ServiceResponse<>(false, res.getMessage(), null);
        }

        return new ServiceResponse<>(true, "", res.getData());

    }

    public ServiceResponse<UserResponse> getUser(long id) {
        Optional<User> user = userRepo.findById(id);
        if (!user.isPresent()) {
            return new ServiceResponse<>(false, "User not found", null);
        }
        ServiceResponse<UserResponse> res = generateResponse(user.get());
        if (!res.isSuccess()) {
            return new ServiceResponse<>(false, res.getMessage(), null);
        }
        return new ServiceResponse<>(true, "", res.getData());
    }

    public ServiceResponse<LoginResponse> login(LoginRequest req) {

        Optional<User> optionalUser = getUserByEmail(req.email());
        if (!optionalUser.isPresent()) {
            return new ServiceResponse<>(false, "User not found", null);
        }
        User user = optionalUser.get();

        if (!user.isEmailVerified()) {
            boolean check = keycloakService.isEmailVerifiedInKeycloak(req.email());
            if (!check) {
                return new ServiceResponse<>(false, "Email not verified", null);
            } else {
                user.setEmailVerified(true);
            }
        }

        KeycloakTokenResponse tokenResponse = keycloakService.getTokenByPassword(req.email(),
                req.password());

        if (tokenResponse == null || tokenResponse.getAccess_token() == null) {
            return new ServiceResponse<>(false, "Invalid Credentials", null);
        }

        String accessToken = tokenResponse.getAccess_token();
        String refreshToken = tokenResponse.getRefresh_token();
        ServiceResponse<UserResponse> res = generateResponse(user);
        if (!res.isSuccess()) {
            return new ServiceResponse<>(false, res.getMessage(), null);
        }
        LoginResponse response = new LoginResponse(user.getId(), accessToken, refreshToken, res.getData(),
                "Login Successfull");
        return new ServiceResponse<>(true, "", response);

    }

    public ServiceResponse<UserResponseList> getAllUser(String role, String search) {
        List<User> users = userRepo.findAll();

        if (search != null) {
            System.out.println(search);
            users.removeIf(user -> !(user.getName().toLowerCase().contains(search.toLowerCase()) ||
                    (user.getCompany() != null
                            && user.getCompany().getName().toLowerCase().contains(search.toLowerCase()))));

        }
        if (role != null) {
            System.out.println(role);
            users.removeIf(user -> !(user.getRole().toString().toLowerCase().equals(role.toLowerCase())));

        }

        List<UserResponse> responseList = new ArrayList<>();

        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            ServiceResponse<UserResponse> response = generateResponse(user);
            if (!response.isSuccess()) {
                return new ServiceResponse<>(false, response.getMessage(), null);
            }
            responseList.add(response.getData());
        }

        UserResponseList list = new UserResponseList(responseList);
        return new ServiceResponse<>(true, "", list);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepo.findByEmail(email);

    }

    public Optional<User> getUserByPhonenumber(String phonenumber) {
        return userRepo.findByPhonenumber(phonenumber);
    }

    @Transactional
    public ServiceResponse<UserResponse> updateUser(RegisterRequest req, Long id) {

        Optional<User> opuser = userRepo.findById(id);

        if (!opuser.isPresent()) {
            return new ServiceResponse<UserResponse>(false, "User not found", null);
        }

        Optional<User> exsistingUserEmail = getUserByEmail(req.getEmail());
        if (exsistingUserEmail.isPresent() && !exsistingUserEmail.get().getEmail().equals(opuser.get().getEmail())) {
            return new ServiceResponse<UserResponse>(false, "User with this email exists", null);
        }

        Optional<User> exsistingUserPhonenumber = getUserByPhonenumber(req.getPhonenumber());
        if (exsistingUserPhonenumber.isPresent()
                && !exsistingUserPhonenumber.get().getPhonenumber().equals(opuser.get().getPhonenumber())) {
            return new ServiceResponse<UserResponse>(false, "User with this phone number exists", null);
        }

        User user = opuser.get();

        if (req.getName() != null) {
            user.setName(req.getName());
        }
        if (req.getEmail() != null) {
            user.setEmail(req.getEmail());
        }

        if (req.getBirthdate() != null) {
            user.setBirthdate(req.getBirthdate());
        }
        if (req.getPhonenumber() != null) {
            user.setPhonenumber(req.getPhonenumber());
        }

        ServiceResponse<UserResponse> res = generateResponse(user);
        if (!res.isSuccess()) {
            return new ServiceResponse<>(false, res.getMessage(), null);
        }
        System.out.println("finally");
        System.out.println(user.getName());

        return new ServiceResponse<UserResponse>(true, "", res.getData());

    }

    public ServiceResponse<String> deleteUser(Long id) {
        Optional<User> optionalUser = userRepo.findById(id);
        if (!optionalUser.isPresent()) {
            return new ServiceResponse<String>(false, "User not found", null);
        }

        User user = optionalUser.get();
        keycloakService.deleteUserFromKeycloak(user.getEmail());
        if (user.getAgent() != null) {
            agentService.deleteAgent(id);
        }

        userRepo.deleteById(id);
        return new ServiceResponse<String>(true, "User Deleted", null);

    }

    @Transactional
    public ServiceResponse<String> uploadProfileImage(long id, MultipartFile file) {
        Optional<User> optionalUser = userRepo.findById(id);
        if (!optionalUser.isPresent()) {
            return new ServiceResponse<String>(false, "User not found", null);
        }
        User user = optionalUser.get();
        String oldPfpUrl = user.getPfpUrl();
        if (oldPfpUrl != null && !oldPfpUrl.isEmpty()) {

            try {
                cloudinaryService.deleteFile(oldPfpUrl, true);

            } catch (Exception e) {
                System.out.println(e.getMessage());
                return new ServiceResponse<String>(false, "profile picture delete failed, image not uploaded. ", null);
            }
        }

        String pfp;
        try {
            pfp = cloudinaryService.uploadFile(file, true);
        } catch (IOException e) {
            return new ServiceResponse<String>(false, "image uploaded failed.", null);
        }
        user.setPfpUrl(pfp);
        return new ServiceResponse<String>(true, "", pfp);

    }

    @Transactional
    public ServiceResponse<String> deletePfp(long userid) {
        Optional<User> optionalUser = userRepo.findById(userid);
        if (!optionalUser.isPresent()) {
            return new ServiceResponse<String>(false, "User not found", null);
        }
        User user = optionalUser.get();
        user.setPfpUrl(null);
        return new ServiceResponse<String>(true, "pfp deleted", null);

    }

    @Transactional
    public ServiceResponse<String> updateUserPassword(ChangePasswordRequest req, long userid) {
        Optional<User> optionalUser = userRepo.findById(userid);
        if (!optionalUser.isPresent()) {
            return new ServiceResponse<String>(false, "User not found", null);
        }
        User user = optionalUser.get();
        keycloakService.updateUserPasswordInKeycloak(user.getEmail(), req.newPassword());
        return new ServiceResponse<String>(true, "Password updated successfully", null);

    }

    // @Transactional
    // public ServiceResponse<String> resendCode(String email) {
    // Optional<User> opUser = userRepo.findByEmail(email);
    // if (!opUser.isPresent()) {
    // return new ServiceResponse<String>(false, "User not found", null);
    // }

    // User user = opUser.get();

    // String code = String.format("%06d", new java.util.Random().nextInt(999999));
    // user.setVerificationCode(code);
    // user.setEmailVerificationExpiry(LocalDateTime.now().plusMinutes(15));
    // user.setEmailVerified(false);
    // userRepo.save(user);
    // emailService.sendMessage(user.getEmail(),
    // "Sira website verification code", "Your verification code " + code);

    // return new ServiceResponse<String>(true, "Code resent to email account",
    // null);

    // }

    public ServiceResponse<LoginResponse> refreshToken(HttpServletRequest request) {

        ServiceResponse<KeycloakTokenResponse> newTokens = keycloakService.refreshAccessToken(request);
        if (!newTokens.isSuccess()) {
            return new ServiceResponse<LoginResponse>(false, newTokens.getMessage(), null);
        }
        Jwt jwt = jwtDecoder.decode(newTokens.getData().getAccess_token());
        String email = jwt.getClaim("email");
        Optional<User> optionalUser = getUserByEmail(email);
        if (!optionalUser.isPresent()) {
            return new ServiceResponse<LoginResponse>(false, "User not found", null);
        }
        User user = optionalUser.get();
        ServiceResponse<UserResponse> userResponse = generateResponse(user);

        if (!userResponse.isSuccess()) {
            return new ServiceResponse<LoginResponse>(false, userResponse.getMessage(), null);
        }
        LoginResponse res = new LoginResponse(user.getId(), newTokens.getData().getAccess_token(),
                newTokens.getData().getRefresh_token(),
                userResponse.getData(),
                "Token refreshed");

        return new ServiceResponse<LoginResponse>(true, "", res);

    }

}
