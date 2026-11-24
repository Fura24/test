package com.example.auth.service;

import com.example.auth.security.JwtService;
import com.example.auth.user.Role;
import com.example.auth.user.RoleRepository;
import com.example.auth.user.User;
import com.example.auth.user.UserRepository;
import com.example.auth.web.AuthResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private static final String DEFAULT_ROLE = "ROLE_USER";

    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    // A real BCrypt hash of a throwaway string. Used so that logging in with an
    // unknown email takes about as long as a wrong password (prevents timing leaks).
    private final String dummyHash;

    public AuthService(UserRepository users, RoleRepository roles,
                       PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.roles = roles;
        this.encoder = encoder;
        this.jwt = jwt;
        this.dummyHash = encoder.encode("not-a-real-password");
    }

    @Transactional
    public User register(String rawEmail, String rawPassword) {
        String email = normalize(rawEmail);

        if (users.existsByEmail(email)) {
            throw new EmailAlreadyUsedException();
        }

        Role defaultRole = roles.findByName(DEFAULT_ROLE)
                .orElseThrow(() -> new IllegalStateException("Role " + DEFAULT_ROLE + " is missing"));

        User user = new User(email, encoder.encode(rawPassword)); // hash it, never store the raw one
        user.getRoles().add(defaultRole);

        try {
            return users.saveAndFlush(user);
        } catch (DataIntegrityViolationException race) {
            // Two requests registered the same email at the same moment; the DB unique index caught it.
            throw new EmailAlreadyUsedException();
        }
    }

    @Transactional(readOnly = true)
    public AuthResponse login(String rawEmail, String rawPassword) {
        User user = users.findByEmail(normalize(rawEmail)).orElse(null);

        // Always run one BCrypt comparison, even when the user doesn't exist.
        String hashToCheck = (user != null) ? user.getPasswordHash() : dummyHash;
        boolean passwordOk = encoder.matches(rawPassword, hashToCheck);

        // One generic message for every failure, so attackers can't tell
        // "no such email" from "wrong password" from "account disabled".
        if (user == null || !passwordOk || !user.isEnabled()) {
            throw new BadCredentialsException("Invalid email or password");
        }

        return new AuthResponse(jwt.issueAccessToken(user), "Bearer", jwt.accessTokenSeconds());
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
