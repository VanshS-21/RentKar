package com.rentkar.service;

import com.rentkar.dto.LoginRequest;
import com.rentkar.dto.LoginResponse;
import com.rentkar.dto.RegisterRequest;
import com.rentkar.dto.UserDTO;
import com.rentkar.model.Role;
import com.rentkar.model.User;
import com.rentkar.repository.UserRepository;
import com.rentkar.security.JwtUtil;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }
    
    @Override
    @Transactional
    public UserDTO register(RegisterRequest request) {
        // Validate email format
        if (!isValidEmail(request.getEmail())) {
            throw new IllegalArgumentException("Invalid email format");
        }
        
        // Validate password length
        if (request.getPassword() == null || request.getPassword().length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
        
        // Check for duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        // Check for duplicate username
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        // Create new user with hashed password
        User user = new User();
        setUserField(user, "username", request.getUsername());
        setUserField(user, "email", request.getEmail());
        setUserField(user, "password", passwordEncoder.encode(request.getPassword()));
        setUserField(user, "fullName", request.getFullName());
        setUserField(user, "phone", request.getPhone());
        setUserField(user, "role", Role.USER);
        
        User savedUser = userRepository.save(user);
        
        return convertToDTO(savedUser);
    }
    
    @Override
    public LoginResponse login(LoginRequest request) {
        // Find user by username
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        
        // Verify password - access field directly since Lombok isn't working
        String userPassword = getUserPassword(user);
        if (!passwordEncoder.matches(request.getPassword(), userPassword)) {
            throw new BadCredentialsException("Invalid credentials");
        }
        
        // Generate JWT token with user claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", getUserId(user));
        claims.put("email", getUserEmail(user));
        claims.put("name", getUserFullName(user));
        claims.put("role", getUserRole(user).name());
        
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(getUserUsername(user))
                .password(userPassword)
                .authorities(getUserRole(user).name())
                .build();
        
        String token = jwtUtil.generateToken(userDetails, claims);
        
        return new LoginResponse(token, convertToDTO(user));
    }
    
    @Override
    public UserDTO getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        return convertToDTO(user);
    }
    
    private UserDTO convertToDTO(User user) {
        return new UserDTO(
            getUserId(user),
            getUserUsername(user),
            getUserEmail(user),
            getUserFullName(user),
            getUserPhone(user),
            getUserRole(user),
            getUserCreatedAt(user)
        );
        // Note: password is intentionally excluded
    }
    
    // Helper methods to access User fields via reflection since Lombok isn't working
    private Long getUserId(User user) {
        try {
            var field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            return (Long) field.get(user);
        } catch (Exception e) {
            throw new RuntimeException("Failed to access user id", e);
        }
    }
    
    private String getUserUsername(User user) {
        try {
            var field = User.class.getDeclaredField("username");
            field.setAccessible(true);
            return (String) field.get(user);
        } catch (Exception e) {
            throw new RuntimeException("Failed to access username", e);
        }
    }
    
    private String getUserEmail(User user) {
        try {
            var field = User.class.getDeclaredField("email");
            field.setAccessible(true);
            return (String) field.get(user);
        } catch (Exception e) {
            throw new RuntimeException("Failed to access email", e);
        }
    }
    
    private String getUserPassword(User user) {
        try {
            var field = User.class.getDeclaredField("password");
            field.setAccessible(true);
            return (String) field.get(user);
        } catch (Exception e) {
            throw new RuntimeException("Failed to access password", e);
        }
    }
    
    private String getUserFullName(User user) {
        try {
            var field = User.class.getDeclaredField("fullName");
            field.setAccessible(true);
            return (String) field.get(user);
        } catch (Exception e) {
            throw new RuntimeException("Failed to access fullName", e);
        }
    }
    
    private String getUserPhone(User user) {
        try {
            var field = User.class.getDeclaredField("phone");
            field.setAccessible(true);
            return (String) field.get(user);
        } catch (Exception e) {
            throw new RuntimeException("Failed to access phone", e);
        }
    }
    
    private Role getUserRole(User user) {
        try {
            var field = User.class.getDeclaredField("role");
            field.setAccessible(true);
            return (Role) field.get(user);
        } catch (Exception e) {
            throw new RuntimeException("Failed to access role", e);
        }
    }
    
    private LocalDateTime getUserCreatedAt(User user) {
        try {
            var field = User.class.getDeclaredField("createdAt");
            field.setAccessible(true);
            return (LocalDateTime) field.get(user);
        } catch (Exception e) {
            throw new RuntimeException("Failed to access createdAt", e);
        }
    }
    
    private void setUserField(User user, String fieldName, Object value) {
        try {
            var field = User.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(user, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set user field: " + fieldName, e);
        }
    }
    
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        // Basic email validation regex
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }
}
