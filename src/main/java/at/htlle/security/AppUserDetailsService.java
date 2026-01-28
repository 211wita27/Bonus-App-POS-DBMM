package at.htlle.security;

import at.htlle.entity.AppUser;
import at.htlle.repository.AppUserRepository;
import java.util.Locale;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Loads application users from the {@link AppUserRepository} for Spring Security.
 */
@Service
public class AppUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    public AppUserDetailsService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    /**
     * Loads an application user by email.
     *
     * @param username email address
     * @return Spring Security user details
     */
    @Override
    public UserDetails loadUserByUsername(String username) {
        String normalized = username == null ? null : username.trim().toLowerCase(Locale.ROOT);
        AppUser user = appUserRepository.findByEmail(normalized)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .roles(user.getRole().name())
                .disabled(!user.isEnabled())
                .build();
    }
}
