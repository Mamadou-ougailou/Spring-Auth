package demo.repository;

import demo.model.Credential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CredentialRepository extends JpaRepository<Credential, Long> {
    
    // Find a role by name (e.g. "ROLE_ADMIN") so we don't create duplicates
    Optional<Credential> findByName(String name);
    
    boolean existsByName(String name);
}