package sample.project.Service;

import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sample.project.Model.User;
import sample.project.Repo.UserRepo;

@Service
@RequiredArgsConstructor
public class UserDetailService implements UserDetailsService {

    private final UserRepo userRepo;

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

    public UserDetails loadUserById(Long id) {
        Optional<User> user = userRepo.findById(id);

        if (!user.isPresent()) {
            return null;
        }

        else {
            return user.get();
        }

    }

}
