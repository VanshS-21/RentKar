package com.rentkar.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import net.jqwik.api.*;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;

public class JwtUtilPropertyTest {

    private static final String TEST_SECRET = "RentKarSecretKeyForJWTTokenGenerationAndValidation2024";
    private static final Long TEST_EXPIRATION = 86400000L; // 24 hours in milliseconds

    private JwtUtil createJwtUtil() {
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", TEST_EXPIRATION);
        return jwtUtil;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Feature: user-authentication, Property 10: JWT tokens expire after 24 hours
     * Validates: Requirements 2.5
     */
    @Property
    void jwtTokensExpireAfter24Hours(@ForAll("usernames") String username) {
        // Arrange
        JwtUtil jwtUtil = createJwtUtil();
        UserDetails userDetails = User.builder()
                .username(username)
                .password("password")
                .authorities(new ArrayList<>())
                .build();

        // Act
        String token = jwtUtil.generateToken(userDetails);
        
        // Extract claims to verify expiration
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        Date issuedAt = claims.getIssuedAt();
        Date expiration = claims.getExpiration();
        
        // Assert
        long actualExpirationTime = expiration.getTime() - issuedAt.getTime();
        long expectedExpirationTime = 86400000L; // 24 hours in milliseconds
        
        // Allow small tolerance for execution time (1 second)
        long tolerance = 1000L;
        assert Math.abs(actualExpirationTime - expectedExpirationTime) <= tolerance :
                String.format("Token expiration should be 24 hours. Expected: %d, Actual: %d",
                        expectedExpirationTime, actualExpirationTime);
    }

    /**
     * Feature: user-authentication, Property 9: JWT tokens contain required claims
     * Validates: Requirements 2.4
     */
    @Property
    void jwtTokensContainRequiredClaims(
            @ForAll("usernames") String username,
            @ForAll("userIds") Long userId,
            @ForAll("emails") String email,
            @ForAll("names") String name) {
        // Arrange
        JwtUtil jwtUtil = createJwtUtil();
        UserDetails userDetails = User.builder()
                .username(username)
                .password("password")
                .authorities(new ArrayList<>())
                .build();

        // Create extra claims with user information
        java.util.Map<String, Object> extraClaims = new java.util.HashMap<>();
        extraClaims.put("userId", userId);
        extraClaims.put("email", email);
        extraClaims.put("name", name);

        // Act
        String token = jwtUtil.generateToken(userDetails, extraClaims);
        
        // Extract claims to verify required fields
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        // Assert - verify all required claims are present
        assert claims.get("userId") != null : "Token should contain userId claim";
        assert claims.get("email") != null : "Token should contain email claim";
        assert claims.get("name") != null : "Token should contain name claim";
        
        // Verify the values match
        assert claims.get("userId", Long.class).equals(userId) : 
                String.format("userId should match. Expected: %d, Actual: %d", 
                        userId, claims.get("userId", Long.class));
        assert claims.get("email", String.class).equals(email) : 
                String.format("email should match. Expected: %s, Actual: %s", 
                        email, claims.get("email", String.class));
        assert claims.get("name", String.class).equals(name) : 
                String.format("name should match. Expected: %s, Actual: %s", 
                        name, claims.get("name", String.class));
    }

    @Provide
    Arbitrary<String> usernames() {
        return Arbitraries.strings()
                .alpha()
                .numeric()
                .ofMinLength(3)
                .ofMaxLength(50);
    }

    @Provide
    Arbitrary<Long> userIds() {
        return Arbitraries.longs()
                .between(1L, 1000000L);
    }

    @Provide
    Arbitrary<String> emails() {
        return Arbitraries.strings()
                .alpha()
                .numeric()
                .ofMinLength(3)
                .ofMaxLength(20)
                .map(s -> s + "@example.com");
    }

    @Provide
    Arbitrary<String> names() {
        return Arbitraries.strings()
                .alpha()
                .withChars(' ')
                .ofMinLength(3)
                .ofMaxLength(50);
    }
}
