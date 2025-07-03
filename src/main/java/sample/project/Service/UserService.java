package sample.project.Service;

import java.util.ArrayList;
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
import sample.project.DTO.response.LoginResponse;
import sample.project.DTO.response.RegisterResponse;
import sample.project.DTO.response.UserResponse;
import sample.project.DTO.response.UserResponseList;
import sample.project.ErrorHandling.Exception.CompanyInformationRequired;
import sample.project.ErrorHandling.Exception.ObjectAlreadyExists;
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

    @Transactional
    public RegisterResponse createUser(RegisterRequest req) {
        if ((req.role().equals("COMPANY")) && (req.companyName() == null || req.companyPhonenumber() == null)) {
            throw new CompanyInformationRequired();
        }
        Optional<User> exsistingUserEmail = getUserByEmail(req.email());
        if (exsistingUserEmail.isPresent()) {
            throw new ObjectAlreadyExists("User", "email");
        }
        Optional<User> exsistingUserPhonenumber = getUserByPhonenumber(req.phonenumber());
        if (exsistingUserPhonenumber.isPresent()) {
            throw new ObjectAlreadyExists("User", "phonenumber");
        }
        Optional<User> exsistingUserUsername = getUserByUsername(req.username());
        if (exsistingUserUsername.isPresent()) {
            throw new ObjectAlreadyExists("User", "username");
        }

        User user = new User();
        user.setName(req.name());
        user.setUsername(req.username());
        user.setEmail(req.email());
        user.setPhonenumber(req.phonenumber());
        user.setBirthdate(req.birthdate());
        user.setPassword(req.password());
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if ((req.role().equals("COMPANY")) || (req.role().equals("AGENT"))) {
            user.setRole(Role.valueOf(req.role()));
        }
        if (req.role().equals("COMPANY")) {
            Company company = companyService.findOrCreateCompany(req.companyName(), req.companyPhonenumber());
            user.setCompany(company);
        }
        User savedUser = userRepo.save(user);
        if (req.role().equals("AGENT")) {
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
        }

        else if (user.getAgent() != null) {
            Agent agent = user.getAgent();
            cv = agent.getCv();
        }

        UserResponse response = new UserResponse(user.getId(), user.getName(), user.getUsername(), companyID,
                companyName,
                companyPhonenumber, cv,
                user.getEmail(),
                user.getPhonenumber(), user.getBirthdate(), user.getRole());

        return response;
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

            }

            else if (user.getAgent() != null) {
                Agent agent = user.getAgent();
                cv = agent.getCv();
            }
            responseList.add(new UserResponse(user.getId(), user.getName(), user.getUsername(), companyID, companyName,
                    companyPhonenumber, cv,
                    user.getEmail(), user.getPhonenumber(), user.getBirthdate(), user.getRole()));
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
        Optional<User> exsistingUserEmail = getUserByEmail(req.email());
        if (exsistingUserEmail.isPresent() && !exsistingUserEmail.get().getEmail().equals(user.getEmail())) {
            throw new ObjectAlreadyExists("User", "email");
        }
        Optional<User> exsistingUserPhonenumber = getUserByPhonenumber(req.phonenumber());
        if (exsistingUserPhonenumber.isPresent()
                && !exsistingUserEmail.get().getPhonenumber().equals(user.getPhonenumber())) {
            throw new ObjectAlreadyExists("User", "phonenumber");
        }
        Optional<User> exsistingUserUsername = getUserByUsername(req.username());
        if (exsistingUserUsername.isPresent() && !exsistingUserEmail.get().getUsername().equals(user.getUsername())) {
            throw new ObjectAlreadyExists("User", "username");
        }

        if (req.name() != null) {
            user.setName(req.name());
        }
        if (req.email() != null) {
            user.setEmail(req.email());
        }
        if (req.password() != null) {
            user.setPassword(passwordEncoder.encode(req.password()));
        }
        if (req.birthdate() != null) {
            user.setBirthdate(req.birthdate());
        }
        if (req.phonenumber() != null) {
            user.setPhonenumber(req.phonenumber());
        }
        if (req.username() != null) {
            user.setUsername(req.username());
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

        }

        else if (user.getAgent() != null) {
            Agent agent = user.getAgent();
            cv = agent.getCv();
        }

        return new UserResponse(user.getId(), user.getName(), user.getUsername(), companyID, companyName,
                companyPhonenumber, cv,
                user.getEmail(), user.getPhonenumber(), user.getBirthdate(),
                user.getRole());
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
