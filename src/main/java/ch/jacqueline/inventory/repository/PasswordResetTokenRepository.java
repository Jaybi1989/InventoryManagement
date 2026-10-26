package ch.jacqueline.inventory.repository; // Dein Repository-Paket

import ch.jacqueline.inventory.entity.PasswordResetToken;
import ch.jacqueline.inventory.entity.User; // Importiere deine User-Entität
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


/**
 * Repository für die Verwaltung von Passwort-Reset-Tokens.
 * Dieses Interface erweitert JpaRepository und bietet Methoden zum Finden und Löschen von Tokens.
 * Es ist mit der @Repository-Annotation annotiert, um es als Spring-Repository zu kennzeichnen.
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    void deleteByUser(User user);

    Optional<PasswordResetToken> findByUser(User user);
}