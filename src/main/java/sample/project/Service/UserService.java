package sample.project.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import sample.project.Auth.JwtService;
import sample.project.DTO.request.ChangePasswordRequest;
import sample.project.DTO.request.LoginRequest;
import sample.project.DTO.request.RegisterRequest;
import sample.project.DTO.response.AgentResponse;
import sample.project.DTO.response.CompanyResponse;
import sample.project.DTO.response.LoginResponse;
import sample.project.DTO.response.ServiceResponse;
import sample.project.DTO.response.UserResponse;
import sample.project.DTO.response.UserResponseList;
import sample.project.ErrorHandling.Exception.AccessDenied;
import sample.project.ErrorHandling.Exception.ObjectNotFound;
import sample.project.Model.Agent;
import sample.project.Model.Company;
import sample.project.Model.Cv;
import sample.project.Model.Role;
import sample.project.Model.User;
import sample.project.Repo.UserRepo;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CompanyService companyService;
    private final AgentService agentService;
    private final CloudinaryService cloudinaryService;
    private final EmailService emailService;

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
        User user = new User();
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPhonenumber(req.getPhonenumber());
        user.setBirthdate(req.getBirthdate());
        user.setPassword(passwordEncoder.encode(req.getPassword()));

        if ((req.getRole().equals("COMPANY")) || (req.getRole().equals("AGENT"))) {
            user.setRole(Role.valueOf(req.getRole()));
        }
        if (req.getRole().equals("COMPANY")) {
            Company company = companyService.findOrCreateCompany(req.getCompanyName(), req.getCompanyPhonenumber());
            user.setCompany(company);
        }
        String code = String.format("%06d", new java.util.Random().nextInt(999999));
        user.setVerificationCode(code);
        user.setEmailVerificationExpiry(LocalDateTime.now().plusMinutes(15));
        user.setEmailVerified(false);
        emailService.sendMessage(user.getEmail(), "Sira website verification code", "Your verification code " + code);
        User savedUser = userRepo.save(user);
        if (req.getRole().toString().equals("AGENT")) {
            agentService.addAgent(savedUser);
        }
        return new ServiceResponse<String>(true, null, "Register Successful");
    }

    public UserResponse getUser(Long id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new ObjectNotFound("User", "id"));

        String companyName = null;
        String companyPhonenumber = null;
        Long companyID = null;
        Cv cv = null;
        if (user.getCompany() != null) {
            Company company = user.getCompany();
            companyName = company.getName();
            companyPhonenumber = company.getPhoneNumber();
            companyID = company.getId();
            return CompanyResponse.builder()
                    .id(user.getId())
                    .birthdate(user.getBirthdate())
                    .email(user.getEmail())
                    .companyId(companyID)
                    .companyName(companyName)
                    .companyPhonenumber(companyPhonenumber)
                    .pfp(user.getPfpUrl())
                    .phonenumber(user.getPhonenumber())
                    .name(user.getName())
                    .role(user.getRole())
                    .build();
        }

        else if (user.getAgent() != null) {
            Agent agent = user.getAgent();
            cv = agent.getCv();
            return AgentResponse.builder()
                    .id(agent.getId())
                    .birthdate(user.getBirthdate())
                    .email(user.getEmail())
                    .phonenumber(user.getPhonenumber())
                    .name(user.getName())
                    .pfp(user.getPfpUrl())
                    .role(user.getRole())
                    .cv(cv)
                    .build();

        } else if (user.getRole().toString().equals("ADMIN")) {
            return UserResponse.builder()
                    .id(user.getId())
                    .name(user.getName())
                    .email(user.getEmail())
                    .phonenumber(user.getPhonenumber())
                    .birthdate(user.getBirthdate())
                    .pfp(user.getPfpUrl())
                    .role(user.getRole())
                    .build();
        } else {
            throw new AccessDenied();
        }

    }

    public ServiceResponse<LoginResponse> login(LoginRequest req) {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(req.email(), req.password()));

        if (authentication.isAuthenticated()) {
            Optional<User> optionalUser = getUserByEmail(req.email());
            if (!optionalUser.isPresent()) {
                return new ServiceResponse<>(false, "User not found", null);
            }
            User user = optionalUser.get();

            if (!user.isEnabled()) {
                resendCode(req.email());
                return new ServiceResponse<>(false, "Email not verified", null);
            }
            String accessToken = jwtService.generateToken(user, 1);
            String refreshToken = jwtService.generateToken(user, 24);
            UserResponse resp;
            if (user.getRole().toString().equals("AGENT")) {
                resp = AgentResponse.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .phonenumber(user.getPhonenumber())
                        .birthdate(user.getBirthdate())
                        .pfp(user.getPfpUrl())
                        .role(user.getRole())
                        .cv(user.getAgent() != null ? user.getAgent().getCv() : null)
                        .build();
            } else if (user.getRole().toString().equals("COMPANY")) {
                resp = CompanyResponse.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .phonenumber(user.getPhonenumber())
                        .birthdate(user.getBirthdate())
                        .pfp(user.getPfpUrl())
                        .role(user.getRole())
                        .companyId(user.getCompany() != null ? user.getCompany().getId() : null)
                        .companyName(user.getCompany() != null ? user.getCompany().getName() : null)
                        .companyPhonenumber(
                                user.getCompany() != null ? user.getCompany().getPhoneNumber() : null)
                        .build();
            } else {
                resp = UserResponse.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .phonenumber(user.getPhonenumber())
                        .birthdate(user.getBirthdate())
                        .pfp(user.getPfpUrl())
                        .role(user.getRole())
                        .build();
            }

            LoginResponse response = new LoginResponse(user.getId(), accessToken, refreshToken, resp,
                    "Login Successfull");
            return new ServiceResponse<>(true, "", response);

        } else {
            return new ServiceResponse<>(false, "Login Failed", null);
        }
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

        // check if another user with the same credentials as the update already exsists
        // in the database.

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
        if (req.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(req.getPassword()));
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

        return new ServiceResponse<UserResponse>(true, "", res.getData());

    }

    public ServiceResponse<String> deleteUser(Long id) {
        Optional<User> optionalUser = userRepo.findById(id);
        if (!optionalUser.isPresent()) {
            return new ServiceResponse<String>(false, "User not found", null);
        }

        User user = optionalUser.get();
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
        if (!passwordEncoder.matches(req.oldPassword(), user.getPassword())) {
            return new ServiceResponse<String>(false, "Credentials not correct", null);
        }
        user.setPassword(passwordEncoder.encode(req.newPassword()));
        return new ServiceResponse<String>(true, "Password updated", null);

    }

    @Transactional
    public ServiceResponse<LoginResponse> verifyEmail(String code, String email) {

        Optional<User> opUser = userRepo.findByEmail(email);
        if (!opUser.isPresent()) {
            return new ServiceResponse<LoginResponse>(false, "User not found", null);
        }

        User user = opUser.get();

        if (!code.equals(user.getVerificationCode()) ||
                user.getEmailVerificationExpiry().isBefore(LocalDateTime.now())) {
            return new ServiceResponse<LoginResponse>(false, "Invalid Code", null);
        }
        if (!user.isEnabled()) {
            user.setEmailVerified(true);
            user.setVerificationCode(null);
            user.setEmailVerificationExpiry(null);

        }

        String accessToken = jwtService.generateToken(user, 1);
        String refreshToken = jwtService.generateToken(user, 1);

        ServiceResponse<UserResponse> res = generateResponse(user);

        if (!res.isSuccess()) {
            return new ServiceResponse<LoginResponse>(false, "Verification failed", null);

        }

        LoginResponse response = new LoginResponse(user.getId(), accessToken, refreshToken, res.getData(), "success");
        return new ServiceResponse<LoginResponse>(true, "", response);

    }

    @Transactional
    public ServiceResponse<String> resendCode(String email) {
        Optional<User> opUser = userRepo.findByEmail(email);
        if (!opUser.isPresent()) {
            return new ServiceResponse<String>(false, "User not found", null);
        }

        User user = opUser.get();

        String code = String.format("%06d", new java.util.Random().nextInt(999999));
        user.setVerificationCode(code);
        user.setEmailVerificationExpiry(LocalDateTime.now().plusMinutes(15));
        user.setEmailVerified(false);
        userRepo.save(user);
        emailService.sendMessage(user.getEmail(),
                "Sira website verification code", "Your verification code " + code);

        return new ServiceResponse<String>(true, "Code resent to email account",
                null);

    }

    public ServiceResponse<LoginResponse> refreshToken(User user) {

        String accessToken = jwtService.generateToken(user, 1);
        String refreshToken = jwtService.generateToken(user, 400);

        ServiceResponse<UserResponse> res = generateResponse(user);

        if (!res.isSuccess()) {
            return new ServiceResponse<LoginResponse>(false, "Refresh not successful", null);
        }
        LoginResponse regres = new LoginResponse(user.getId(), accessToken, refreshToken, res.getData(), "success");

        return new ServiceResponse<LoginResponse>(true, "", regres);
    }

}
