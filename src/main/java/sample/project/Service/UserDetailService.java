package sample.project.Service;

import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import sample.project.Model.User;
import sample.project.repo.UserRepo;

@Service
public class UserDetailService implements UserDetailsService {

    private UserRepo userRepo;

    public UserDetailService(UserRepo userRepo) {
        this.userRepo = userRepo;

    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepo.findByUsername(username);

        if (!user.isPresent()) {
            throw new UsernameNotFoundException("User not found!");
        }

        else {
            return user.get();
        }

    }

}
