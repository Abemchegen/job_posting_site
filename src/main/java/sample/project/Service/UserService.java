package sample.project.Service;

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

    public User getUser(Long id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new UserNotFound("id"));
        return user;
    }

    public LoginResponse login(LoginRequest req) {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(req.username(), req.password()));

        if (authentication.isAuthenticated()) {
            return new LoginResponse("Login Successful.");

        } else {
            return new LoginResponse("Login failed.");
        }
    }

    public List<User> getAllUser() {
        return userRepo.findAll();
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
    public User updateUser(User userUpdates, Long id) {

        User exsistingUser = userRepo.findById(id)
                .orElseThrow(() -> new UserNotFound("id"));

        if (userUpdates.getName() != null) {
            exsistingUser.setName(userUpdates.getName());
        }
        if (userUpdates.getEmail() != null) {
            exsistingUser.setEmail(userUpdates.getEmail());
        }
        if (userUpdates.getPassword() != null) {
            exsistingUser.setPassword(userUpdates.getPassword());
        }
        if (userUpdates.getBirthdate() != null) {
            exsistingUser.setBirthdate(userUpdates.getBirthdate());
        }
        if (userUpdates.getPhonenumber() != null) {
            exsistingUser.setPhonenumber(userUpdates.getPhonenumber());
        }
        if (userUpdates.getUsername() != null) {
            exsistingUser.setUsername(userUpdates.getUsername());
        }

        return exsistingUser;
    }

    public void deleteUser(Long id) {
        userRepo.findById(id).orElseThrow(() -> new UserNotFound("id"));
        userRepo.deleteById(id);
    }
}
