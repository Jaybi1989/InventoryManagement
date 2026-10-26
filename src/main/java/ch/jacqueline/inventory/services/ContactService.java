//package ch.jacqueline.inventory.services;
//
//import ch.jacqueline.inventory.entity.Contact;
//import ch.jacqueline.inventory.entity.User;
//import ch.jacqueline.inventory.entity.Location;
//import ch.jacqueline.inventory.repository.ContactRepository;
//import ch.jacqueline.inventory.repository.LocationRepository;
//import ch.jacqueline.inventory.repository.UserRepository;
//
//import jakarta.transaction.Transactional;
//import org.springframework.dao.DuplicateKeyException;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//@Service
//public class ContactService {
//
//    private static final Logger logger = LoggerFactory.getLogger(ContactService.class);
//
//    private final ContactRepository contactRepository;
//
//    private final UserService userService;
//
//    private final LocationService locationService;
//
//    private final LocationRepository locationRepository;
//    private final UserRepository userRepository;
//
//    public ContactService(ContactRepository contactRepository, UserService userService, LocationService locationService, LocationRepository locationRepository, UserRepository userRepository) {
//        this.contactRepository = contactRepository;
//        this.userService = userService;
//        this.locationService = locationService;
//        this.locationRepository = locationRepository;
//        this.userRepository = userRepository;
//    }
//
//
//    @Transactional
//    public List<Contact> findAll() {
//        return contactRepository.findAll();
//    }
//
//    @Transactional
//    public Contact save(Contact contact) {
//        if (contact.getEmailAddress() != null && !contact.getEmailAddress().isEmpty()) {
//            Optional<Contact> existingContact = contactRepository.findByEmailAddress(contact.getEmailAddress());
//            if (existingContact.isPresent() && (contact.getContactId() == null || !existingContact.get().getContactId().equals(contact.getContactId()))) {
//                throw new DuplicateKeyException("Die E-Mail-Adresse '" + contact.getEmailAddress() + "' ist bereits vorhanden.");
//            }
//        }
//        logger.info("Kontakt '{} {}' gespeichert oder aktualisiert.", contact.getFirstname(), contact.getLastname());
//        return contactRepository.save(contact);
//    }
//
//    public Optional<Contact> findById(Integer id) {
//        return contactRepository.findById(id);
//    }
//
//    @Transactional
//    public void delete(Contact contact) {
//        logger.info("Versuche Kontakt mit ID {} zu löschen.", contact.getContactId());
//
//        Optional<Contact> managedContactOptional = contactRepository.findById(contact.getContactId());
//        if (managedContactOptional.isEmpty()) {
//            logger.warn("Kontakt mit ID {} konnte nicht gelöscht werden, da er nicht gefunden wurde.", contact.getContactId());
//            return;
//        }
//        Contact managedContact = managedContactOptional.get();
//
//        List<User> linkedUsers = userService.findAllUsersByContact(managedContact);
//        if (!linkedUsers.isEmpty()) {
//            String userNames = linkedUsers.stream()
//                    .map(user -> user.getUsername() + " (" + user.getContact().getFirstname() + " " + user.getContact().getLastname() + ")")
//                    .collect(Collectors.joining(", "));
//            logger.warn("Löschen verhindert: Kontakt mit ID {} ist noch mit Benutzern verknüpft: {}", contact.getContactId(), userNames);
//            throw new IllegalStateException("Der Kontakt '" + managedContact.getFirstname() + " " + managedContact.getLastname() +
//                    "' kann nicht gelöscht werden, da er noch mit folgenden Benutzern verknüpft ist: " + userNames + ".");
//        }
//
//        List<Location> activeLoans = locationService.findActiveLoansByContact(managedContact);
//        if (!activeLoans.isEmpty()) {
//            String articleInfo = activeLoans.stream()
//                    .map(location -> location.getArticle() != null ?
//                            location.getArticle().getArticlename() + " (ID: " + location.getArticle().getArticleId() + ")" :
//                            "Unbekannter Artikel (ID: unbekannt)")
//                    .collect(Collectors.joining(", "));
//            logger.warn("Löschen verhindert: Kontakt mit ID {} hat noch aktive Ausleihen: {}", contact.getContactId(), articleInfo);
//            throw new IllegalStateException("Der Kontakt '" + managedContact.getFirstname() + " " + managedContact.getLastname() +
//                    "' kann nicht gelöscht werden, da er noch aktive Ausleihen für die Artikel: " + articleInfo + " besitzt.");
//        }
//
//        List<Location> allAssociatedLocations = locationRepository.findAllByContact(managedContact);
//        if (!allAssociatedLocations.isEmpty()) {
//            logger.info("Lösche {} zugeordnete Ausleih-Einträge für Kontakt mit ID {}.", allAssociatedLocations.size(), contact.getContactId());
//            locationRepository.deleteAll(allAssociatedLocations);
//        } else {
//            logger.info("Keine zu löschenden Ausleih-Einträge für Kontakt mit ID {} gefunden.", contact.getContactId());
//        }
//        try {
//            contactRepository.delete(managedContact);
//            logger.info("Kontakt mit ID {} erfolgreich gelöscht.", contact.getContactId());
//        } catch (org.springframework.dao.DataIntegrityViolationException e) {
//            logger.error("Datenbank-Integritätsfehler beim Löschen des Kontakts mit ID {}: {}", contact.getContactId(), e.getMessage());
//            throw new IllegalStateException("Kann Kontakt nicht löschen! Es existieren noch abhängige Daten (z.B. Ausleihen), die nicht entfernt werden konnten. Details: " + e.getMessage(), e);
//        } catch (Exception e) {
//            logger.error("Unerwarteter Fehler beim Löschen des Kontakts mit ID {}: {}", contact.getContactId(), e.getMessage(), e);
//            throw new RuntimeException("Ein unerwarteter Fehler ist beim Löschen des Kontakts aufgetreten.", e);
//        }
//    }
//
//}








