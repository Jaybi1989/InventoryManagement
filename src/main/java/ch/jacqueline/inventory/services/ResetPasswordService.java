package ch.jacqueline.inventory.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.server.VaadinSession;
import lombok.Getter;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

/**
 * Service-Klasse für das Zurücksetzen des Passworts.
 * Diese Klasse kommuniziert mit dem Backend, um das CSRF-Token abzurufen und das Passwort zurückzusetzen.
 * Sie verwendet HttpClient für die HTTP-Kommunikation und ObjectMapper für die JSON-Verarbeitung.
 * <p>
 * Die Klasse ist so konzipiert, dass sie asynchron arbeitet und UI-Updates über Vaadin Sessions ermöglicht.
 */
public class ResetPasswordService {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();
    //ONLY FOR TESTING, DO NOT USE IN PRODUCTION
//    private final String backendBaseUrl = "http://localhost:8082";
    private final String backendBaseUrl = "http://10.33.100.25:8082";

    @Getter
    private String csrfHeaderName;
    public String csrfTokenValue;

    private static final java.util.concurrent.ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    /**
     * Ruft das CSRF-Token synchron ab und aktualisiert den Button-Status.
     * Diese Methode wird nun aus der View aufgerufen und beinhaltet die Logik zur Backend-Kommunikation.
     * @param onComplete Aktion, die nach dem Abschluss des Token-Abrufs ausgeführt wird.
     */
    public void fetchCsrfTokenAndThen(Runnable onComplete) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(backendBaseUrl + "/api/csrf-token"))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            UI.getCurrent().access(() -> {
                if (response.statusCode() == 200) {
                    try {
                        JsonNode rootNode = objectMapper.readTree(response.body());
                        if (rootNode.has("headerName") && rootNode.has("token")) {
                            this.csrfHeaderName = rootNode.get("headerName").asText();
                            this.csrfTokenValue = rootNode.get("token").asText();
                            Notification.show("CSRF-Token geladen.", 2000, Notification.Position.BOTTOM_START);
                        } else {
                            Notification.show("Fehler beim CSRF-Token: Antwort unvollständig.", 6000, Notification.Position.MIDDLE);
                            System.err.println("FEHLER: CSRF-Token Antwort unvollständig. Body: " + response.body());
                        }
                    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                        Notification.show("Fehler beim Parsen des CSRF-Tokens.", 5000, Notification.Position.MIDDLE);
                        System.err.println("Fehler beim Parsen des CSRF-Tokens: " + e.getMessage());
                    }
                } else {
                    Notification.show("Fehler beim Abrufen des CSRF-Tokens vom Backend. Status: " + response.statusCode(), 5000, Notification.Position.MIDDLE);
                    System.err.println("Fehler beim Abrufen des CSRF-Tokens: Status " + response.statusCode() + ", Body: " + response.body());
                }
                onComplete.run();
            });
        } catch (Exception ex) {
            UI.getCurrent().access(() -> {
                Notification.show("Netzwerkfehler beim Abrufen des CSRF-Tokens: " + ex.getMessage(), 8000, Notification.Position.MIDDLE);
                System.err.println("Ausnahme beim Abrufen des CSRF-Tokens: " + ex.getClass().getSimpleName() + " - " + ex.getMessage());
                onComplete.run();
            });
        }
    }

    /**
     * Setzt das Passwort des Benutzers zurück, indem es eine Anfrage an das Backend sendet.
     * Diese Methode wird asynchron aufgerufen und aktualisiert die UI entsprechend.
     */
    public void resetPasswordBackend(String token, String newPassword) {
        if (this.csrfHeaderName == null || this.csrfTokenValue == null || this.csrfTokenValue.isEmpty()) {
            System.out.println("DEBUG: CSRF-Token ist null oder leer. Zeige Fehlermeldung an.");
            Notification.show("CSRF-Token ist nicht verfügbar. Bitte versuchen Sie es erneut.", 5000, Notification.Position.MIDDLE);
            return;
        }

        UI ui = UI.getCurrent();
        VaadinSession session = VaadinSession.getCurrent();

        if (ui == null || session == null) {
            System.err.println("WARNUNG: UI oder VaadinSession ist null vor async-Call. UI-Update unmöglich.");
            Notification.show("Fehler: Kann die Anwendung nicht erreichen. Bitte Seite neu laden.", 5000, Notification.Position.MIDDLE);
            return;
        }
        System.out.println("DEBUG: UI und VaadinSession sind gültig und aktiv. UIId: " + ui.getUIId() + ", SessionId: " + session.getSession().getId());

        try {
            String jsonRequest = objectMapper.writeValueAsString(Map.of("token", token, "newPassword", newPassword));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(backendBaseUrl + "/api/password/reset"))
                    .header("Content-Type", "application/json")
                    .header(this.csrfHeaderName, this.csrfTokenValue)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                    .build();

            System.out.println("DEBUG: Sende HTTP-Request asynchron...");
            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> ui.access(() -> {
                        System.out.println("DEBUG: Betrete ersten ui.access()-Block (Notification).");

                        if (response.statusCode() >= 200 && response.statusCode() < 300) {
                            // ZUERST die Notification anzeigen
                            Notification.show("Dein Passwort wurde erfolgreich gesetzt. Du kannst dich jetzt anmelden.", 3000, Notification.Position.MIDDLE); // Die Notification ist hier nur 3 Sekunden sichtbar
                            System.out.println("DEBUG: Notification sollte jetzt im Browser erscheinen.");

                            scheduler.schedule(() -> {
                                ui.access(() -> {
                                    System.out.println("DEBUG: Betrete verzögerten ui.access()-Block (Navigation).");
                                    ui.navigate("/login");
                                    System.out.println("DEBUG: Navigation zur Login-Seite ausgelöst.");
                                });
                            }, 1, TimeUnit.SECONDS);
                            System.out.println("DEBUG: Navigation für 1 Sekunde geplant.");

                        } else {
                            Notification.show("Fehler beim Zurücksetzen des Passworts: " + response.body(), 5000, Notification.Position.MIDDLE);
                            System.err.println("Backend-Fehler beim Reset (Status " + response.statusCode() + "): " + response.body());
                            ui.navigate("/login?error=resetFailed");
                        }
                        System.out.println("DEBUG: Erster ui.access()-Block beendet.");
                    }))
                    .exceptionally(ex -> {
                        ui.access(() -> {
                            System.err.println("DEBUG: exceptional-Callback erreicht.");
                            Throwable cause = ex;
                            if (ex instanceof CompletionException && ex.getCause() != null) {
                                cause = ex.getCause();
                            }
                            Notification.show("Netzwerkfehler: Passwort konnte nicht geändert werden: " + cause.getMessage(), 6000, Notification.Position.MIDDLE);
                            System.err.println("Ausnahme beim Senden/Verarbeiten des Passwort-Resets: " + cause.getClass().getSimpleName() + " - " + cause.getMessage());
                            ui.navigate("/login");
                        });
                        return null;
                    });
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            System.err.println("DEBUG: JsonProcessingException.");
            Notification.show("Interner Fehler: Daten konnten nicht vorbereitet werden.", 3000, Notification.Position.MIDDLE);
        } catch (Exception e) {
            System.err.println("DEBUG: Unerwartete synchrone Ausnahme.");
            Notification.show("Ein unerwarteter Fehler ist aufgetreten: " + e.getMessage(), 6000, Notification.Position.MIDDLE);
        }
    }
}