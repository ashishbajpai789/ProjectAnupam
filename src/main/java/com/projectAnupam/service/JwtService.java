package com.projectAnupam.service;

import com.projectAnupam.entity.Token;
import com.projectAnupam.entity.UserType;
import com.projectAnupam.repository.TokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private final TokenRepository tokenRepository;

    public JwtService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(String email, Long userId, UserType userType) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", userId);
        extraClaims.put("userType", userType.name());
        return generateToken(extraClaims, email);
    }

    public String generateToken(Map<String, Object> extraClaims, String email) {
        return buildToken(extraClaims, email, jwtExpiration);
    }

    private String buildToken(Map<String, Object> extraClaims, String subject, long expiration) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, String email) {
        final String username = extractUsername(token);
        boolean isUsernameValid = username.equals(email);
        boolean isNotExpired = !isTokenExpired(token);
        boolean isValidInDb = tokenRepository.isTokenValid(token);

        return isUsernameValid && isNotExpired && isValidInDb;
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public void saveToken(String jwtToken, Long userId, UserType userType) {
        Token token = new Token();
        token.setToken(jwtToken);
        token.setUserId(userId);
        token.setUserType(userType);
        token.setIsExpired(false);
        token.setIsRevoked(false);

        LocalDateTime expiresAt = LocalDateTime.now()
                .plusSeconds(jwtExpiration / 1000);
        token.setExpiresAt(expiresAt);

        tokenRepository.save(token);
    }

    public void revokeToken(String jwtToken) {
        tokenRepository.findByToken(jwtToken).ifPresent(token -> {
            token.setIsRevoked(true);
            tokenRepository.save(token);
        });
    }

    public void revokeAllUserTokens(Long userId, UserType userType) {
        tokenRepository.revokeAllUserTokens(userId, userType);
    }
}
