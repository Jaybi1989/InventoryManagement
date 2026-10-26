//package ch.jacqueline.inventory.controller;
//
//import ch.jacqueline.inventory.entity.Article;
//import ch.jacqueline.inventory.security.CipherService;
//import ch.jacqueline.inventory.security.EncryptedDataRequest;
//import ch.jacqueline.inventory.security.KeyManagementService;
//// import ch.jacqueline.inventory.security.MacService; // <-- Diese Zeile entfernen
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import javax.crypto.SecretKey;
//import javax.crypto.AEADBadTagException; // <-- Neuen Import hinzufügen
//import java.nio.charset.StandardCharsets;
//import java.util.Base64;
//
//@RestController
//@RequestMapping("/api/articles")
//public class ArticleController {
//
//    private final CipherService cipherService;
//    // private final MacService macService; // <-- Diese Zeile entfernen
//    private final KeyManagementService keyManagementService;
//    private final ObjectMapper objectMapper;
//
//    public ArticleController(CipherService cipherService,
//                             // MacService macService, // <-- Diesen Parameter entfernen
//                             KeyManagementService keyManagementService, ObjectMapper objectMapper) {
//        this.cipherService = cipherService;
//        // this.macService = macService; // <-- Diese Initialisierung entfernen
//        this.keyManagementService = keyManagementService;
//        this.objectMapper = objectMapper;
//    }
//
//    /**
//     * Empfängt verschlüsselte Artikeldaten, entschlüsselt und speichert sie.
//     * Nimmt ein EncryptedDataRequest an und gibt eine Erfolgsmeldung zurück.
//     * (Typischerweise ein POST für das Speichern von Daten)
//     */
//    @PostMapping("/add-secure-article")
//    public ResponseEntity<String> addSecureArticle(@RequestBody EncryptedDataRequest request) {
//        try {
//            SecretKey encryptionKey = keyManagementService.getEncryptionKey();
//            // SecretKey macKey = keyManagementService.getMacKey(); // <-- Diese Zeile entfernen
//
//            // Die übergebenen Daten enthalten jetzt IV + Ciphertext + GCM-Tag
//            byte[] encryptedDataWithIvAndTag = Base64.getDecoder().decode(request.getData());
//
//            // 1. Entschlüsselung (inklusive GCM-Tag-Verifizierung)
//            // Wenn der GCM-Tag nicht übereinstimmt (Daten manipuliert oder falscher Schlüssel),
//            // wirft decrypt eine AEADBadTagException
//            byte[] decryptedDataBytes = cipherService.decrypt(encryptedDataWithIvAndTag, encryptionKey);
//            String decryptedJson = new String(decryptedDataBytes, StandardCharsets.UTF_8);
//
//            // 2. Von JSON-String zu Artikel-Objekt umwandeln
//            Article article = objectMapper.readValue(decryptedJson, Article.class);
//
//            System.out.println("Erfolgreich entschlüsselter Artikel: " + article.getArticlename());
//            return ResponseEntity.ok("Artikel erfolgreich sicher hinzugefügt.");
//
//        } catch (AEADBadTagException e) { // <-- Spezifische Exception für GCM-Fehler
//            System.err.println("Sicherheitsfehler: GCM-Tag-Verifizierung für Artikel fehlgeschlagen! Datenintegrität kompromittiert oder Schlüssel geändert.");
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sicherheitsverletzung: Artikeldatenintegrität verletzt.");
//        } catch (Exception e) {
//            System.err.println("Fehler beim Empfangen/Verarbeiten sicherer Artikeldaten: " + e.getMessage());
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Interner Serverfehler beim Artikel hinzufügen.");
//        }
//    }
//
//    /**
//     * Ruft einen Artikel ab und gibt ihn verschlüsselt zurück.
//     * (Typischerweise ein GET, aber hier mit @PostMapping für Body wie in deinem Beispiel)
//     */
//    @PostMapping("/get-secure-article") // Oder @GetMapping("/get-secure-article/{id}")
//    public ResponseEntity<EncryptedDataRequest> getSecureArticle(@RequestBody String articleId) {
//        try {
//            // Hier Artikel aus der Datenbank abrufen
//            // Für das Beispiel: Mock-Artikel. In einer echten Anwendung würden Sie hier
//            // den Artikel über einen Service aus dem Repository laden.
//            Article article = new Article(); // Dummy-Artikel für das Beispiel
//            article.setArticleId(Integer.parseInt(articleId)); // Setzen der ID
//            article.setArticlename("Testartikel Name " + articleId);
//            article.setArticleserialnumber("SN-XYZ-" + articleId);
//            // ... weitere Felder setzen
//
//            // 1. Artikel-Objekt in JSON-String umwandeln
//            String plainJson = objectMapper.writeValueAsString(article);
//            byte[] messageBytes = plainJson.getBytes(StandardCharsets.UTF_8);
//
//            SecretKey encryptionKey = keyManagementService.getEncryptionKey();
//            // SecretKey macKey = keyManagementService.getMacKey(); // <-- Diese Zeile entfernen
//
//            // 2. Verschlüsselung: CipherService liefert IV + Ciphertext + GCM-Tag
//            byte[] encryptedDataWithIvAndTag = cipherService.encrypt(messageBytes, encryptionKey);
//            String encryptedDataBase64 = Base64.getEncoder().encodeToString(encryptedDataWithIvAndTag);
//
//            // 3. MAC-Berechnung ist nicht mehr nötig, da GCM das integriert
//            // byte[] mac = macService.calculateMac(encryptedData, macKey); // <-- Diese Zeile entfernen
//            // String macBase64 = Base64.getEncoder().encodeToString(mac); // <-- Diese Zeile entfernen
//
//            // 4. EncryptedDataRequest zurückgeben (ohne separates MAC-Feld)
//            // Wenn EncryptedDataRequest immer noch ein 'mac'-Feld hat, setzen Sie es auf null.
//            EncryptedDataRequest response = new EncryptedDataRequest(encryptedDataBase64); // Wenn EncryptedDataRequest nur noch 'data' hat
//            // Oder: EncryptedDataRequest response = new EncryptedDataRequest(encryptedDataBase64, null); // Wenn EncryptedDataRequest das 'mac'-Feld noch hat
//
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            System.err.println("Fehler beim Vorbereiten/Abrufen sicherer Artikeldaten: " + e.getMessage());
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
//        }
//    }
//}