//package ch.jacqueline.inventory.services;
//
//import ch.jacqueline.inventory.entity.Contact;
//import ch.jacqueline.inventory.entity.User;
//import ch.jacqueline.inventory.entity.Location;
//import ch.jacqueline.inventory.repository.ContactRepository;
//import ch.jacqueline.inventory.repository.LocationRepository;
//import ch.jacqueline.inventory.repository.UserRepository;
//import ch.jacqueline.inventory.security.CipherService;
//import ch.jacqueline.inventory.security.KeyManagementService;
//import ch.jacqueline.inventory.security.MacService;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//import org.springframework.dao.DuplicateKeyException;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import javax.crypto.SecretKey;
//import java.nio.charset.StandardCharsets;
//import java.util.Base64;
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//@Service
//public class ContactService {
//
//    private static final Logger logger = LoggerFactory.getLogger(ContactService.class);
//
//    private final ContactRepository contactRepository;
//    private final UserService userService;
//    private final LocationService locationService;
//    private final LocationRepository locationRepository;
//    private final UserRepository userRepository;
//    private final CipherService cipherService;
//    private final MacService macService;
//    private final KeyManagementService keyManagementService;
//    private final ObjectMapper objectMapper;
//
//    public ContactService(ContactRepository contactRepository, UserService userService, LocationService locationService,
//                          LocationRepository locationRepository, UserRepository userRepository,
//                          CipherService cipherService, MacService macService, KeyManagementService keyManagementService, ObjectMapper objectMapper) {
//        this.contactRepository = contactRepository;
//        this.userService = userService;
//        this.locationService = locationService;
//        this.locationRepository = locationRepository;
//        this.userRepository = userRepository;
//        this.cipherService = cipherService;
//        this.macService = macService;
//        this.keyManagementService = keyManagementService;
//        this.objectMapper = objectMapper;
//    }
//
//    @Transactional
//    public List<Contact> findAll() {
//        logger.info("Alle Kontakte werden aus der Datenbank geladen.");
//        // Jeden geladenen Kontakt nachladen und entschlüsseln
//        return contactRepository.findAll().stream()
//                .map(this::processContactAfterLoad)
//                .collect(Collectors.toList());
//    }
//
//    @Transactional
//    public Contact save(Contact contact) {
//        // ... (Ihr bestehender Code für E-Mail-Prüfung) ...
//
//        // Logger: Prüfen der eingehenden Klartext-Daten
//        logger.info("SAVE: Eingehender Klartext-Kontakt - ID: {}, Lastname: {}, Firstname: {}, Email: {}",
//                contact.getContactId(), contact.getLastname(), contact.getFirstname(), contact.getEmailAddress());
//
//        try {
//            // Die relevanten transienten Daten für die Verschlüsselung sammeln
//            Contact.EncryptableContactData encryptableData = new Contact.EncryptableContactData(
//                    contact.getLastname(),
//                    contact.getFirstname(),
//                    contact.getPostcode(),
//                    contact.getCity(),
//                    contact.getStreetname(),
//                    contact.getHousenumber()
//            );
//
//            // Logger: Prüfen des EncryptableContactData Objekts
//            logger.info("SAVE: EncryptableContactData vor JSON-Konvertierung: {}", encryptableData);
//
//            String plainJson = objectMapper.writeValueAsString(encryptableData);
//
//            // Logger: Prüfen des Klartext-JSON-Strings
//            logger.info("SAVE: Klartext-JSON für Verschlüsselung: {}", plainJson);
//
//            byte[] messageBytes = plainJson.getBytes(StandardCharsets.UTF_8);
//
//            SecretKey encryptionKey = keyManagementService.getEncryptionKey();
//            SecretKey macKey = keyManagementService.getMacKey();
//
//            byte[] encryptedData = cipherService.encrypt(messageBytes, encryptionKey);
//            String encryptedDataBase64 = Base64.getEncoder().encodeToString(encryptedData);
//
//            byte[] mac = macService.calculateMac(encryptedData, macKey);
//            String macBase64 = Base64.getEncoder().encodeToString(mac);
//
//            String combinedEncryptedString = encryptedDataBase64 + ":" + macBase64;
//
//            // Logger: Prüfen des finalen kombinierten verschlüsselten Strings
//            logger.info("SAVE: Kombinierter verschlüsselter String (encryptedContactData): {}", combinedEncryptedString);
//
//            contact.setEncryptedContactData(combinedEncryptedString);
//
//            // Logger: Prüfen, ob encryptedContactData im Contact-Objekt gesetzt wurde
//            logger.info("SAVE: Contact-Objekt vor dem Speichern - encryptedContactData: {}", contact.getEncryptedContactData());
//
//            // Setze transienten Felder auf null
//            contact.setLastname(null);
//            contact.setFirstname(null);
//            contact.setPostcode(null);
//            contact.setCity(null);
//            contact.setStreetname(null);
//            contact.setHousenumber(null);
//
//        } catch (Exception e) {
//            logger.error("SAVE: Fehler beim Verschlüsseln der Kontaktdaten: {}", e.getMessage(), e);
//            throw new RuntimeException("Fehler beim Verarbeiten der Kontaktdaten für die Verschlüsselung.", e);
//        }
//
//        logger.info("SAVE: Kontakt (Email: {}) wird in der Datenbank gespeichert.", contact.getEmailAddress());
//        Contact savedContact = contactRepository.save(contact);
//
//        // Logger: Prüfen des gespeicherten Kontakts, insbesondere encryptedContactData
//        logger.info("SAVE: Gespeicherter Kontakt aus Repository - ID: {}, encryptedContactData Länge: {}",
//                savedContact.getContactId(), (savedContact.getEncryptedContactData() != null ? savedContact.getEncryptedContactData().length() : "NULL"));
//
//
//        return processContactAfterLoad(savedContact);
//    }
//    public Optional<Contact> findById(Integer id) {
//        logger.info("Kontakt mit ID {} wird aus der Datenbank geladen.", id);
//        // Nach dem Laden entschlüsseln
//        return contactRepository.findById(id)
//                .map(this::processContactAfterLoad);
//    }
//
//    public Contact processContactAfterLoad(Contact contact) {
//        if (contact == null) {
//            logger.info("LOAD: Eingehender Kontakt ist null.");
//            return null;
//        }
//
//        // Logger: Prüfen des Contact-Objekts, bevor Entschlüsselung startet
//        logger.info("LOAD: Eingehender Kontakt für Entschlüsselung - ID: {}, encryptedContactData Länge: {}",
//                contact.getContactId(), (contact.getEncryptedContactData() != null ? contact.getEncryptedContactData().length() : "NULL"));
//
//        String combinedEncryptedString = contact.getEncryptedContactData();
//
//        if (combinedEncryptedString == null || combinedEncryptedString.isEmpty()) {
//            logger.warn("LOAD: 'encryptedContactData' Feld für Kontakt (ID: {}) ist leer oder null. Entschlüsselung nicht möglich.", contact.getContactId());
//            contact.setLastname(null);
//            contact.setFirstname(null);
//            contact.setPostcode(null);
//            contact.setCity(null);
//            contact.setStreetname(null);
//            contact.setHousenumber(null);
//            return contact;
//        }
//
//        try {
//            // Logger: Prüfen des kombinierten verschlüsselten Strings
//            logger.info("LOAD: Kombinierter verschlüsselter String: {}", combinedEncryptedString);
//
//            String[] parts = combinedEncryptedString.split(":", 2);
//            if (parts.length != 2) {
//                logger.warn("LOAD: Sicherheitswarnung: 'encryptedContactData' für Kontakt (ID: {}) hat ungültiges Format. MAC fehlt oder ist falsch formatiert.", contact.getContactId());
//                contact.setLastname(null);
//                contact.setFirstname(null);
//                contact.setPostcode(null);
//                contact.setCity(null);
//                contact.setStreetname(null);
//                contact.setHousenumber(null);
//                return contact;
//            }
//
//            // Logger: Prüfen der getrennten Teile
//            logger.info("LOAD: Getrennte Teile - EncryptedData Part Länge: {}, MAC Part Länge: {}", parts[0].length(), parts[1].length());
//
//
//            byte[] encryptedData = Base64.getDecoder().decode(parts[0]);
//            byte[] mac = Base64.getDecoder().decode(parts[1]);
//
//            // Logger: Prüfen der entschlüsselten Bytes
//            logger.info("LOAD: Entschlüsselte Base64-Teile in Bytes konvertiert.");
//
//            SecretKey macKey = keyManagementService.getMacKey();
//            if (!macService.verifyMac(encryptedData, macKey, mac)) {
//                logger.error("LOAD: Sicherheitsfehler: MAC-Verifizierung für Kontakt (ID: {}) fehlgeschlagen! Datenintegrität kompromittiert oder Schlüssel geändert.", contact.getContactId());
//                contact.setLastname(null);
//                contact.setFirstname(null);
//                contact.setPostcode(null);
//                contact.setCity(null);
//                contact.setStreetname(null);
//                contact.setHousenumber(null);
//                return contact;
//            }
//
//            SecretKey encryptionKey = keyManagementService.getEncryptionKey();
//            byte[] decryptedMessageBytes = cipherService.decrypt(encryptedData, encryptionKey);
//            String decryptedJson = new String(decryptedMessageBytes, StandardCharsets.UTF_8);
//
//            // Logger: Prüfen des entschlüsselten JSON-Strings
//            logger.info("LOAD: Entschlüsselter JSON-String: {}", decryptedJson);
//
//            Contact.EncryptableContactData decryptedData = objectMapper.readValue(decryptedJson, Contact.EncryptableContactData.class);
//
//            // Logger: Prüfen des deserialisierten EncryptableContactData Objekts
//            logger.info("LOAD: Deserialisierte Daten: Lastname: {}, Firstname: {}", decryptedData.getLastname(), decryptedData.getFirstname());
//
//            // Befüllen der transienten Felder des Contact-Objekts
//            contact.setLastname(decryptedData.getLastname());
//            contact.setFirstname(decryptedData.getFirstname());
//            contact.setPostcode(decryptedData.getPostcode());
//            contact.setCity(decryptedData.getCity());
//            contact.setStreetname(decryptedData.getStreetname());
//            contact.setHousenumber(decryptedData.getHousenumber());
//
//        } catch (Exception e) {
//            logger.error("LOAD: Fehler beim Entschlüsseln oder Verarbeiten des Kontakts (ID: {}): {}", contact.getContactId(), e.getMessage(), e);
//            contact.setLastname(null);
//            contact.setFirstname(null);
//            contact.setPostcode(null);
//            contact.setCity(null);
//            contact.setStreetname(null);
//            contact.setHousenumber(null);
//        }
//        return contact;
//    }
//
//
//
//    @Transactional
//    public void delete(Contact contact) {
//        // Beim Löschen wird der Klartext-Kontakt geladen, um die Geschäftslogik zu prüfen (z.B. Verknüpfungen)
//        // Die Verschlüsselung der Daten im "lastname"-Feld spielt für den Löschvorgang selbst keine Rolle.
//        Optional<Contact> managedContactOptional = contactRepository.findById(contact.getContactId());
//        if (managedContactOptional.isEmpty()) {
//            logger.warn("Kontakt mit ID {} konnte nicht gelöscht werden, da er nicht gefunden wurde.", contact.getContactId());
//            return;
//        }
//        Contact managedContact = managedContactOptional.get();
//
//        // Um die Logging-Nachricht korrekt auszugeben, wird der Kontakt hier kurz entschlüsselt.
//        // Das ist wichtig, da die Log-Nachricht Vor- und Nachnamen anzeigt, welche transient sind.
//        managedContact = processContactAfterLoad(managedContact);
//
//        logger.info("Versuche Kontakt '{} {}' (ID: {}) zu löschen.", managedContact.getFirstname(), managedContact.getLastname(), managedContact.getContactId());
//
//        List<User> linkedUsers = userService.findAllUsersByContact(managedContact);
//        if (!linkedUsers.isEmpty()) {
//            String userNames = linkedUsers.stream()
//                    .map(user -> user.getUsername() + " (" + user.getContact().getFirstname() + " " + user.getContact().getLastname() + ")")
//                    .collect(Collectors.joining(", "));
//            logger.warn("Löschen verhindert: Kontakt mit ID {} ist noch mit Benutzern verknüpft: {}", managedContact.getContactId(), userNames);
//            throw new IllegalStateException("Der Kontakt '" + managedContact.getFirstname() + " " + managedContact.getLastname() +
//                    "' kann nicht gelöscht werden, da er noch mit folgenden Benutzern verknüpft ist: " + userNames + ".");
//        }
//
//        List<Location> activeLoans = locationService.findActiveLoansByContact(managedContact);
//        if (!activeLoans.isEmpty()) {
//            String articleInfo = activeLoans.stream()
//                    .map(location -> location.getArticle() != null ?
//                            location.getArticle().getArticlename() + " (ID: " + location.getArticle().getArticleId() + ")" :
//                            "Unbekannter Artikel (ID: unbekannt)")
//                    .collect(Collectors.joining(", "));
//            logger.warn("Löschen verhindert: Kontakt mit ID {} hat noch aktive Ausleihen: {}", managedContact.getContactId(), articleInfo);
//            throw new IllegalStateException("Der Kontakt '" + managedContact.getFirstname() + " " + managedContact.getLastname() +
//                    "' kann nicht gelöscht werden, da er noch aktive Ausleihen für die Artikel: " + articleInfo + " besitzt.");
//        }
//
//        List<Location> allAssociatedLocations = locationRepository.findAllByContact(managedContact);
//        if (!allAssociatedLocations.isEmpty()) {
//            logger.info("Lösche {} zugeordnete Ausleih-Einträge für Kontakt mit ID {}.", allAssociatedLocations.size(), managedContact.getContactId());
//            locationRepository.deleteAll(allAssociatedLocations);
//        } else {
//            logger.info("Keine zu löschenden Ausleih-Einträge für Kontakt mit ID {} gefunden.", contact.getContactId());
//        }
//        try {
//            contactRepository.delete(managedContact);
//            logger.info("Kontakt mit ID {} erfolgreich gelöscht.", managedContact.getContactId());
//        } catch (org.springframework.dao.DataIntegrityViolationException e) {
//            logger.error("Datenbank-Integritätsfehler beim Löschen des Kontakts mit ID {}: {}", contact.getContactId(), e.getMessage());
//            throw new IllegalStateException("Kann Kontakt nicht löschen! Es existieren noch abhängige Daten (z.B. Ausleihen), die nicht entfernt werden konnten. Details: " + e.getMessage(), e);
//        } catch (Exception e) {
//            logger.error("Unerwarteter Fehler beim Löschen des Kontakts mit ID {}: {}", contact.getContactId(), e.getMessage(), e);
//            throw new RuntimeException("Ein unerwarteter Fehler ist beim Löschen des Kontakts aufgetreten.", e);
//        }
//    }
//}



