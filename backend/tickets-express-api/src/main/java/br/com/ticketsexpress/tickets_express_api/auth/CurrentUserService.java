package br.com.ticketsexpress.tickets_express_api.auth;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UUID requireCurrentUserId() {
        return requireCurrentUser().getId();
    }

    public ApplicationUser requireCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new AuthenticationCredentialsNotFoundException("Unauthenticated");
        }

        Object principal = auth.getPrincipal();
        String username;
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
            username = userDetails.getUsername();
        } else if (principal instanceof String email) {
            username = email;
        } else {
            throw new AuthenticationCredentialsNotFoundException("Unsupported authentication principal");
        }

        return userRepository.findByEmail(username)
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Authenticated user not found"));
    }

    public ApplicationUser findCurrentUserOrNull() {
        try {
            return requireCurrentUser();
        } catch (AuthenticationCredentialsNotFoundException ex) {
            return null;
        }
    }
}
