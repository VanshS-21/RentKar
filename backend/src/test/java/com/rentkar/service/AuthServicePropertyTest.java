package com.rentkar.service;

import com.rentkar.dto.LoginRequest;
import com.rentkar.dto.LoginResponse;
import com.rentkar.dto.RegisterRequest;
import com.rentkar.dto.UserDTO;
import com.rentkar.model.User;
import com.rentkar.repository.UserRepository;
import com.rentkar.security.JwtUtil;
import net.jqwik.api.*;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class AuthServicePropertyTest {
    
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
    
    @Property(tries = 100)
    void validRegistrationCreatesUserWithHashedPassword(
            @ForAll("validUsername") String username,
            @ForAll("validEmail") String email,
            @ForAll("validPassword") String password,
            @ForAll("validFullName") String fullName) {
        
        UserRepository mockRepo = Mockito.mock(UserRepository.class);
        Map<String, User> db = new HashMap<>();
        
        when(mockRepo.existsByEmail(anyString())).thenReturn(false);
        when(mockRepo.existsByUsername(anyString())).thenReturn(false);
        when(mockRepo.save(any(User.class))).thenAnswer(inv -> {
            User user = inv.getArgument(0);
            setField(user, "id", 1L);
            db.put(getField(user, "username"), user);
            return user;
        });
        
        AuthService service = new AuthServiceImpl(mockRepo, passwordEncoder, createJwt());
        
        RegisterRequest req = new RegisterRequest();
        req.setUsername(username);
        req.setEmail(email);
        req.setPassword(password);
        req.setFullName(fullName);
        
        UserDTO result = service.register(req);
        
        assertThat(result.getUsername()).isEqualTo(username);
        User saved = db.get(username);
        String savedPwd = getField(saved, "password");
        assertThat(savedPwd).isNotEqualTo(password);
        assertThat(passwordEncoder.matches(password, savedPwd)).isTrue();
    }
    
    @Property(tries = 100)
    void duplicateEmailRegistrationIsRejected(
            @ForAll("validUsername") String u1,
            @ForAll("validUsername") String u2,
            @ForAll("validEmail") String email,
            @ForAll("validPassword") String pwd,
            @ForAll("validFullName") String name) {
        
        UserRepository mockRepo = Mockito.mock(UserRepository.class);
        when(mockRepo.existsByEmail(email)).thenReturn(false).thenReturn(true);
        when(mockRepo.existsByUsername(anyString())).thenReturn(false);
        when(mockRepo.save(any(User.class))).thenAnswer(inv -> {
            User user = inv.getArgument(0);
            setField(user, "id", 1L);
            return user;
        });
        
        AuthService service = new AuthServiceImpl(mockRepo, passwordEncoder, createJwt());
        
        RegisterRequest r1 = new RegisterRequest();
        r1.setUsername(u1);
        r1.setEmail(email);
        r1.setPassword(pwd);
        r1.setFullName(name);
        service.register(r1);
        
        RegisterRequest r2 = new RegisterRequest();
        r2.setUsername(u2);
        r2.setEmail(email);
        r2.setPassword(pwd);
        r2.setFullName(name);
        
        try {
            service.register(r2);
            throw new AssertionError("Expected exception");
        } catch (RuntimeException e) {
            assertThat(e.getMessage().toLowerCase()).contains("email");
        }
    }
    
    @Property(tries = 100)
    void invalidEmailFormatsAreRejected(
            @ForAll("validUsername") String username,
            @ForAll("invalidEmail") String email,
            @ForAll("validPassword") String pwd,
            @ForAll("validFullName") String name) {
        
        UserRepository mockRepo = Mockito.mock(UserRepository.class);
        when(mockRepo.existsByEmail(anyString())).thenReturn(false);
        when(mockRepo.existsByUsername(anyString())).thenReturn(false);
        
        AuthService service = new AuthServiceImpl(mockRepo, passwordEncoder, createJwt());
        
        RegisterRequest req = new RegisterRequest();
        req.setUsername(username);
        req.setEmail(email);
        req.setPassword(pwd);
        req.setFullName(name);
        
        try {
            service.register(req);
            throw new AssertionError("Expected exception");
        } catch (RuntimeException e) {
            assertThat(e.getMessage().toLowerCase()).contains("email");
        }
    }
    
    @Property(tries = 100)
    void shortPasswordsAreRejected(
            @ForAll("validUsername") String username,
            @ForAll("validEmail") String email,
            @ForAll("shortPassword") String pwd,
            @ForAll("validFullName") String name) {
        
        UserRepository mockRepo = Mockito.mock(UserRepository.class);
        when(mockRepo.existsByEmail(anyString())).thenReturn(false);
        when(mockRepo.existsByUsername(anyString())).thenReturn(false);
        
        AuthService service = new AuthServiceImpl(mockRepo, passwordEncoder, createJwt());
        
        RegisterRequest req = new RegisterRequest();
        req.setUsername(username);
        req.setEmail(email);
        req.setPassword(pwd);
        req.setFullName(name);
        
        try {
            service.register(req);
            throw new AssertionError("Expected exception");
        } catch (RuntimeException e) {
            assertThat(e.getMessage().toLowerCase()).contains("password");
        }
    }
    
    @Property(tries = 100)
    void userResponsesExcludePasswords(
            @ForAll("validUsername") String username,
            @ForAll("validEmail") String email,
            @ForAll("validPassword") String pwd,
            @ForAll("validFullName") String name) {
        
        UserRepository mockRepo = Mockito.mock(UserRepository.class);
        when(mockRepo.existsByEmail(anyString())).thenReturn(false);
        when(mockRepo.existsByUsername(anyString())).thenReturn(false);
        when(mockRepo.save(any(User.class))).thenAnswer(inv -> {
            User user = inv.getArgument(0);
            setField(user, "id", 1L);
            return user;
        });
        
        AuthService service = new AuthServiceImpl(mockRepo, passwordEncoder, createJwt());
        
        RegisterRequest req = new RegisterRequest();
        req.setUsername(username);
        req.setEmail(email);
        req.setPassword(pwd);
        req.setFullName(name);
        
        UserDTO result = service.register(req);
        
        assertThat(result).isNotNull();
        try {
            UserDTO.class.getDeclaredField("password");
            throw new AssertionError("UserDTO should not have password field");
        } catch (NoSuchFieldException e) {
        }
    }
    
    @Provide
    Arbitrary<String> validUsername() {
        return Arbitraries.strings().alpha().numeric().ofMinLength(3).ofMaxLength(20).map(s -> "u_" + s);
    }
    
    @Provide
    Arbitrary<String> validEmail() {
        return Arbitraries.strings().alpha().numeric().ofMinLength(3).ofMaxLength(10).map(s -> s + "@ex.com");
    }
    
    @Provide
    Arbitrary<String> validPassword() {
        return Arbitraries.strings().alpha().numeric().ofMinLength(8).ofMaxLength(20);
    }
    
    @Provide
    Arbitrary<String> validFullName() {
        return Arbitraries.strings().alpha().withChars(' ').ofMinLength(3).ofMaxLength(30);
    }
    
    @Provide
    Arbitrary<String> invalidEmail() {
        return Arbitraries.oneOf(
            Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(10),
            Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(10).map(s -> "@" + s),
            Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(10).map(s -> s + "@"),
            Arbitraries.just("")
        );
    }
    
    @Provide
    Arbitrary<String> shortPassword() {
        return Arbitraries.strings().alpha().numeric().ofMinLength(1).ofMaxLength(7);
    }
    
    private String getField(User user, String field) {
        try {
            var f = User.class.getDeclaredField(field);
            f.setAccessible(true);
            return (String) f.get(user);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private void setField(User user, String field, Object val) {
        try {
            var f = User.class.getDeclaredField(field);
            f.setAccessible(true);
            f.set(user, val);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Property(tries = 100)
    void validCredentialsGenerateJwtToken(
            @ForAll("validUsername") String username,
            @ForAll("validEmail") String email,
            @ForAll("validPassword") String pwd,
            @ForAll("validFullName") String name) {
        
        UserRepository mockRepo = Mockito.mock(UserRepository.class);
        Map<String, User> db = new HashMap<>();
        
        when(mockRepo.existsByEmail(anyString())).thenReturn(false);
        when(mockRepo.existsByUsername(anyString())).thenReturn(false);
        when(mockRepo.save(any(User.class))).thenAnswer(inv -> {
            User user = inv.getArgument(0);
            setField(user, "id", 1L);
            db.put(getField(user, "username"), user);
            return user;
        });
        when(mockRepo.findByUsername(anyString())).thenAnswer(inv -> {
            String u = inv.getArgument(0);
            return Optional.ofNullable(db.get(u));
        });
        
        AuthService service = new AuthServiceImpl(mockRepo, passwordEncoder, createJwt());
        
        RegisterRequest regReq = new RegisterRequest();
        regReq.setUsername(username);
        regReq.setEmail(email);
        regReq.setPassword(pwd);
        regReq.setFullName(name);
        service.register(regReq);
        
        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsername(username);
        loginReq.setPassword(pwd);
        
        LoginResponse response = service.login(loginReq);
        
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isNotNull().isNotEmpty();
        assertThat(response.getType()).isEqualTo("Bearer");
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getUsername()).isEqualTo(username);
    }
    
    @Property(tries = 100)
    void incorrectPasswordsAreRejected(
            @ForAll("validUsername") String username,
            @ForAll("validEmail") String email,
            @ForAll("validPassword") String correctPwd,
            @ForAll("validPassword") String wrongPwd,
            @ForAll("validFullName") String name) {
        
        Assume.that(!correctPwd.equals(wrongPwd));
        
        UserRepository mockRepo = Mockito.mock(UserRepository.class);
        Map<String, User> db = new HashMap<>();
        
        when(mockRepo.existsByEmail(anyString())).thenReturn(false);
        when(mockRepo.existsByUsername(anyString())).thenReturn(false);
        when(mockRepo.save(any(User.class))).thenAnswer(inv -> {
            User user = inv.getArgument(0);
            setField(user, "id", 1L);
            db.put(getField(user, "username"), user);
            return user;
        });
        when(mockRepo.findByUsername(anyString())).thenAnswer(inv -> {
            String u = inv.getArgument(0);
            return Optional.ofNullable(db.get(u));
        });
        
        AuthService service = new AuthServiceImpl(mockRepo, passwordEncoder, createJwt());
        
        RegisterRequest regReq = new RegisterRequest();
        regReq.setUsername(username);
        regReq.setEmail(email);
        regReq.setPassword(correctPwd);
        regReq.setFullName(name);
        service.register(regReq);
        
        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsername(username);
        loginReq.setPassword(wrongPwd);
        
        try {
            service.login(loginReq);
            throw new AssertionError("Expected exception");
        } catch (RuntimeException e) {
            // Expected
        }
    }
    
    @Property(tries = 100)
    void nonExistentUsersAreRejected(
            @ForAll("validUsername") String username,
            @ForAll("validPassword") String pwd) {
        
        UserRepository mockRepo = Mockito.mock(UserRepository.class);
        when(mockRepo.findByUsername(anyString())).thenReturn(Optional.empty());
        
        AuthService service = new AuthServiceImpl(mockRepo, passwordEncoder, createJwt());
        
        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsername(username);
        loginReq.setPassword(pwd);
        
        try {
            service.login(loginReq);
            throw new AssertionError("Expected exception");
        } catch (RuntimeException e) {
            // Expected
        }
    }
    
    private JwtUtil createJwt() {
        try {
            JwtUtil jwt = new JwtUtil();
            var s = JwtUtil.class.getDeclaredField("secret");
            s.setAccessible(true);
            s.set(jwt, "testSecretKeyForJwtTokenGenerationThatIsLongEnough12345678");
            var e = JwtUtil.class.getDeclaredField("expiration");
            e.setAccessible(true);
            e.set(jwt, 86400000L);
            return jwt;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