//package ch.jacqueline.inventory.services;
//
//import ch.jacqueline.inventory.entity.Contact;
//import ch.jacqueline.inventory.entity.User;
//import ch.jacqueline.inventory.entity.Location;
//import ch.jacqueline.inventory.repository.ContactRepository;
//import ch.jacqueline.inventory.repository.LocationRepository;
//import ch.jacqueline.inventory.repository.UserRepository;
//import ch.jacqueline.inventory.security.CipherService;
//import ch.jacqueline.inventory.security.KeyManagementService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import javax.crypto.SecretKey;
//import javax.crypto.AEADBadTagException; // <-- Neuen Import hinzufügen, um spezifischen GCM-Fehler abzufangen
//import java.nio.charset.StandardCharsets;
//import java.util.Base64;
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//@Service
//public class ContactService {
//
//    private static final Logger logger = LoggerFactory.getLogger(ContactService.class);
//
//    private final ContactRepository contactRepository;
//    private final UserService userService;
//    private final LocationService locationService;
//    private final LocationRepository locationRepository;
//    private final UserRepository userRepository;
//    private final CipherService cipherService;
//    private final KeyManagementService keyManagementService;
//    private final ObjectMapper objectMapper;
//
//    public ContactService(ContactRepository contactRepository, UserService userService, LocationService locationService,
//                          LocationRepository locationRepository, UserRepository userRepository,
//                          CipherService cipherService,
//                          // MacService macService, // <-- Diesen Parameter entfernen
//                          KeyManagementService keyManagementService, ObjectMapper objectMapper) {
//        this.contactRepository = contactRepository;
//        this.userService = userService;
//        this.locationService = locationService;
//        this.locationRepository = locationRepository;
//        this.userRepository = userRepository;
//        this.cipherService = cipherService;
//        // this.macService = macService; // <-- Diese Initialisierung entfernen
//        this.keyManagementService = keyManagementService;
//        this.objectMapper = objectMapper;
//    }
//
//    @Transactional
//    public List<Contact> findAll() {
//        logger.info("Alle Kontakte werden aus der Datenbank geladen.");
//        // Jeden geladenen Kontakt nachladen und entschlüsseln
//        return contactRepository.findAll().stream()
//                .map(this::processContactAfterLoad)
//                .collect(Collectors.toList());
//    }
//
//    @Transactional
//    public Contact save(Contact contact) {
//        logger.info("SAVE: Eingehender Klartext-Kontakt - ID: {}, Lastname: {}, Firstname: {}, Email: {}",
//                contact.getContactId(), contact.getLastname(), contact.getFirstname(), contact.getEmailAddress());
//
//        try {
//            Contact.EncryptableContactData encryptableData = new Contact.EncryptableContactData(
//                    contact.getLastname(),
//                    contact.getFirstname(),
//                    contact.getPostcode(),
//                    contact.getCity(),
//                    contact.getStreetname(),
//                    contact.getHousenumber()
//            );
//            logger.info("SAVE: EncryptableContactData vor JSON-Konvertierung: {}", encryptableData);
//
//            String plainJson = objectMapper.writeValueAsString(encryptableData);
//
//            logger.info("SAVE: Klartext-JSON für Verschlüsselung: {}", plainJson);
//
//            byte[] messageBytes = plainJson.getBytes(StandardCharsets.UTF_8);
//
//            SecretKey encryptionKey = keyManagementService.getEncryptionKey();
//            byte[] encryptedDataWithIvAndTag = cipherService.encrypt(messageBytes, encryptionKey);
//            String combinedEncryptedString = Base64.getEncoder().encodeToString(encryptedDataWithIvAndTag);
//
//            logger.info("SAVE: Kombinierter verschlüsselter String (encryptedContactData): {}", combinedEncryptedString);
//
//            contact.setEncryptedContactData(combinedEncryptedString);
//
//            logger.info("SAVE: Contact-Objekt vor dem Speichern - encryptedContactData: {}", contact.getEncryptedContactData());
//
//            contact.setLastname(null);
//            contact.setFirstname(null);
//            contact.setPostcode(null);
//            contact.setCity(null);
//            contact.setStreetname(null);
//            contact.setHousenumber(null);
//
//        } catch (Exception e) {
//            logger.error("SAVE: Fehler beim Verschlüsseln der Kontaktdaten: {}", e.getMessage(), e);
//            throw new RuntimeException("Fehler beim Verarbeiten der Kontaktdaten für die Verschlüsselung.", e);
//        }
//
//        logger.info("SAVE: Kontakt (Email: {}) wird in der Datenbank gespeichert.", contact.getEmailAddress());
//        Contact savedContact = contactRepository.save(contact);
//
//        logger.info("SAVE: Gespeicherter Kontakt aus Repository - ID: {}, encryptedContactData Länge: {}",
//                savedContact.getContactId(), (savedContact.getEncryptedContactData() != null ? savedContact.getEncryptedContactData().length() : "NULL"));
//
//
//        return processContactAfterLoad(savedContact);
//    }
//    public Optional<Contact> findById(Integer id) {
//        logger.info("Kontakt mit ID {} wird aus der Datenbank geladen.", id);
//        return contactRepository.findById(id)
//                .map(this::processContactAfterLoad);
//    }
//
//    public Contact processContactAfterLoad(Contact contact) {
//        if (contact == null) {
//            logger.info("LOAD: Eingehender Kontakt ist null.");
//            return null;
//        }
//        logger.info("LOAD: Eingehender Kontakt für Entschlüsselung - ID: {}, encryptedContactData Länge: {}",
//                contact.getContactId(), (contact.getEncryptedContactData() != null ? contact.getEncryptedContactData().length() : "NULL"));
//
//        String combinedEncryptedString = contact.getEncryptedContactData();
//
//        if (combinedEncryptedString == null || combinedEncryptedString.isEmpty()) {
//            logger.warn("LOAD: 'encryptedContactData' Feld für Kontakt (ID: {}) ist leer oder null. Entschlüsselung nicht möglich.", contact.getContactId());
//            contact.setLastname(null);
//            contact.setFirstname(null);
//            contact.setPostcode(null);
//            contact.setCity(null);
//            contact.setStreetname(null);
//            contact.setHousenumber(null);
//            return contact;
//        }
//
//        try {
//            logger.info("LOAD: Kombinierter verschlüsselter String: {}", combinedEncryptedString);
//
//            byte[] encryptedDataWithIvAndTag = Base64.getDecoder().decode(combinedEncryptedString);
//
//            logger.info("LOAD: Entschlüsselte Base64-Teile in Bytes konvertiert.");
//
//            SecretKey encryptionKey = keyManagementService.getEncryptionKey();
//            byte[] decryptedMessageBytes = cipherService.decrypt(encryptedDataWithIvAndTag, encryptionKey);
//            String decryptedJson = new String(decryptedMessageBytes, StandardCharsets.UTF_8);
//
//            logger.info("LOAD: Entschlüsselter JSON-String: {}", decryptedJson);
//
//            Contact.EncryptableContactData decryptedData = objectMapper.readValue(decryptedJson, Contact.EncryptableContactData.class);
//
//            logger.info("LOAD: Deserialisierte Daten: Lastname: {}, Firstname: {}", decryptedData.getLastname(), decryptedData.getFirstname());
//
//            // Befüllen der transienten Felder des Contact-Objekts
//            contact.setLastname(decryptedData.getLastname());
//            contact.setFirstname(decryptedData.getFirstname());
//            contact.setPostcode(decryptedData.getPostcode());
//            contact.setCity(decryptedData.getCity());
//            contact.setStreetname(decryptedData.getStreetname());
//            contact.setHousenumber(decryptedData.getHousenumber());
//
//        } catch (AEADBadTagException e) {
//            logger.error("LOAD: Sicherheitsfehler: Authentifizierungstag-Verifizierung für Kontakt (ID: {}) fehlgeschlagen! Datenintegrität kompromittiert oder Schlüssel geändert. (GCM Tag Mismatch)", contact.getContactId(), e);
//            contact.setLastname(null);
//            contact.setFirstname(null);
//            contact.setPostcode(null);
//            contact.setCity(null);
//            contact.setStreetname(null);
//            contact.setHousenumber(null);
//        } catch (Exception e) {
//            logger.error("LOAD: Fehler beim Entschlüsseln oder Verarbeiten des Kontakts (ID: {}): {}", contact.getContactId(), e.getMessage(), e);
//            contact.setLastname(null);
//            contact.setFirstname(null);
//            contact.setPostcode(null);
//            contact.setCity(null);
//            contact.setStreetname(null);
//            contact.setHousenumber(null);
//        }
//        return contact;
//    }
//    @Transactional
//    public void delete(Contact contact) {
//        // Beim Löschen wird der Klartext-Kontakt geladen, um die Geschäftslogik zu prüfen (z.B. Verknüpfungen)
//        // Die Verschlüsselung der Daten im "lastname"-Feld spielt für den Löschvorgang selbst keine Rolle.
//        Optional<Contact> managedContactOptional = contactRepository.findById(contact.getContactId());
//        if (managedContactOptional.isEmpty()) {
//            logger.warn("Kontakt mit ID {} konnte nicht gelöscht werden, da er nicht gefunden wurde.", contact.getContactId());
//            return;
//        }
//        Contact managedContact = managedContactOptional.get();
//
//        // Um die Logging-Nachricht korrekt auszugeben, wird der Kontakt hier kurz entschlüsselt.
//        // Das ist wichtig, da die Log-Nachricht Vor- und Nachnamen anzeigt, welche transient sind.
//        managedContact = processContactAfterLoad(managedContact);
//
//        logger.info("Versuche Kontakt '{} {}' (ID: {}) zu löschen.", managedContact.getFirstname(), managedContact.getLastname(), managedContact.getContactId());
//
//        List<User> linkedUsers = userService.findAllUsersByContact(managedContact);
//        if (!linkedUsers.isEmpty()) {
//            String userNames = linkedUsers.stream()
//                    .map(user -> user.getUsername() + " (" + user.getContact().getFirstname() + " " + user.getContact().getLastname() + ")")
//                    .collect(Collectors.joining(", "));
//            logger.warn("Löschen verhindert: Kontakt mit ID {} ist noch mit Benutzern verknüpft: {}", managedContact.getContactId(), userNames);
//            throw new IllegalStateException("Der Kontakt '" + managedContact.getFirstname() + " " + managedContact.getLastname() +
//                    "' kann nicht gelöscht werden, da er noch mit folgenden Benutzern verknüpft ist: " + userNames + ".");
//        }
//
//        List<Location> activeLoans = locationService.findActiveLoansByContact(managedContact);
//        if (!activeLoans.isEmpty()) {
//            String articleInfo = activeLoans.stream()
//                    .map(location -> location.getArticle() != null ?
//                            location.getArticle().getArticlename() + " (ID: " + location.getArticle().getArticleId() + ")" :
//                            "Unbekannter Artikel (ID: unbekannt)")
//                    .collect(Collectors.joining(", "));
//            logger.warn("Löschen verhindert: Kontakt mit ID {} hat noch aktive Ausleihen: {}", managedContact.getContactId(), articleInfo);
//            throw new IllegalStateException("Der Kontakt '" + managedContact.getFirstname() + " " + managedContact.getLastname() +
//                    "' kann nicht gelöscht werden, da er noch aktive Ausleihen für die Artikel: " + articleInfo + " besitzt.");
//        }
//
//        List<Location> allAssociatedLocations = locationRepository.findAllByContact(managedContact);
//        if (!allAssociatedLocations.isEmpty()) {
//            logger.info("Lösche {} zugeordnete Ausleih-Einträge für Kontakt mit ID {}.", allAssociatedLocations.size(), managedContact.getContactId());
//            locationRepository.deleteAll(allAssociatedLocations);
//        } else {
//            logger.info("Keine zu löschenden Ausleih-Einträge für Kontakt mit ID {} gefunden.", contact.getContactId());
//        }
//        try {
//            contactRepository.delete(managedContact);
//            logger.info("Kontakt mit ID {} erfolgreich gelöscht.", managedContact.getContactId());
//        } catch (org.springframework.dao.DataIntegrityViolationException e) {
//            logger.error("Datenbank-Integritätsfehler beim Löschen des Kontakts mit ID {}: {}", contact.getContactId(), e.getMessage());
//            throw new IllegalStateException("Kann Kontakt nicht löschen! Es existieren noch abhängige Daten (z.B. Ausleihen), die nicht entfernt werden konnten. Details: " + e.getMessage(), e);
//        } catch (Exception e) {
//            logger.error("Unerwarteter Fehler beim Löschen des Kontakts mit ID {}: {}", contact.getContactId(), e.getMessage(), e);
//            throw new RuntimeException("Ein unerwarteter Fehler ist beim Löschen des Kontakts aufgetreten.", e);
//        }
//    }
//}




