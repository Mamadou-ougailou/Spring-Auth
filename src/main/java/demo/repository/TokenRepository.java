package demo.repository;

import demo.model.Autority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Autority, Long> {

    // Trouver un token par sa valeur
    Optional<Autority> findByToken(String token);

    // Supprimer tous les tokens d'un utilisateur
    void deleteByEmail(String email);
}
