package demo.repository;

import demo.model.Identity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IdentityRepository extends JpaRepository<Identity, Long> {

    // Trouver un utilisateur par son email
    Optional<Identity> findByEmail(String email);

    // Vérifier si un utilisateur existe
    boolean existsByEmail(String email);

    // Supprimer par email
    void deleteByEmail(String email);
}
