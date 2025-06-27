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
import sample.project.Auth.JwtService;
import sample.project.DTO.request.LoginRequest;
import sample.project.DTO.request.RegisterRequest;
import sample.project.DTO.response.LoginResponse;
import sample.project.DTO.response.RegisterResponse;
import sample.project.DTO.response.UserResponse;
import sample.project.DTO.response.UserResponseList;
import sample.project.Model.Role;
import sample.project.Model.User;
import sample.project.exception.UserNotFound;
import sample.project.exception.UserWithThisIdentifierExists;
import sample.project.repo.UserRepo;

@Service
public class UserService {
    private UserRepo userRepo;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private JwtService jwtService;

    public UserService(UserRepo userRepo, PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;

    }

    public RegisterResponse createUser(RegisterRequest req) {
        Optional<User> exsistingUserEmail = getUserByEmail(req.email());
        if (exsistingUserEmail.isPresent()) {
            throw new UserWithThisIdentifierExists("email");
        }
        Optional<User> exsistingUserPhonenumber = getUserByPhonenumber(req.phonenumber());
        if (exsistingUserPhonenumber.isPresent()) {
            throw new UserWithThisIdentifierExists("phonenumber");
        }
        Optional<User> exsistingUserUsername = getUserByUsername(req.username());
        if (exsistingUserUsername.isPresent()) {
            throw new UserWithThisIdentifierExists("username");
        }

        User user = new User();
        user.setName(req.name());
        user.setUsername(req.username());
        user.setEmail(req.email());
        user.setPhonenumber(req.phonenumber());
        user.setBirthdate(req.birthdate());
        user.setPassword(req.password());
        user.setRole(Role.USER);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepo.save(user);
        String token = jwtService.generateToken(user);

        return new RegisterResponse(savedUser.getId(), savedUser.getUsername(), token);
    }

    public UserResponse getUser(Long id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new UserNotFound("id"));
        UserResponse response = new UserResponse(user.getId(), user.getName(), user.getUsername(), user.getEmail(),
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
            responseList.add(new UserResponse(user.getId(), user.getName(), user.getUsername(),
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
                .orElseThrow(() -> new UserNotFound("id"));

        // check if another user with the same credentials as the update already exsists
        // in the database.
        Optional<User> exsistingUserEmail = getUserByEmail(req.email());
        if (exsistingUserEmail.isPresent() && !exsistingUserEmail.get().getEmail().equals(user.getEmail())) {
            throw new UserWithThisIdentifierExists("email");
        }
        Optional<User> exsistingUserPhonenumber = getUserByPhonenumber(req.phonenumber());
        if (exsistingUserPhonenumber.isPresent()
                && !exsistingUserEmail.get().getPhonenumber().equals(user.getPhonenumber())) {
            throw new UserWithThisIdentifierExists("phonenumber");
        }
        Optional<User> exsistingUserUsername = getUserByUsername(req.username());
        if (exsistingUserUsername.isPresent() && !exsistingUserEmail.get().getUsername().equals(user.getUsername())) {
            throw new UserWithThisIdentifierExists("username");
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

        return new UserResponse(user.getId(), user.getName(), user.getUsername(),
                user.getEmail(), user.getPhonenumber(), user.getBirthdate(),
                user.getRole());
    }

    public void deleteUser(Long id) {
        userRepo.findById(id).orElseThrow(() -> new UserNotFound("id"));
        userRepo.deleteById(id);
    }
}
