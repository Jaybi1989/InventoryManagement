package ch.jacqueline.inventory.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import ch.jacqueline.inventory.entity.Contact;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String senderEmail;

    @Autowired
    private UserService userService;
    @Autowired
    private ContactService contactService;

    public EmailService(JavaMailSender mailSender,
                        @org.springframework.beans.factory.annotation.Value("${spring.mail.username}") String senderEmail) {
        this.mailSender = mailSender;
        this.senderEmail = senderEmail;
        logger.info("EmailService initialisiert mit Absender: {}", senderEmail);
    }

    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text);

            mailSender.send(message);
            logger.info("E-Mail erfolgreich gesendet an '{}' mit Betreff '{}'", to, subject);
        } catch (MailException | MessagingException e) {
            logger.error("Fehler beim Senden der einfachen E-Mail an '{}' mit Betreff '{}': {}", to, subject, e.getMessage(), e);
        }
    }

    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("HTML-E-Mail erfolgreich gesendet an '{}' mit Betreff '{}'", to, subject);
        } catch (MailException | MessagingException e) {
            logger.error("Fehler beim Senden der HTML-E-Mail an '{}' mit Betreff '{}': {}", to, subject, e.getMessage(), e);
        }
    }

    public void sendPasswordResetEmail(String toUserEmail, String resetToken, String baseUrl) {
        String resetLink = baseUrl + "/reset-password?token=" + resetToken;


        userService.findByEmail(toUserEmail)
                .ifPresentOrElse(user -> {
                    Contact contact = user.getContact();

                    String welcomeName = "Sehr geehrte/r Nutzer/in";

                    if (contact != null) {
                        String firstName = contact.getFirstname();
                        String lastName = contact.getLastname();

                        if (firstName != null && !firstName.trim().isEmpty() && lastName != null && !lastName.trim().isEmpty()) {
                            welcomeName = firstName.trim() + " " + lastName.trim();
                        } else if (firstName != null && !firstName.trim().isEmpty()) {
                            welcomeName = firstName.trim();
                        } else if (lastName != null && !lastName.trim().isEmpty()) {
                            welcomeName = lastName.trim();
                        }
                    }

                    String subject = "Dein Passwort-Reset-Link für deine App";
                    String htmlContent = "<p>Hallo " + welcomeName + ",</p>"
                            + "<p>Du hast soeben eine Anfrage zum Zurücksetzen deines Passworts gemacht.</p>"
                            + "<p>Damit du schnell weiterarbeiten kannst verwende den folgenden Link:</p>"
                            + "<p><a href=\"" + resetLink + "\">Passwort zurücksetzen</a></p>"
                            + "<p>Falls du die Anfrage nicht gestellt hast melde dies bitte weiter um Sicherheitslücken ausschliessen zu können.</p>"
                            + "<p>Bitte beachte das es etwas dauern kann das du ins Login weitergeleitet wirst, wenn dir das zu lange ist kannst direkt nach dem Speichern auf den Button (Zurück zum Login klicken.</p>"
                            + "<p>Freundliche Grüsse aus Beispielhausen,<br/>Beispiel-Team</p>";

                    logger.info("Versuche personalisierte Passwort-Reset-E-Mail zu senden an: {} mit Link: {}", toUserEmail, resetLink);
                    sendHtmlEmail(toUserEmail, subject, htmlContent);
                }, () -> {
                    logger.warn("Kein Benutzer mit E-Mail-Adresse '{}' für Passwort-Reset gefunden. Keine E-Mail gesendet.", toUserEmail);

                });
    }
}