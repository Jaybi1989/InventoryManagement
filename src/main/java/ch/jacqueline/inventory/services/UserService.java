package ch.jacqueline.inventory.services;

import ch.jacqueline.inventory.entity.Contact;
import ch.jacqueline.inventory.entity.User;
import ch.jacqueline.inventory.entity.PasswordResetToken;
import ch.jacqueline.inventory.repository.UserRepository;
import ch.jacqueline.inventory.repository.PasswordResetTokenRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service-Klasse zur Verwaltung von Benutzern.
 *
 * <p>Diese Klasse bietet Methoden zum Abrufen, Speichern, Aktualisieren und Löschen von Benutzern.
 * Sie verwaltet auch Passwort-Reset-Tokens und stellt sicher, dass Benutzer- und Kontaktinformationen
 * konsistent und sicher gespeichert werden.</p>
 *
 * <p>Die Klasse ist mit {@code @Service} annotiert, damit Spring sie als Service erkennt.
 * Transaktionen werden über {@code @Transactional} gesteuert, wobei lesende Methoden
 * als read-only gekennzeichnet sind.</p>
 *
 * @see UserRepository
 * @see ContactService
 * @see PasswordResetTokenRepository
 * @see User
 * @see Contact
 * @see PasswordResetToken
 * @see PasswordEncoder
 * @see DuplicateKeyException
 */

