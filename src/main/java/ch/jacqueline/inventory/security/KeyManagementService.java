package ch.jacqueline.inventory.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Service
public class KeyManagementService {

    private SecretKey encryptionKey;
    private SecretKey macKey;

    public KeyManagementService(
            @Value("${app.encryption.aes-key}") String aesKeyBase64,
            @Value("${app.encryption.hmac-key}") String hmacKeyBase64) {
        try {
            byte[] decodedAesKey = Base64.getDecoder().decode(aesKeyBase64);
            byte[] decodedHmacKey = Base64.getDecoder().decode(hmacKeyBase64);

            this.encryptionKey = new SecretKeySpec(decodedAesKey, 0, decodedAesKey.length, "AES");
            this.macKey = new SecretKeySpec(decodedHmacKey, 0, decodedHmacKey.length, "HmacSHA256");

        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Fehler beim Dekodieren der Schlüssel aus der Konfiguration. Stellen Sie sicher, dass sie gültige Base64-Strings sind.", e);
        } catch (Exception e) {
            throw new RuntimeException("Fehler beim Initialisieren der Schlüssel aus der Konfiguration.", e);
        }
    }

    public SecretKey generateAesKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256, CryptoRandom.secureRandom);
        return keyGen.generateKey();
    }

    public SecretKey generateHmacKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
        keyGen.init(256, CryptoRandom.secureRandom);
        return keyGen.generateKey();
    }

    public SecretKey getEncryptionKey() {
        return encryptionKey;
    }

    public SecretKey getMacKey() {
        return macKey;
    }
}




//public static void main(String[] args) throws NoSuchAlgorithmException {
//    KeyManagementService tempService = new KeyManagementService();
//    SecretKey aesKey = tempService.getEncryptionKey();
//    SecretKey hmacKey = tempService.getMacKey();
//
//    String aesKeyBase64 = Base64.getEncoder().encodeToString(aesKey.getEncoded());
//    String hmacKeyBase64 = Base64.getEncoder().encodeToString(hmacKey.getEncoded());
//
//    System.out.println("Generated AES Key (Base64): " + aesKeyBase64);
//    System.out.println("Generated HMAC Key (Base64): " + hmacKeyBase64);
//}