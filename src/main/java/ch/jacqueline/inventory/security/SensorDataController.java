//package ch.jacqueline.inventory.security;
//
//
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import javax.crypto.SecretKey;
//import java.nio.charset.StandardCharsets;
//import java.util.Base64;
//
//@RestController
//@RequestMapping("/api/sensor")
//public class SensorDataController {
//    private final CipherService cipherService;
//    private final MacService macService;
//    private final KeyManagementService keyManagementService;
//
//    public SensorDataController(CipherService cipherService, MacService macService, KeyManagementService keyManagementService) {
//        this.cipherService = cipherService;
//        this.macService = macService;
//        this.keyManagementService = keyManagementService;
//    }
//
//    @PostMapping("/receive-data")
//    public ResponseEntity<String> receiveSensorData(@RequestBody EncryptedDataRequest request) {
//        try {
//            SecretKey encryptionKey = keyManagementService.getEncryptionKey();
//            SecretKey macKey = keyManagementService.getMacKey();
//            byte[] encryptedDataBytes = Base64.getDecoder().decode(request.getData());
//            if (request.getMac() != null && !request.getMac().isEmpty()) {
//                byte[] receivedMacBytes = Base64.getDecoder().decode(request.getMac());
//
//                if (!macService.verifyMac(encryptedDataBytes, macKey, receivedMacBytes)) {
//                    System.err.println("Sicherheitsfehler: MAC-Verifizierung fehlgeschlagen! Daten wurden manipuliert oder stammen von unbekannter Quelle.");
//                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sicherheitsverletzung: Datenintegrität oder -authentizität verletzt.");
//                }
//                System.out.println("MAC-Verifizierung erfolgreich für eingehende Daten.");
//            } else {
//                System.out.println("Hinweis: Eingehende Daten ohne separaten MAC empfangen. Vertrauen auf GCM-Tag oder Annahme, dass kein MAC benötigt wird.");
//            }
//
//            byte[] decryptedDataBytes = cipherService.decrypt(encryptedDataBytes, encryptionKey);
//            String decryptedMessage = new String(decryptedDataBytes, StandardCharsets.UTF_8);
//
//            System.out.println("Erfolgreich entschlüsselte Sensordaten: " + decryptedMessage);
//
//            return ResponseEntity.ok("Daten erfolgreich verarbeitet: " + decryptedMessage);
//
//        } catch (IllegalArgumentException e) {
//            System.err.println("Fehler beim Dekodieren der Daten (Base64-Formatfehler): " + e.getMessage());
//            return ResponseEntity.badRequest().body("Fehler beim Dekodieren der Daten: Ungültiges Format.");
//        } catch (SecurityException e) {
//            System.err.println("Sicherheitsfehler bei der Datenverarbeitung: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sicherheitsfehler: Entschlüsselung oder integrierte Authentifizierung fehlgeschlagen.");
//        } catch (Exception e) {
//            System.err.println("Unerwarteter Fehler beim Empfangen/Verarbeiten der Sensordaten: " + e.getClass().getSimpleName() + " - " + e.getMessage());
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Interner Serverfehler.");
//        }
//    }
//
//    @PostMapping("/prepare-data")
//    public ResponseEntity<EncryptedDataRequest> prepareSensorData(@RequestBody PlainTextData plainTextRequest) {
//        try {
//            SecretKey encryptionKey = keyManagementService.getEncryptionKey();
//            SecretKey macKey = keyManagementService.getMacKey();
//
//            byte[] messageBytes = plainTextRequest.getMessage().getBytes(StandardCharsets.UTF_8);
//
//            byte[] encryptedData = cipherService.encrypt(messageBytes, encryptionKey);
//            String encryptedDataBase64 = Base64.getEncoder().encodeToString(encryptedData);
//
//            byte[] mac = macService.calculateMac(encryptedData, macKey);
//            String macBase64 = Base64.getEncoder().encodeToString(mac);
//
//            // KORREKTUR HIER: Fülle das response-Objekt mit den berechneten Werten
//            EncryptedDataRequest response = new EncryptedDataRequest(encryptedDataBase64, macBase64);
//
//            return ResponseEntity.ok(response);
//
//        } catch (SecurityException e) {
//            System.err.println("Sicherheitsfehler beim Vorbereiten der Daten: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
//        } catch (Exception e) {
//            System.err.println("Unerwarteter Fehler beim Vorbereiten der Daten: " + e.getMessage());
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
//        }
//    }
//}