package molip.server.auth.jwt.userDetails;

import molip.server.user.entity.Users;
import molip.server.user.repository.UserRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Cacheable(value = "userDetailsCache", key = "#userId")
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        Users user =
                userRepository
                        .findById(Long.valueOf(userId))
                        .orElseThrow(() -> new RuntimeException("NOT_FOUND_USEr"));

        return new CustomUserDetails(user);
    }
}
