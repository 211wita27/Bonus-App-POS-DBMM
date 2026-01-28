package at.htlle.service;

import at.htlle.entity.AppUser;
import at.htlle.repository.AppUserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Locale;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final AppUserRepository appUserRepository;

    public CurrentUserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public AppUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new EntityNotFoundException("No authenticated user");
        }
        String email = authentication.getName();
        String normalized = email == null ? null : email.trim().toLowerCase(Locale.ROOT);
        return appUserRepository.findByEmail(normalized)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }
}
