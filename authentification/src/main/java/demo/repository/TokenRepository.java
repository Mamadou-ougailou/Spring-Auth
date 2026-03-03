package demo.repository;

import demo.model.Identity;
import demo.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    // Find a token by its value
    Optional<Token> findByToken(String token);

    // Delete all tokens for a given identity
    void deleteByIdentityId(Long identityId);

    // Custom query used by AdminService.getAllConnectedUsers()
    @Query("SELECT DISTINCT t.identity FROM Token t WHERE t.expirationTime > :now")
    Set<Identity> findAllIdentitiesWithValidToken(@Param("now") long now);
}
