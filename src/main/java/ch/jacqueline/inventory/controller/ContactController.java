package ch.jacqueline.inventory.controller;

import ch.jacqueline.inventory.entity.Contact;
import ch.jacqueline.inventory.services.ContactService; // Dein ContactService
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/contacts") // Basis-Pfad für Kontakte
public class ContactController {

    private static final Logger logger = LoggerFactory.getLogger(ContactController.class);

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    /**
     * Speichert oder aktualisiert einen Kontakt. Der Service übernimmt die Verschlüsselung und Entschlüsselung.
     * Empfängt und gibt ein Klartext-Contact-Objekt zurück.
     */
    @PostMapping
    public ResponseEntity<Contact> saveOrUpdateContact(@RequestBody Contact contact) {
        try {
            Contact savedContact = contactService.save(contact);
            logger.info("Kontakt '{} {}' (ID: {}) erfolgreich empfangen und zur Speicherung übergeben.", savedContact.getFirstname(), savedContact.getLastname(), savedContact.getContactId());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedContact);
        } catch (Exception e) {
            logger.error("Fehler beim Speichern oder Aktualisieren des Kontakts: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Ruft einen Kontakt anhand seiner ID ab. Der Service übernimmt die Entschlüsselung.
     * Gibt ein Klartext-Contact-Objekt zurück.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Contact> getContactById(@PathVariable Integer id) {
        try {
            Optional<Contact> contactOptional = contactService.findById(id);
            if (contactOptional.isPresent()) {
                Contact contact = contactOptional.get();
                logger.info("Kontakt mit ID {} erfolgreich abgerufen und entschlüsselt.", id);
                return ResponseEntity.ok(contact);
            } else {
                logger.warn("Kontakt mit ID {} nicht gefunden.", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } catch (Exception e) {
            logger.error("Fehler beim Abrufen des Kontakts mit ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Ruft alle Kontakte ab. Der Service übernimmt die Entschlüsselung.
     * Gibt eine Liste von Klartext-Contact-Objekten zurück.
     */
    @GetMapping
    public ResponseEntity<List<Contact>> getAllContacts() {
        try {
            List<Contact> contacts = contactService.findAll();
            logger.info("Alle {} Kontakte erfolgreich abgerufen und entschlüsselt.", contacts.size());
            return ResponseEntity.ok(contacts);
        } catch (Exception e) {
            logger.error("Fehler beim Abrufen aller Kontakte: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Löscht einen Kontakt anhand der ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteContact(@PathVariable Integer id) {
        try {
            Optional<Contact> contactOptional = contactService.findById(id); // Holt Kontakt im Klartext zum Prüfen
            if (contactOptional.isEmpty()) {
                logger.warn("Löschen verhindert: Kontakt mit ID {} nicht gefunden.", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Kontakt mit ID " + id + " nicht gefunden.");
            }
            contactService.delete(contactOptional.get());
            logger.info("Kontakt mit ID {} erfolgreich gelöscht.", id);
            return ResponseEntity.ok("Kontakt erfolgreich gelöscht.");
        } catch (IllegalStateException e) {
            logger.warn("Löschen von Kontakt {} verhindert: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Fehler beim Löschen des Kontakts mit ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Interner Serverfehler beim Löschen des Kontakts.");
        }
    }
}