package ch.jacqueline.inventory.security;

import java.security.SecureRandom;

public class CryptoRandom {
    static final SecureRandom secureRandom = new SecureRandom();

    private CryptoRandom() {

    }

     public static byte[] generateRandomBytes(int numBytes) {
        byte[] randomBytes = new byte[numBytes];
        secureRandom.nextBytes(randomBytes);
        return randomBytes;
     }

     public static byte[] generateIv(int ivLength) {
        if (ivLength <= 0) {
            throw new IllegalArgumentException("IV length must be greater than 0");
        }
        return generateRandomBytes(ivLength);
     }
}
