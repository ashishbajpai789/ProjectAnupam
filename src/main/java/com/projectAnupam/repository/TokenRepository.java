package com.projectAnupam.repository;

import com.projectAnupam.entity.Token;
import com.projectAnupam.entity.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    // Find token by token string
    Optional<Token> findByToken(String token);

    // Find all valid tokens for a user
    @Query("SELECT t FROM Token t WHERE t.userId = :userId AND t.userType = :userType " +
            "AND t.isExpired = false AND t.isRevoked = false")
    List<Token> findAllValidTokensByUser(
            @Param("userId") Long userId,
            @Param("userType") UserType userType
    );

    // Revoke all tokens for a user
    @Modifying
    @Query("UPDATE Token t SET t.isRevoked = true WHERE t.userId = :userId AND t.userType = :userType")
    void revokeAllUserTokens(
            @Param("userId") Long userId,
            @Param("userType") UserType userType
    );

    // Delete expired tokens (cleanup job)
    @Modifying
    @Query("DELETE FROM Token t WHERE t.isExpired = true OR t.isRevoked = true")
    void deleteExpiredAndRevokedTokens();

    // Check if token is valid
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Token t " +
            "WHERE t.token = :token AND t.isExpired = false AND t.isRevoked = false")
    boolean isTokenValid(@Param("token") String token);
}