@Service
public class
UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final ContactService contactService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public UserService(UserRepository userRepository, ContactService contactService,
                       PasswordEncoder passwordEncoder, PasswordResetTokenRepository passwordResetTokenRepository) {
        this.userRepository = userRepository;
        this.contactService = contactService;
        this.passwordEncoder = passwordEncoder;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        logger.info("Lade alle Benutzer.");
        return userRepository.findAll().stream()
                .map(this::populateUserContact)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<User> findByContact(Contact contact) {
        logger.debug("Suche Benutzer nach Kontakt-ID: {}", contact != null ? contact.getContactId() : "null");
        return userRepository.findByContact(contact)
                .map(this::populateUserContact);
    }

    @Transactional(readOnly = true)
    public List<User> findAllUsersByContact(Contact contact) {
        logger.debug("Suche alle Benutzer verknüpft mit Kontakt-ID: {}", contact != null ? contact.getContactId() : "null");
        return userRepository.findAllByContact(contact).stream()
                .map(this::populateUserContact)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        logger.debug("Suche Benutzer nach Benutzer-ID: {}", id);
        return userRepository.findById(id)
                .map(this::populateUserContact);
    }

    @Transactional
    public User save(User user) {
        logger.info("Speichere/Aktualisiere Benutzer: {}", user.getUsername());

        if (user.getUserID() == null) {
            if (userRepository.findByUsername(user.getUsername()).isPresent()) {
                throw new DuplicateKeyException("Benutzername '" + user.getUsername() + "' ist bereits vergeben.");
            }
        } else {
            Optional<User> existingUserWithSameUsername = userRepository.findByUsername(user.getUsername());
            if (existingUserWithSameUsername.isPresent() &&
                    !existingUserWithSameUsername.get().getUserID().equals(user.getUserID())) {
                throw new DuplicateKeyException("Benutzername '" + user.getUsername() + "' ist bereits vergeben.");
            }
        }

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            logger.warn("Benutzer '{}' (ID: {}) hat kein Passwort gesetzt. Dies sollte nur bei Admins der Fall sein, die später ein PW vergeben, oder wenn die Entität das Hashing übernimmt.", user.getUsername(), user.getUserID());
        }

        if (user.getContact() != null) {
            Contact savedContact = contactService.save(user.getContact());
            user.setContact(savedContact);
            logger.debug("Kontakt (ID: {}) für Benutzer '{}' gespeichert/aktualisiert.", savedContact.getContactId(), user.getUsername());
        } else {
            logger.debug("Benutzer '{}' hat keinen verknüpften Kontakt.", user.getUsername());
        }

        User savedUser = userRepository.save(user);
        logger.info("Benutzer '{}' (ID: {}) erfolgreich gespeichert/aktualisiert.", savedUser.getUsername(), savedUser.getUserID());

        return populateUserContact(savedUser);
    }

    public String hashPassword(String plainPassword) {
        return passwordEncoder.encode(plainPassword);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        logger.debug("Suche Benutzer nach Benutzername: {}", username);
        return userRepository.findByUsername(username)
                .map(this::populateUserContact);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        logger.debug("Suche Benutzer nach E-Mail-Adresse: {}", email);
        return userRepository.findByContact_EmailAddress(email)
                .map(this::populateUserContact);
    }

    @Transactional
    public String createPasswordResetTokenForUser(User user) {
        logger.debug("Erstelle/Aktualisiere Passwort-Reset-Token für Benutzer: {}", user.getUsername());

        User managedUser = userRepository.findById(user.getUserID())
                .orElseThrow(() -> new IllegalArgumentException("Benutzer zum Erstellen des Reset-Tokens nicht gefunden."));

        Optional<PasswordResetToken> existingTokenOpt = passwordResetTokenRepository.findByUser(managedUser);

        String tokenValue = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(24);
        PasswordResetToken passwordResetToken;

        if (existingTokenOpt.isPresent()) {
            passwordResetToken = existingTokenOpt.get();
            passwordResetToken.setToken(tokenValue);
            passwordResetToken.setExpiryDate(expiryDate);
            logger.info("Bestehender Passwort-Reset-Token für Benutzer '{}' aktualisiert.", managedUser.getUsername());
        } else {
            passwordResetToken = new PasswordResetToken(tokenValue, managedUser, expiryDate);
            logger.info("Neuen Passwort-Reset-Token für Benutzer '{}' erstellt.", managedUser.getUsername());
        }

        passwordResetTokenRepository.save(passwordResetToken);
        logger.info("Passwort-Reset-Token für Benutzer '{}' generiert/aktualisiert und gespeichert. Token: {}", managedUser.getUsername(), tokenValue);
        return tokenValue;
    }

    @Transactional
    public boolean resetPassword(String token, String newPlainPassword) {
        logger.info("Versuche Passwort zurückzusetzen (Self-Service) mit Token: {}", token);
        Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository.findByToken(token);

        if (tokenOpt.isEmpty()) {
            logger.warn("Passwort-Reset (Self-Service): Token '{}' nicht gefunden.", token);
            return false;
        }

        PasswordResetToken resetToken = tokenOpt.get();

        if (resetToken.isExpired()) {
            logger.warn("Passwort-Reset (Self-Service): Token '{}' ist abgelaufen.", token);
            passwordResetTokenRepository.delete(resetToken);
            return false;
        }

        User user = resetToken.getUser();
        if (user == null) {
            logger.error("Passwort-Reset (Self-Service): Token '{}' ist mit keinem Benutzer verknüpft. (Datenbank-Inkonsistenz?)", token);
            passwordResetTokenRepository.delete(resetToken);
            return false;
        }

        user.setPassword(newPlainPassword);
        save(user);

        passwordResetTokenRepository.delete(resetToken);
        logger.info("Passwort erfolgreich zurückgesetzt (Self-Service) für Benutzer '{}'. Token '{}' gelöscht.", user.getUsername(), token);
        return true;
    }

    @Transactional
    public boolean resetPasswordByAdmin(Long userId, String newPlainPassword) {
        logger.info("Versuche Passwort zurückzusetzen (Admin-Reset) für Benutzer ID: {}", userId);
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isEmpty()) {
            logger.warn("Admin-Reset: Benutzer mit ID {} nicht gefunden.", userId);
            return false;
        }

        User user = userOpt.get();
        user.setPassword(newPlainPassword);
        save(user);

        logger.info("Passwort erfolgreich zurückgesetzt (Admin-Reset) für Benutzer '{}' (ID: {}).", user.getUsername(), userId);
        return true;
    }

    @Transactional
    public void delete(Long userId) {
        logger.info("Versuche Benutzer mit ID {} zu löschen.", userId);

        Optional<User> userToDeleteOptional = userRepository.findById(userId);
        if (userToDeleteOptional.isEmpty()) {
            logger.warn("Benutzer mit ID {} konnte nicht gelöscht werden, da er nicht gefunden wurde.", userId);
            return;
        }

        User userToDelete = userToDeleteOptional.get();

        Optional<PasswordResetToken> tokenOptional = passwordResetTokenRepository.findByUser(userToDelete);

        tokenOptional.ifPresent(token -> {
            passwordResetTokenRepository.delete(token);
            logger.debug("Gelöschtes PasswordResetToken für Benutzer mit ID {}.", userId);
        });

        userRepository.deleteById(userId);
        logger.info("Benutzer mit ID {} erfolgreich gelöscht.", userId);
    }

    public User populateUserContact(User user) {
        if (user != null && user.getContact() != null) {
            logger.debug("Contact für Benutzer '{}' (ID: {}) ist vorhanden. Transiente Felder sind bereits durch @PostLoad in Contact-Entität befüllt.", user.getUsername(), user.getUserID());
        }
        return user;
    }
}