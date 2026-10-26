//package ch.jacqueline.inventory.security;
//
//import org.springframework.stereotype.Service;
//
//import javax.crypto.Mac;
//import javax.crypto.SecretKey;
//import java.security.InvalidKeyException;
//import java.security.NoSuchAlgorithmException;
//import java.util.Arrays;
//
//@Service
//public class MacService {
//
//    private static final String MAC_ALGORITHM = "HmacSHA256";
//
//    public byte[] calculateMac(byte[] data, SecretKey key) throws SecurityException {
//        try {
//            Mac mac = Mac.getInstance(MAC_ALGORITHM);
//            mac.init(key);
//            return mac.doFinal(data);
//        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
//            throw new SecurityException("Fehler beim Berechnen des MAC: " + e.getMessage(), e);
//        }
//    }
//
//    public boolean verifyMac(byte[] data, SecretKey key, byte[] receivedMac) throws SecurityException {
//       byte[] calculatedMac = calculateMac(data, key);
//       return Arrays.equals(calculatedMac, receivedMac);
//    }
//}
