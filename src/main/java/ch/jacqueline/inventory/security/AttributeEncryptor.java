package ch.jacqueline.inventory.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.BeanFactory; // Import für BeanFactory
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Konvertiert Attribute zwischen Klartext und verschlüsseltem Format für die Datenbank.
 * Dieser Konverter verwendet den CipherService und KeyManagementService, um Attribute zu verschlüsseln und zu entschlüsseln.
 * Er ist als Spring-Bean registriert, um die Abhängigkeiten automatisch zu injizieren.
 * @see CipherService
 * @see KeyManagementService
 */
@Converter()
@Component
public class AttributeEncryptor implements AttributeConverter<String, String> {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AttributeEncryptor.class);
    private static BeanFactory beanFactory;

    @Autowired
    public void setBeanFactory(BeanFactory beanFactory) {
        AttributeEncryptor.beanFactory = beanFactory;
    }

    private CipherService cipherService;
    private KeyManagementService keyManagementService;

    private void ensureServicesInitialized() {
        if (beanFactory == null) {
            logger.error("BeanFactory nicht im AttributeEncryptor gesetzt. Kann Sicherheitsdienste nicht initialisieren.");
            throw new IllegalStateException("BeanFactory nicht gesetzt. Dies deutet auf ein Problem mit der Spring-Initialisierung hin.");
        }
        if (cipherService == null) {
            try {
                this.cipherService = beanFactory.getBean(CipherService.class);
                this.keyManagementService = beanFactory.getBean(KeyManagementService.class);
                logger.info("CipherService und KeyManagementService erfolgreich über BeanFactory geladen.");
            } catch (Exception e) {
                logger.error("Fehler beim Abrufen der Sicherheits-Beans vom BeanFactory.", e);
                throw new IllegalStateException("Fehler beim Initialisieren der Sicherheitsdienste: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Konvertiert den Klartext-String des Attributs in einen verschlüsselten, Base64-kodierten String für die Datenbank.
     * Der IV und der GCM-Tag sind bereits im Output des CipherService enthalten.
     *
     * @param attribute Der Klartext-String des Attributs.
     * @return Der verschlüsselte und Base64-kodierte String für die Datenbank oder null, wenn das Attribut null ist.
     */
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        ensureServicesInitialized();

        try {
            SecretKey encryptionKey = keyManagementService.getEncryptionKey();
            byte[] encryptedBytes = cipherService.encrypt(attribute.getBytes(StandardCharsets.UTF_8), encryptionKey);
            return Base64.getEncoder().encodeToString(encryptedBytes);

        } catch (Exception e) {
            logger.error("Fehler beim Verschlüsseln des Attributs.", e);
            throw new RuntimeException("Verschlüsselungsfehler: " + e.getMessage(), e);
        }
    }

    /**
     * Konvertiert den verschlüsselten, Base64-kodierten String aus der Datenbank in den Klartext-String des Attributs.
     * Versucht, verschiedene Formate zu erkennen (neues GCM, altes mit ':') und gibt im Zweifelsfall Klartext zurück.
     *
     * @param dbData Der String aus der Datenbank.
     * @return Der Klartext-String des Attributs oder null, wenn dbData null ist.
     * @throws RuntimeException bei schwerwiegenden, nicht behebbaren Sicherheitsfehlern.
     */
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        ensureServicesInitialized();
        SecretKey encryptionKey = keyManagementService.getEncryptionKey();
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(dbData);
            byte[] decryptedBytes = cipherService.decrypt(decodedBytes, encryptionKey);
            return new String(decryptedBytes, StandardCharsets.UTF_8);

        } catch (IllegalArgumentException e) {
            logger.debug("String ist keine gültige Base64-Kodierung für das neue Format. Versuche andere Optionen. Fehler: {}", e.getMessage());
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            logger.warn("Fehler bei der Entschlüsselung des neuen Formats (z.B. Tag mismatch): {}. Versuche altes Format oder behandle als Klartext.", e.getMessage());
        } catch (Exception e) {
            logger.error("Unerwarteter Fehler beim Versuch, Daten im neuen Format zu entschlüsseln: {}", e.getMessage(), e);
        }
        if (dbData.contains(":")) {
            logger.warn("Datenstring enthält ':' und konnte nicht als neues Format entschlüsselt werden. Versuche als Altdaten zu entschlüsseln.");
            try {
                String[] parts = dbData.split(":", 2);
                String base64EncryptedDataPart = parts[0];

                byte[] decodedBytes = Base64.getDecoder().decode(base64EncryptedDataPart);
                byte[] decryptedBytes = cipherService.decrypt(decodedBytes, encryptionKey);

                logger.warn("Altdaten erfolgreich entschlüsselt. Bitte erwäge eine Migration dieser Daten in das neue Format!");
                return new String(decryptedBytes, StandardCharsets.UTF_8);

            } catch (IllegalArgumentException e) {
                logger.warn("Altdatenstring mit ':' konnte nicht Base64-dekodiert werden: {}. Behandle als Klartext.", e.getMessage());
            } catch (BadPaddingException | IllegalBlockSizeException e) {
                logger.warn("Fehler bei der Entschlüsselung des alten Formats (z.B. Tag mismatch): {}. Behandle als Klartext.", e.getMessage());
            } catch (Exception e) {
                logger.error("Unerwarteter Fehler beim Versuch, Daten im alten Format zu entschlüsseln: {}", e.getMessage(), e);
            }
        }
        logger.warn("Datenstring konnte weder als neues noch altes verschlüsseltes Format entschlüsselt werden. Gebe den String als Klartext zurück. Prüfe, ob dies ein unbeabsichtigter Zustand ist.");
        return dbData;
    }
}