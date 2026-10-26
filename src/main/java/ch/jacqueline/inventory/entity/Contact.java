//package ch.jacqueline.inventory.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.util.Set;
//
///**
// * Entity für Kontakte.
// * Diese Klasse repräsentiert die Kontakte in der Datenbank.
// * Sie enthält Attribute wie Nachname, Vorname, Postleitzahl, Stadt, Strasse, Hausnummer und E-Mail-Adresse.
// * @see User
// */
//@Entity
//@Table(name = "Contact")
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//@EqualsAndHashCode(exclude = "users")
//@ToString(exclude = "users")
//public class Contact {
//
//    @OneToMany(mappedBy = "contact")
//    private Set<User> users;
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "ContactID")
//    private Integer contactId;
//
//    @Column(name = "Lastname")
//    private String lastname;
//
//    @Column(name = "Firstname")
//    private String firstname;
//
//    @Column(name = "Postcode")
//    private String postcode;
//
//    @Column(name = "City")
//    private String city;
//
//    @Column(name = "Streetname")
//    private String streetname;
//
//    @Column(name = "Housenumber")
//    private String housenumber;
//
//    @Column(name = "EmailAddress")
//    private String emailAddress;
//}


//package ch.jacqueline.inventory.entity;
//
//import ch.jacqueline.inventory.security.AttributeEncryptor;
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.util.Set;
//
///**
// * Entity für Kontakte.
// */
//@Entity
//@Table(name = "Contact")
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//@EqualsAndHashCode(exclude = "users")
//@ToString(exclude = "users")
//public class Contact {
//
//    @OneToMany(mappedBy = "contact")
//    private Set<User> users;
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "ContactID")
//    private Integer contactId;
//
//    @Column(name = "Lastname")
//    private String lastname;
//
//    @Column(name = "Firstname")
//    private String firstname;
//
//    // Die verschlüsselten Kontaktdaten werden in einem BLOB gespeichert.
//
//    @Column(name = "EncryptedContactData", columnDefinition = "BLOB")
//    @Convert(converter = AttributeEncryptor.class)
//    private String encryptedContactData;
//
//    // EmailAddress bleibt eine Klartext-Spalte in der Datenbank für die Suche
//    @Column(name = "EmailAddress")
//    private String emailAddress;
//
//
//    @Transient
//    private String postcode;
//    @Transient
//    private String city;
//    @Transient
//    private String streetname;
//    @Transient
//    private String housenumber;
//
//
//    // Innere Klasse für die Daten, die verschlüsselt werden sollen.
//    // Der Nachname ist nun auch Teil dieser verschlüsselten Daten.
//    @Data
//    @NoArgsConstructor
//    @AllArgsConstructor
//    public static class EncryptableContactData {
//        private String postcode;
//        private String city;
//        private String streetname;
//        private String housenumber;
//    }
//}


package ch.jacqueline.inventory.entity;

import ch.jacqueline.inventory.security.AttributeEncryptor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Entity für Kontakte.
 */
@Entity
@Table(name = "Contact")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = "users")
@ToString(exclude = "users")
public class Contact {

    private static final Logger logger = LoggerFactory.getLogger(Contact.class);
    private static final ObjectMapper objectMapper = new ObjectMapper(); // Für JSON-Serialisierung/-Deserialisierung

    @OneToMany(mappedBy = "contact")
    private Set<User> users;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ContactID")
    private Integer contactId;

    @Column(name = "Lastname")
    private String lastname;

    @Column(name = "Firstname")
    private String firstname;

    // Die verschlüsselten Kontaktdaten werden in einem BLOB gespeichert.
    // Der Converter wird das JSON (als String) verschlüsseln/entschlüsseln.
    @Column(name = "EncryptedContactData", columnDefinition = "BLOB")
    @Convert(converter = AttributeEncryptor.class)
    private String encryptedContactData;

    // EmailAddress bleibt eine Klartext-Spalte in der Datenbank für die Suche
    @Column(name = "EmailAddress")
    private String emailAddress;


    // Transient-Felder für die entschlüsselten Adressdaten
    @Transient
    private String postcode;
    @Transient
    private String city;
    @Transient
    private String streetname;
    @Transient
    private String housenumber;


    // Innere Klasse für die Daten, die verschlüsselt werden sollen.
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EncryptableContactData {
        private String postcode;
        private String city;
        private String streetname;
        private String housenumber;
    }

    // --- JPA Lifecycle Callbacks ---

    /**
     * Wird nach dem Laden der Entität aus der Datenbank aufgerufen.
     * Entschlüsselt die Daten und befüllt die transienten Felder.
     */
    @PostLoad
    public void afterLoad() {
        if (this.encryptedContactData != null && !this.encryptedContactData.isEmpty()) {
            try {
                EncryptableContactData data = objectMapper.readValue(this.encryptedContactData, EncryptableContactData.class);

                this.postcode = data.getPostcode();
                this.city = data.getCity();
                this.streetname = data.getStreetname();
                this.housenumber = data.getHousenumber();
                logger.debug("ContactID {} Adressdaten nach dem Laden entschlüsselt und befüllt.", this.contactId);
            } catch (JsonProcessingException e) {
                logger.error("Fehler beim Deserialisieren der verschlüsselten Kontaktdaten für ContactID {}: {}", this.contactId, e.getMessage());
                this.postcode = null;
                this.city = null;
                this.streetname = null;
                this.housenumber = null;
            }
        } else {
            logger.debug("ContactID {} hat keine verschlüsselten Adressdaten (EncryptedContactData ist null/leer).", this.contactId);
        }
    }

    /**
     * Wird vor dem Persistieren (neu speichern) der Entität in die Datenbank aufgerufen.
     * Sammelt die transienten Felder und serialisiert sie in den verschlüsselten String.
     */
    @PrePersist
    @PreUpdate
    public void beforeSave() {
        // Sammle die Daten aus den transienten Feldern
        EncryptableContactData data = new EncryptableContactData(
                this.postcode,
                this.city,
                this.streetname,
                this.housenumber
        );

        try {
            // Serialisiere das Objekt zu einem JSON-String
            String jsonString = objectMapper.writeValueAsString(data);
            this.encryptedContactData = jsonString;
            logger.debug("ContactID {} Adressdaten vor dem Speichern/Aktualisieren serialisiert.", this.contactId);
        } catch (JsonProcessingException e) {
            logger.error("Fehler beim Serialisieren der Kontaktdaten für ContactID {}: {}", this.contactId, e.getMessage());
            this.encryptedContactData = null;
        }
    }
}