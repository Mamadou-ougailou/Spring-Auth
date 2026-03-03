package demo.repository;

import demo.model.Authority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthorityRepository extends JpaRepository<Authority, Long> {

    // Find all authorities for a given identity
    List<Authority> findByIdentityId(Long identityId);

    // Find by provider type
    List<Authority> findByProvider(Authority.Provider provider);
}