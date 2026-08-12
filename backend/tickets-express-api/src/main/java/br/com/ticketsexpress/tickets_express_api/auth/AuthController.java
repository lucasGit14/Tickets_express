package br.com.ticketsexpress.tickets_express_api.auth;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AuthController.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        var userOpt = userRepository.findByEmail(request.email());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        var user = userOpt.get();
        boolean matches;
        try {
            matches = passwordEncoder.matches(request.password(), user.getPasswordHash());
        } catch (Exception ex) {
            // If the stored password hash is in an unexpected format, treat as invalid credentials.
            // Log for diagnostics but avoid exposing password or hash contents.
            logger.warn("Password verification failed for user {}: {}", user.getEmail(), ex.getClass().getSimpleName());
            logger.debug("Password verification exception details", ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!matches) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token;
        try {
            token = jwtService.generateToken(user);
        } catch (Exception ex) {
            // If token generation fails for an unexpected reason, return 500 so the issue can be investigated.
            // Log without exposing secrets.
            logger.error("Failed to generate JWT for user {}: {}", user.getEmail(), ex.getClass().getSimpleName());
            logger.debug("JWT generation exception details", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.ok(new LoginResponse(token));
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        // allow only ORGANIZER or CUSTOMER; GATEKEEPER reserved for seed/admin
        UserRole role = request.role() == null ? UserRole.CUSTOMER : request.role();
        if (role != UserRole.CUSTOMER && role != UserRole.ORGANIZER) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        ApplicationUser user = new ApplicationUser(
                java.util.UUID.randomUUID(),
                request.name(),
                request.email(),
                passwordEncoder.encode(request.password()),
                role,
                java.time.Instant.now()
        );

        userRepository.save(user);
        String token = jwtService.generateToken(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(new LoginResponse(token));
    }
}