package ch.jacqueline.inventory.services;

import ch.jacqueline.inventory.entity.Contact;
import ch.jacqueline.inventory.entity.User;
import ch.jacqueline.inventory.entity.Location;
import ch.jacqueline.inventory.repository.ContactRepository;
import ch.jacqueline.inventory.repository.LocationRepository;
import ch.jacqueline.inventory.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ContactService {

    private static final Logger logger = LoggerFactory.getLogger(ContactService.class);

    private final ContactRepository contactRepository;
    private final LocationService locationService;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper; // Kann entfernt werden, wenn nicht anderswo im Service genutzt

    public ContactService(ContactRepository contactRepository, LocationService locationService,
                          LocationRepository locationRepository, UserRepository userRepository,
                          ObjectMapper objectMapper) {
        this.contactRepository = contactRepository;
        this.locationService = locationService;
        this.locationRepository = locationRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<Contact> findAll() {
        logger.info("Alle Kontakte werden aus der Datenbank geladen.");
        return contactRepository.findAll();
    }

    @Transactional
    public Contact save(Contact contact) {
        logger.info("SAVE: Eingehender Klartext-Kontakt - ID: {}, Lastname: {}, Firstname: {}, Email: {}",
                contact.getContactId(), contact.getLastname(), contact.getFirstname(), contact.getEmailAddress());

        logger.info("SAVE: Kontakt (Email: {}) wird in der Datenbank gespeichert.", contact.getEmailAddress());
        Contact savedContact = contactRepository.save(contact);

        logger.info("SAVE: Gespeicherter Kontakt aus Repository - ID: {}, Lastname: {}, Firstname: {}",
                savedContact.getContactId(), savedContact.getLastname(), savedContact.getFirstname());

        return savedContact;
    }

    @Transactional(readOnly = true)
    public Optional<Contact> findById(Integer id) {
        logger.info("Kontakt mit ID {} wird aus der Datenbank geladen.", id);
        return contactRepository.findById(id);
    }

    @Transactional
    public void delete(Contact contact) {
        Optional<Contact> managedContactOptional = contactRepository.findById(contact.getContactId());
        if (managedContactOptional.isEmpty()) {
            logger.warn("Löschen fehlgeschlagen: Kontakt mit ID {} konnte nicht gefunden werden.", contact.getContactId());
            throw new IllegalStateException("Kontakt mit ID " + contact.getContactId() + " zum Löschen nicht gefunden.");
        }
        Contact managedContact = managedContactOptional.get();

        logger.info("Versuche Kontakt '{} {}' (ID: {}) zu löschen.",
                managedContact.getFirstname() != null ? managedContact.getFirstname() : "N/A",
                managedContact.getLastname() != null ? managedContact.getLastname() : "N/A",
                managedContact.getContactId());

        List<User> linkedUsers = userRepository.findAllByContact(managedContact);
        if (!linkedUsers.isEmpty()) {
            String userNames = linkedUsers.stream()
                    .map(user -> {
                        String userContactName = "Unbekannter Kontakt";
                        if (user.getContact() != null) {
                            Contact processedContact = user.getContact();
                            userContactName = (processedContact.getFirstname() != null ? processedContact.getFirstname() : "") +
                                    " " +
                                    (processedContact.getLastname() != null ? processedContact.getLastname() : "");
                            if (userContactName.trim().isEmpty() && processedContact.getEmailAddress() != null) {
                                userContactName = processedContact.getEmailAddress();
                            }
                        }
                        return user.getUsername() + " (" + userContactName.trim() + ")";
                    })
                    .collect(Collectors.joining(", "));
            logger.warn("Löschen verhindert: Kontakt mit ID {} ist noch mit Benutzern verknüpft: {}", managedContact.getContactId(), userNames);
            throw new IllegalStateException("Der Kontakt '" + (managedContact.getFirstname() != null ? managedContact.getFirstname() : "") + " " +
                    (managedContact.getLastname() != null ? managedContact.getLastname() : "") +
                    "' kann nicht gelöscht werden, da er noch mit folgenden Benutzern verknüpft ist: " + userNames + ".");
        }

        List<Location> activeLoans = locationService.findActiveLoansByContact(managedContact);
        if (!activeLoans.isEmpty()) {
            String articleInfo = activeLoans.stream()
                    .map(location -> location.getArticle() != null ?
                            location.getArticle().getArticlename() + " (ID: " + location.getArticle().getArticleId() + ")" :
                            "Unbekannter Artikel (ID: unbekannt)")
                    .collect(Collectors.joining(", "));
            logger.warn("Löschen verhindert: Kontakt mit ID {} hat noch aktive Ausleihen: {}", managedContact.getContactId(), articleInfo);
            throw new IllegalStateException("Der Kontakt '" + (managedContact.getFirstname() != null ? managedContact.getFirstname() : "") + " " +
                    (managedContact.getLastname() != null ? managedContact.getLastname() : "") +
                    "' kann nicht gelöscht werden, da er noch aktive Ausleihen für die Artikel: " + articleInfo + " besitzt.");
        }

        List<Location> allAssociatedLocations = locationRepository.findAllByContact(managedContact);
        if (!allAssociatedLocations.isEmpty()) {
            logger.info("Lösche {} zugeordnete Ausleih-Einträge für Kontakt mit ID {}.", allAssociatedLocations.size(), managedContact.getContactId());
            locationRepository.deleteAll(allAssociatedLocations);
        } else {
            logger.info("Keine zu löschenden Ausleih-Einträge für Kontakt mit ID {} gefunden.", contact.getContactId());
        }

        try {
            contactRepository.delete(managedContact);
            logger.info("Kontakt mit ID {} erfolgreich gelöscht.", managedContact.getContactId());
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            logger.error("Datenbank-Integritätsfehler beim Löschen des Kontakts mit ID {}: {}", contact.getContactId(), e.getMessage());
            throw new IllegalStateException("Kann Kontakt nicht löschen! Es existieren noch abhängige Daten (z.B. Ausleihen), die nicht entfernt werden konnten. Details: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unerwarteter Fehler beim Löschen des Kontakts mit ID {}: {}", contact.getContactId(), e.getMessage(), e);
            throw new RuntimeException("Ein unerwarteter Fehler ist beim Löschen des Kontakts aufgetreten.", e);
        }
    }
}
