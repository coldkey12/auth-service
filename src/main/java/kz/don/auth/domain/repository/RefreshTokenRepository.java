package kz.don.auth.domain.repository;

import kz.don.auth.domain.entity.RefreshToken;
import kz.don.auth.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByToken(String token);

    Optional<RefreshToken> findByUser(User user);

    Optional<RefreshToken> findByUserId(UUID userId);

    List<RefreshToken> findByExpiryDateBefore(Instant now);
}
