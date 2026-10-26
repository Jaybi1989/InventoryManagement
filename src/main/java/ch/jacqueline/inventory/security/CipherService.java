//package ch.jacqueline.inventory.security;
//
//
//import org.springframework.stereotype.Service;
//
//import javax.crypto.Cipher;
//import javax.crypto.NoSuchPaddingException;
//import javax.crypto.SecretKey;
//import javax.crypto.spec.GCMParameterSpec;
//import java.nio.ByteBuffer;
//import java.security.InvalidAlgorithmParameterException;
//import java.security.InvalidKeyException;
//import java.security.NoSuchAlgorithmException;
//
//@Service
//public class CipherService {
//
//    private static final String ALGORITHM = "AES/GCM/NoPadding";
//    private static final int GCM_IV_LENGTH_BYTES = 12;
//    private static final int GCM_TAG_LENGTH_BYTES = 128;
//
//    public byte[] encrypt(byte[] plaintext, SecretKey secretKey) throws SecurityException {
//        try {
//            javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(ALGORITHM);
//            byte[] iv = CryptoRandom.generateIv(GCM_IV_LENGTH_BYTES);
//            cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, secretKey, new javax.crypto.spec.GCMParameterSpec(GCM_TAG_LENGTH_BYTES, iv));
//            byte[] ciphertext = cipher.doFinal(plaintext);
//            byte[] encryptedData = new byte[iv.length + ciphertext.length];
//            System.arraycopy(iv, 0, encryptedData, 0, iv.length);
//            System.arraycopy(ciphertext, 0, encryptedData, iv.length, ciphertext.length);
//            return encryptedData;
//        } catch (Exception e) {
//            throw new SecurityException("Encryption failed", e);
//        }
//    }
//
//    public byte[] decrypt(byte[] encryptedData, SecretKey secretKey) throws SecurityException {
//        try {
//            if (encryptedData.length < GCM_IV_LENGTH_BYTES) {
//                throw new SecurityException("Verschlüsselter Text ist zu kurz, um ein IV zu enthalten");
//            }
//            ByteBuffer encryptedDataBuffer = ByteBuffer.wrap(encryptedData);
//            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
//            encryptedDataBuffer.get(iv);
//            byte[] ciphertext = new byte[encryptedDataBuffer.remaining()];
//            encryptedDataBuffer.get(ciphertext);
//
//            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BYTES, iv);
//            Cipher cipher = Cipher.getInstance(ALGORITHM);
//            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec);
//
//            return cipher.doFinal(ciphertext);
//        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
//                 | InvalidAlgorithmParameterException | IllegalArgumentException e) {
//            throw new SecurityException("Decryption failed", e);
//        } catch (javax.crypto.BadPaddingException | javax.crypto.IllegalBlockSizeException e) {
//            throw new SecurityException("Decryption failed: Invalid ciphertext or key", e);
//        }
//
//    }
//}




package ch.jacqueline.inventory.security;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.SecureRandom; // Für das IV

@Service
public class CipherService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12; // 96 bits
    private static final int GCM_TAG_LENGTH = 16 * 8; // 128 bits (in bits, for GCMParameterSpec)

    public byte[] encrypt(byte[] plaintext, SecretKey key) throws Exception {
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv); // Generieren Sie ein zufälliges IV für jede Verschlüsselung

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);

        // Die doFinal-Methode liefert Ciphertext + GCM-Tag
        byte[] ciphertextWithTag = cipher.doFinal(plaintext);

        // Kombinieren Sie IV und Ciphertext+Tag für die Speicherung
        byte[] combined = new byte[GCM_IV_LENGTH + ciphertextWithTag.length];
        System.arraycopy(iv, 0, combined, 0, GCM_IV_LENGTH);
        System.arraycopy(ciphertextWithTag, 0, combined, GCM_IV_LENGTH, ciphertextWithTag.length);

        return combined;
    }

    public byte[] decrypt(byte[] combinedData, SecretKey key) throws Exception {
        // Extrahieren Sie das IV
        byte[] iv = new byte[GCM_IV_LENGTH];
        System.arraycopy(combinedData, 0, iv, 0, GCM_IV_LENGTH);

        // Extrahieren Sie den Ciphertext + GCM-Tag
        byte[] ciphertextWithTag = new byte[combinedData.length - GCM_IV_LENGTH];
        System.arraycopy(combinedData, GCM_IV_LENGTH, ciphertextWithTag, 0, ciphertextWithTag.length);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);

        // doFinal überprüft den GCM-Tag automatisch.
        // Wenn der Tag nicht übereinstimmt, wird eine AEADBadTagException geworfen.
        return cipher.doFinal(ciphertextWithTag);
    }
}