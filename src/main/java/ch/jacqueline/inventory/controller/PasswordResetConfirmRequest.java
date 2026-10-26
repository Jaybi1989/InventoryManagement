package ch.jacqueline.inventory.controller;

import ch.jacqueline.inventory.entity.User;
import ch.jacqueline.inventory.services.EmailService;
import ch.jacqueline.inventory.services.UserService;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/** Controller für die Passwort-Reset-Funktionalität.
 * Dieser Controller ermöglicht es Benutzern, ein Passwort-Reset anzufordern und das Passwort zurückzusetzen.
 * Er verwendet den EmailService zum Senden von E-Mails und den UserService für die Benutzerverwaltung.
 * * Endpunkte:
 * * POST /api/password/request-reset: Fordert ein Passwort-Reset an, indem eine E-Mail-Adresse übergeben wird.
 * * POST /api/password/reset: Setzt das Passwort zurück, indem ein Token und ein neues Passwort übergeben werden.
 * * Die Anfragen werden mit JSON-Daten verarbeitet, die die erforderlichen Informationen enthalten.
 * * @see EmailService
 * * @see UserService
 * * @RestController annotiert, um HTTP-Anfragen zu verarbeiten.
 * * @RequestMapping annotiert, um den Basis-Pfad für alle Endpunkte dieses Controllers festzulegen.
 */
@Setter
@Getter
class PasswordResetConfirmRequest {
    private String token;
    private String newPassword;

    @RestController
    @RequestMapping("/api/password")
    public static class PasswordResetController {

        private static final Logger logger = LoggerFactory.getLogger(PasswordResetController.class);

        private final EmailService emailService;
        private final UserService userService;

        /**
         * Konstruktor für den PasswordResetController.
         * Initialisiert den Controller mit den erforderlichen Services.
         *
         * @param emailService der Service zum Senden von E-Mails
         * @param userService  der Service zur Benutzerverwaltung
         */
        public PasswordResetController(EmailService emailService, UserService userService) {
            this.emailService = emailService;
            this.userService = userService;
        }

        /**
         * Endpunkt zum Anfordern eines Passwort-Resets.
         * Dieser Endpunkt nimmt eine E-Mail-Adresse entgegen und sendet eine E-Mail mit Anweisungen zum Zurücksetzen des Passworts.
         *
         * @param request das Passwort-Reset-Anfrageobjekt, das die E-Mail-Adresse enthält
         * @return eine ResponseEntity mit dem Status und einer Nachricht
         */
        @PostMapping("/request-reset")
        public ResponseEntity<?> requestPasswordReset(@RequestBody PasswordResetRequest request) {
            String userEmailAddress = request.getEmail();
            logger.info("Empfangene Passwort-Reset-Anfrage für E-Mail-Adresse: {}", userEmailAddress);

            try {
                Optional<User> userOpt = userService.findByEmail(userEmailAddress);
                if (userOpt.isEmpty()) {
                    logger.warn("Passwort-Reset angefordert für unbekannte E-Mail-Adresse: {}", userEmailAddress);
                    return ResponseEntity.ok("Falls diese E-Mail-Adresse existiert, wurde eine Nachricht mit Anweisungen gesendet.");
                }

                User user = userOpt.get();
                String resetToken = userService.createPasswordResetTokenForUser(user);

                emailService.sendPasswordResetEmail(userEmailAddress, resetToken, "http://10.33.100.25:8082");
//                emailService.sendPasswordResetEmail(userEmailAddress, resetToken, "http://localhost:8082");

                logger.info("Passwort-Reset-E-Mail wurde zum Versand an {} übergeben.", userEmailAddress);
                return ResponseEntity.ok("Falls diese E-Mail-Adresse existiert, wurde eine Nachricht mit Anweisungen gesendet.");

            } catch (Exception e) {
                logger.error("Fehler beim Anfordern des Passwort-Resets für E-Mail {}: {}", userEmailAddress, e.getMessage(), e);
                return ResponseEntity.status(500).body("Ein Fehler ist aufgetreten. Bitte versuchen Sie es später erneut.");
            }
        }

        /**
         * Endpunkt zum Zurücksetzen des Passworts.
         * Dieser Endpunkt nimmt ein Token und ein neues Passwort entgegen und setzt das Passwort des Benutzers zurück.
         *
         * @param request das Passwort-Reset-Bestätigungsobjekt, das das Token und das neue Passwort enthält
         * @return eine ResponseEntity mit dem Status und einer Nachricht
         */
        @PostMapping("/reset")
        public ResponseEntity<?> resetPassword(@RequestBody PasswordResetConfirmRequest request) {
            String token = request.getToken();
            String newPassword = request.getNewPassword();
            logger.info("Empfangene Passwort-Setzen-Anfrage mit Token: {}", token);

            if (newPassword == null || newPassword.isEmpty() || newPassword.length() < 6) {
                logger.warn("Passwort-Reset-Anfrage mit ungültigem oder zu kurzem Passwort.");
                return ResponseEntity.badRequest().body("Neues Passwort muss mindestens 6 Zeichen lang sein.");
            }

            try {
                boolean success = userService.resetPassword(token, newPassword);

                if (success) {
                    logger.info("Passwort erfolgreich zurückgesetzt.");
                    return ResponseEntity.ok("Passwort erfolgreich geändert.");
                } else {
                    logger.warn("Passwort konnte nicht zurückgesetzt werden. Ungültiger oder abgelaufener Token.");
                    return ResponseEntity.badRequest().body("Ungültiger oder abgelaufener Link. Bitte fordere einen neuen Passwort-Reset an.");
                }
            } catch (Exception e) {
                logger.error("Fehler beim Zurücksetzen des Passworts mit Token {}: {}", token, e.getMessage(), e);
                return ResponseEntity.status(500).body("Ein Fehler ist aufgetreten. Bitte versuchen Sie es später erneut.");
            }
        }
    }
}

