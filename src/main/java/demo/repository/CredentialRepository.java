package demo.repository;

import demo.model.Credential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CredentialRepository extends JpaRepository<Credential, Long> {

    // Trouver un credential par email
    Optional<Credential> findByEmail(String email);

    // Supprimer par email
    void deleteByEmail(String email);
}
