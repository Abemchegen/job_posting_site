package sample.project.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import sample.project.Auth.JwtService;
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

    @Transactional
    public RegisterResponse createUser(RegisterRequest req) {

        if ((!req.getRole().equals("COMPANY")) || (!req.getRole().equals("AGENT"))) {
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
        Optional<User> exsistingUserUsername = getUserByUsername(req.getUsername());
        if (exsistingUserUsername.isPresent()) {
            throw new ObjectAlreadyExists("User", "username");
        }

        User user = new User();
        user.setName(req.getName());
        user.setUsername(req.getUsername());
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

        return new RegisterResponse(savedUser.getId(), savedUser.getUsername(), token);
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
                    .phonenumber(user.getPhonenumber())
                    .name(user.getName())
                    .username(user.getUsername())
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
                    .username(user.getUsername())
                    .role(user.getRole())
                    .cv(cv)
                    .build();

        } else {
            throw new AccessDenied();
        }

    }

    public LoginResponse login(LoginRequest req) {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(req.username(), req.password()));

        if (authentication.isAuthenticated()) {
            Optional<User> user = getUserByUsername(req.username());
            if (!user.isPresent()) {
                return new LoginResponse("Login Failed.", null);
            }
            String token = jwtService.generateToken(user.get());
            return new LoginResponse("Login Successful.", token);

        } else {
            return new LoginResponse("Login Failed.", null);
        }
    }

    public UserResponseList getAllUser() {
        List<User> users = userRepo.findAll();

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
                        .username(user.getUsername())
                        .role(user.getRole())
                        .companyId(companyID)
                        .companyName(companyName)
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
                        .username(user.getUsername())
                        .role(user.getRole())
                        .cv(cv)
                        .build();
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

    public Optional<User> getUserByUsername(String username) {
        return userRepo.findByUsername(username);
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
                && !exsistingUserEmail.get().getPhonenumber().equals(user.getPhonenumber())) {
            throw new ObjectAlreadyExists("User", "phonenumber");
        }
        Optional<User> exsistingUserUsername = getUserByUsername(req.getUsername());
        if (exsistingUserUsername.isPresent() && !exsistingUserEmail.get().getUsername().equals(user.getUsername())) {
            throw new ObjectAlreadyExists("User", "username");
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
        if (req.getUsername() != null) {
            user.setUsername(req.getUsername());
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
                    .username(user.getUsername())
                    .role(user.getRole())
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
                    .username(user.getUsername())
                    .role(user.getRole())
                    .cv(cv)
                    .build();
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
}
