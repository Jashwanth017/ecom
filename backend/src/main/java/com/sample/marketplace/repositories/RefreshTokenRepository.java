package com.sample.marketplace.repositories;

import com.sample.marketplace.models.RefreshToken;
import com.sample.marketplace.models.User;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findAllByUserAndRevokedFalse(User user);

    void deleteAllByExpiresAtBefore(Instant instant);
}
