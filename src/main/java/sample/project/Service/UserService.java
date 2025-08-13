package sample.project.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
import sample.project.DTO.response.RegisterResponse;
import sample.project.DTO.response.UserResponse;
import sample.project.DTO.response.UserResponseList;
import sample.project.ErrorHandling.Exception.AccessDenied;
import sample.project.ErrorHandling.Exception.ObjectAlreadyExists;
import sample.project.ErrorHandling.Exception.ObjectNotFound;
import sample.project.ErrorHandling.Exception.RequiredFieldsEmpty;
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

    @Transactional
    public RegisterResponse createUser(RegisterRequest req) {

        if ((!req.getRole().equals("COMPANY")) && (!req.getRole().equals("AGENT"))) {
            throw new AccessDenied();
        }
        if (req.getRole().equals("COMPANY")) {
            if (req.getCompanyName() == null) {
                throw new RequiredFieldsEmpty("Company", Collections.singletonList("Company name"));

            } else if (req.getCompanyPhonenumber() == null) {
                throw new RequiredFieldsEmpty("Company", Collections.singletonList("Company Phonenumber"));

            }
        }

        Optional<User> exsistingUserEmail = getUserByEmail(req.getEmail());
        if (exsistingUserEmail.isPresent()) {
            throw new ObjectAlreadyExists("User", "email");
        }
        Optional<User> exsistingUserPhonenumber = getUserByPhonenumber(req.getPhonenumber());
        if (exsistingUserPhonenumber.isPresent()) {
            throw new ObjectAlreadyExists("User", "phonenumber");
        }

        User user = new User();
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPhonenumber(req.getPhonenumber());
        user.setBirthdate(req.getBirthdate());
        user.setPassword(req.getPassword());
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if ((req.getRole().equals("COMPANY")) || (req.getRole().equals("AGENT"))) {
            user.setRole(Role.valueOf(req.getRole()));
        }
        if (req.getRole().equals("COMPANY")) {
            Company company = companyService.findOrCreateCompany(req.getCompanyName(), req.getCompanyPhonenumber());
            user.setCompany(company);
        }
        User savedUser = userRepo.save(user);
        if (req.getRole().equals("AGENT")) {
            agentService.addAgent(savedUser);
        }

        String token = jwtService.generateToken(user);
        UserResponse resp;
        if (savedUser.getRole().toString().equals("AGENT")) {
            resp = AgentResponse.builder()
                    .id(savedUser.getId())
                    .name(savedUser.getName())
                    .email(savedUser.getEmail())
                    .phonenumber(savedUser.getPhonenumber())
                    .birthdate(savedUser.getBirthdate())
                    .pfp(savedUser.getPfpUrl())
                    .role(savedUser.getRole())
                    .cv(savedUser.getAgent() != null ? savedUser.getAgent().getCv() : null)
                    .build();
        } else if (savedUser.getRole().toString().equals("COMPANY")) {
            resp = CompanyResponse.builder()
                    .id(savedUser.getId())
                    .name(savedUser.getName())
                    .email(savedUser.getEmail())
                    .phonenumber(savedUser.getPhonenumber())
                    .birthdate(savedUser.getBirthdate())
                    .pfp(savedUser.getPfpUrl())
                    .role(savedUser.getRole())
                    .companyId(savedUser.getCompany() != null ? savedUser.getCompany().getId() : null)
                    .companyName(savedUser.getCompany() != null ? savedUser.getCompany().getName() : null)
                    .companyPhonenumber(
                            savedUser.getCompany() != null ? savedUser.getCompany().getPhoneNumber() : null)
                    .build();
        } else {
            resp = UserResponse.builder()
                    .id(savedUser.getId())
                    .name(savedUser.getName())
                    .email(savedUser.getEmail())
                    .phonenumber(savedUser.getPhonenumber())
                    .birthdate(savedUser.getBirthdate())
                    .pfp(savedUser.getPfpUrl())
                    .role(savedUser.getRole())
                    .build();
        }

        return new RegisterResponse(savedUser.getId(), token, resp);
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

    public LoginResponse login(LoginRequest req) {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(req.email(), req.password()));

        if (authentication.isAuthenticated()) {
            Optional<User> optionalUser = getUserByEmail(req.email());
            if (!optionalUser.isPresent()) {
                return new LoginResponse(null, null, null);
            }
            User user = optionalUser.get();
            String token = jwtService.generateToken(user);
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
            return new LoginResponse(user.getId(), token, resp);

        } else {
            return new LoginResponse(null, null, null);
        }
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
            System.out.println(search);
            users.removeIf(user -> !(user.getRole().toString().toLowerCase().contains(role.toLowerCase())));

        }

        List<UserResponse> responseList = new ArrayList<>();

        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            String companyName = null;
            String companyPhonenumber = null;
            Cv cv = null;
            Long companyID = null;
            if (user.getCompany() != null) {
                Company company = user.getCompany();
                companyName = company.getName();
                companyPhonenumber = company.getPhoneNumber();
                companyID = company.getId();
                UserResponse response = CompanyResponse.builder()
                        .id(user.getId())
                        .birthdate(user.getBirthdate())
                        .email(user.getEmail())
                        .phonenumber(user.getPhonenumber())
                        .name(user.getName())
                        .role(user.getRole())
                        .pfp(user.getPfpUrl())
                        .companyId(companyID)
                        .companyName(companyName)
                        .pfp(user.getPfpUrl())
                        .companyPhonenumber(companyPhonenumber)
                        .build();
                responseList.add(response);

            }

            else if (user.getAgent() != null) {
                Agent agent = user.getAgent();
                cv = agent.getCv();
                UserResponse response = AgentResponse.builder()
                        .id(agent.getId())
                        .birthdate(user.getBirthdate())
                        .email(user.getEmail())
                        .phonenumber(user.getPhonenumber())
                        .name(user.getName())
                        .role(user.getRole())
                        .pfp(user.getPfpUrl())
                        .cv(cv)
                        .build();
                responseList.add(response);
            } else if (user.getRole().toString().equals("ADMIN")) {

                UserResponse response = new UserResponse(user.getId(), user.getName(), user.getEmail(),
                        user.getPhonenumber(), user.getBirthdate(),
                        user.getRole(), user.getPfpUrl());

                responseList.add(response);
            }

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

        // check if another user with the same credentials as the update already exsists
        // in the database.

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
        if (req.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(req.getPassword()));
        }
        if (req.getBirthdate() != null) {
            user.setBirthdate(req.getBirthdate());
        }
        if (req.getPhonenumber() != null) {
            user.setPhonenumber(req.getPhonenumber());
        }

        String companyName = null;
        String companyPhonenumber = null;
        Cv cv = null;
        Long companyID = null;
        if (user.getCompany() != null) {
            Company company = user.getCompany();
            companyName = company.getName();
            companyPhonenumber = company.getPhoneNumber();
            companyID = company.getId();
            return CompanyResponse.builder()
                    .id(user.getId())
                    .birthdate(user.getBirthdate())
                    .email(user.getEmail())
                    .phonenumber(user.getPhonenumber())
                    .name(user.getName())
                    .role(user.getRole())
                    .pfp(user.getPfpUrl())
                    .companyId(companyID)
                    .companyName(companyName)
                    .companyPhonenumber(companyPhonenumber)
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
                    .role(user.getRole())
                    .pfp(user.getPfpUrl())
                    .cv(cv)
                    .build();
        } else if (user.getRole().toString().equals("ADMIN")) {
            return UserResponse.builder().id(user.getId()).birthdate(user.getBirthdate()).email(user.getEmail())
                    .phonenumber(user.getPhonenumber()).name(user.getName()).role(user.getRole()).build();
        } else {
            throw new AccessDenied();
        }

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
            cloudinaryService.deleteFile(oldPfpUrl);
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
        if (!passwordEncoder.matches(req.oldPassword(), user.getPassword())) {
            throw new AccessDenied();
        }
        user.setPassword(passwordEncoder.encode(req.newPassword()));

    }

}
