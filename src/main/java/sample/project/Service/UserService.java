package sample.project.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

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
import sample.project.DTO.response.UserResponse;
import sample.project.DTO.response.UserResponseList;
import sample.project.ErrorHandling.Exception.AccessDenied;
import sample.project.ErrorHandling.Exception.ObjectAlreadyExists;
import sample.project.ErrorHandling.Exception.ObjectNotFound;
import sample.project.ErrorHandling.Exception.RequiredFieldsEmpty;
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

    public static UserResponse generateResponse(User user) {
        if (user.getCompany() != null) {
            Company company = user.getCompany();
            return CompanyResponse.builder()
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

        } else if (user.getAgent() != null) {
            Agent agent = user.getAgent();

            return AgentResponse.builder()
                    .id(agent.getId())
                    .birthdate(user.getBirthdate())
                    .email(user.getEmail())
                    .phonenumber(user.getPhonenumber())
                    .name(user.getName())
                    .role(user.getRole())
                    .pfp(user.getPfpUrl())
                    .cv(agent.getCv())
                    .build();
        } else if (user.getRole().toString().equals("ADMIN")) {
            return UserResponse.builder().id(user.getId()).birthdate(user.getBirthdate()).email(user.getEmail())
                    .phonenumber(user.getPhonenumber()).name(user.getName()).role(user.getRole()).build();
        } else {
            throw new AccessDenied();
        }
    }

    private final EmailService emailService;

    @Transactional
    public void createUser(RegisterRequest req) {
        String role = req.getRole();

        if (!List.of("COMPANY", "AGENT").contains(role)) {
            throw new AccessDenied();
        }

        if ("COMPANY".equals(role) && (req.getCompanyName() == null || req.getCompanyPhonenumber() == null)) {
            List<String> missingFields = new ArrayList<>();
            if (req.getCompanyName() == null)
                missingFields.add("Company name");
            if (req.getCompanyPhonenumber() == null)
                missingFields.add("Company Phonenumber");
            throw new RequiredFieldsEmpty("Company", missingFields);
        }

        getUserByEmail(req.getEmail()).ifPresent(u -> {
            throw new ObjectAlreadyExists("User", "email");
        });
        getUserByPhonenumber(req.getPhonenumber()).ifPresent(u -> {
            throw new ObjectAlreadyExists("User", "phonenumber");
        });

        keycloakService.createUserInKeycloak(req.getEmail(), req.getName(), req.getPassword(), req.getRole());

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

        String code = String.format("%06d", new Random().nextInt(999999));
        user.setVerificationCode(code);
        user.setEmailVerificationExpiry(LocalDateTime.now().plusMinutes(15));
        user.setEmailVerified(false);

        emailService.sendMessage(user.getEmail(), "Sira website verification code", "Your verification code: " + code);

        User savedUser = userRepo.save(user);

        if ("AGENT".equals(role)) {
            agentService.addAgent(savedUser);
        }
    }

    public UserResponse getUser(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ObjectNotFound("User", "email"));

        UserResponse res = generateResponse(user);
        return res;

    }

    public UserResponse getUser(long id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new ObjectNotFound("User", "email"));
        UserResponse res = generateResponse(user);
        return res;

    }

    public LoginResponse login(LoginRequest req) {

        Optional<User> optionalUser = getUserByEmail(req.email());
        if (!optionalUser.isPresent()) {
            return new LoginResponse(null, null, null, null, "User doesn't exist");
        }
        User user = optionalUser.get();

        if (!user.isEmailVerified()) {
            resendCode(req.email());
            return new LoginResponse(null, null, null, null, "Please verify your email before logging in");
        }

        KeycloakTokenResponse tokenResponse = keycloakService.getTokenByPassword(req.email(),
                req.password());

        if (tokenResponse == null || tokenResponse.getAccess_token() == null) {
            return new LoginResponse(null, null, null, null, "Invalid credentials");
        }

        String accessToken = tokenResponse.getAccess_token();
        String refreshToken = tokenResponse.getRefresh_token();
        UserResponse res = generateResponse(user);
        return new LoginResponse(user.getId(), accessToken, refreshToken, res, "Login Successfull");

    }

    public UserResponseList getAllUser(String role, String search) {
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
            UserResponse response = generateResponse(user);
            responseList.add(response);
        }

        return new UserResponseList(responseList);

    }

    public Optional<User> getUserByEmail(String email) {
        return userRepo.findByEmail(email);

    }

    public Optional<User> getUserByPhonenumber(String phonenumber) {
        return userRepo.findByPhonenumber(phonenumber);
    }

    @Transactional
    public UserResponse updateUser(RegisterRequest req, Long id) {

        User user = userRepo.findById(id)
                .orElseThrow(() -> new ObjectNotFound("Users", "id"));

        Optional<User> exsistingUserEmail = getUserByEmail(req.getEmail());
        if (exsistingUserEmail.isPresent() && !exsistingUserEmail.get().getEmail().equals(user.getEmail())) {
            throw new ObjectAlreadyExists("User", "email");
        }

        Optional<User> exsistingUserPhonenumber = getUserByPhonenumber(req.getPhonenumber());
        if (exsistingUserPhonenumber.isPresent()
                && !exsistingUserPhonenumber.get().getPhonenumber().equals(user.getPhonenumber())) {
            throw new ObjectAlreadyExists("User", "phonenumber");
        }

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

        UserResponse res = generateResponse(user);
        return res;

    }

    public void deleteUser(Long id) {
        Optional<User> optionalUser = userRepo.findById(id);
        if (!optionalUser.isPresent()) {
            throw new ObjectNotFound("User", "id");
        }

        User user = optionalUser.get();
        if (user.getAgent() != null) {
            agentService.deleteAgent(id);
        }

        userRepo.deleteById(id);

    }

    @Transactional
    public String uploadProfileImage(long id, MultipartFile file) {
        Optional<User> optionalUser = userRepo.findById(id);
        if (!optionalUser.isPresent()) {
            throw new ObjectNotFound("User", "id");
        }
        User user = optionalUser.get();
        String oldPfpUrl = user.getPfpUrl();
        if (oldPfpUrl != null && !oldPfpUrl.isEmpty()) {

            try {
                cloudinaryService.deleteFile(oldPfpUrl, true);

            } catch (Exception e) {
                throw new RuntimeException("profile picture delete failed, image not uploaded" + e.getMessage());

            }
        }

        String pfp;
        try {
            pfp = cloudinaryService.uploadFile(file, true);
        } catch (IOException e) {
            return "";
        }
        user.setPfpUrl(pfp);
        return pfp;

    }

    @Transactional
    public void deletePfp(long userid) {
        Optional<User> optionalUser = userRepo.findById(userid);
        if (!optionalUser.isPresent()) {
            throw new ObjectNotFound("User", "id");
        }
        User user = optionalUser.get();
        user.setPfpUrl(null);
    }

    @Transactional
    public void updateUserPassword(ChangePasswordRequest req, long userid) {
        Optional<User> optionalUser = userRepo.findById(userid);
        if (!optionalUser.isPresent()) {
            throw new ObjectNotFound("User", "id");
        }
        User user = optionalUser.get();
        keycloakService.updateUserPasswordInKeycloak(user.getEmail(), req.newPassword());

    }

    @Transactional
    public void verifyEmail(String code, String email) {

        Optional<User> opUser = userRepo.findByEmail(email);
        if (!opUser.isPresent()) {
            throw new ObjectNotFound("user", "email");
        }

        User user = opUser.get();

        if (!code.equals(user.getVerificationCode()) ||
                user.getEmailVerificationExpiry().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired code");
        }
        if (!user.isEmailVerified()) {
            user.setEmailVerified(true);
            user.setVerificationCode(null);
            user.setEmailVerificationExpiry(null);
        }

        keycloakService.markEmailVerified(user.getEmail());

    }

    @Transactional
    public void resendCode(String email) {
        Optional<User> opUser = userRepo.findByEmail(email);
        if (!opUser.isPresent()) {
            throw new ObjectNotFound("user", "email");
        }

        User user = opUser.get();

        String code = String.format("%06d", new java.util.Random().nextInt(999999));
        user.setVerificationCode(code);
        user.setEmailVerificationExpiry(LocalDateTime.now().plusMinutes(15));
        user.setEmailVerified(false);
        userRepo.save(user);
        emailService.sendMessage(user.getEmail(),
                "Sira website verification code", "Your verification code " + code);
    }

    public LoginResponse refreshToken(HttpServletRequest request) {

        KeycloakTokenResponse newTokens = keycloakService.refreshAccessToken(request);
        Jwt jwt = jwtDecoder.decode(newTokens.getAccess_token());
        String email = jwt.getClaim("email");
        Optional<User> optionalUser = getUserByEmail(email);
        if (!optionalUser.isPresent()) {
            throw new ObjectNotFound("user", "email");
        }
        User user = optionalUser.get();
        UserResponse userResponse = generateResponse(user);
        return new LoginResponse(user.getId(), newTokens.getAccess_token(), newTokens.getRefresh_token(), userResponse,
                "Token refreshed");
    }

}
