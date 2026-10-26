package ch.jacqueline.inventory.services;


import ch.jacqueline.inventory.entity.User;
// Kontakt-Repository ist hier nicht mehr direkt nötig, da UserService verwendet wird
// UserRepository ist hier nicht mehr direkt nötig, da UserService verwendet wird
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger; // Import für Logger
import org.slf4j.LoggerFactory; // Import für LoggerFactory

/**
 * <p>
 * Implementierung des {@link UserDetailsService} Interfaces, lädt Userdetails aus dem Repository für die Spring Security Authentifizierung.
 * Dieser Service wurde angepasst, um mit dem {@link UserService} zusammenzuarbeiten und detailliertere Debug-Logs zu liefern.
 * </p>
 */
@Service
public class CustomUserRepositoryUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserRepositoryUserDetailsService.class);

    private final UserService userService;

    /**
     * Konstruktor für die {@code CustomUserRepositoryUserDetailsService}.
     *
     * @param userService zur Interaktion mit den Benutzerdaten.
     */
    public CustomUserRepositoryUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    /**
     * Lädt User anhand der E-Mail-Adresse, die als Benutzername dient.
     *
     * @param email die E-Mail-Adresse des Benutzers.
     * @return {@link UserDetails} Objekt, das Benutzerinformationen enthält.
     * @throws UsernameNotFoundException wenn kein Benutzer mit der gegebenen E-Mail-Adresse gefunden wird.
     */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        logger.debug("loadUserByUsername aufgerufen für Benutzername (E-Mail): {}", email);

        User user = userService.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("Benutzer mit E-Mail {} nicht gefunden. Authentifizierung fehlgeschlagen.", email);
                    return new UsernameNotFoundException("Benutzer mit E-Mail '" + email + "' wurde nicht gefunden.");
                });

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            logger.error("Passwort für Benutzer '{}' (E-Mail: {}) ist null oder leer. Authentifizierung unmöglich.", user.getUsername(), email);
            throw new UsernameNotFoundException("Benutzerkonto ungültig: Passwort fehlt.");
        }
        if (!user.getPassword().startsWith("$2a$") && !user.getPassword().startsWith("$2b$") && !user.getPassword().startsWith("$2y$")) {
            logger.warn("Passwort für Benutzer '{}' (E-Mail: {}) scheint nicht BCrypt-gehasht zu sein. Dies könnte ein Authentifizierungsproblem verursachen.", user.getUsername(), email);
        }

        logger.info("Benutzer '{}' (E-Mail: {}) erfolgreich geladen für Authentifizierung.", user.getUsername(), email);

        return new org.springframework.security.core.userdetails.User(
                user.getContact().getEmailAddress(),
                user.getPassword(),
                AuthorityUtils.createAuthorityList(user.getRole())
        );
    }
}