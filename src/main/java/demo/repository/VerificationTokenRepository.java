package demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import demo.model.VerificationToken;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, String> {
    Optional<VerificationToken> findByTokenId(String tokenId);
    void deleteByUserId(Long userId);
}
