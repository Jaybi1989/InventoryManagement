package ch.jacqueline.inventory.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.UI;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.Map;
import java.util.concurrent.CompletionException;
import java.net.ConnectException;
import java.util.function.Consumer;

public class PasswordForgotService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String backendBaseUrl;

    private String csrfHeaderName;
    public String csrfTokenValue;

    public PasswordForgotService(String backendBaseUrl) {
        this.backendBaseUrl = backendBaseUrl;
        this.httpClient = HttpClient.newBuilder()
                .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
                .build();
        this.objectMapper = new ObjectMapper();
    }


    /**
     * Ruft das CSRF-Token vom Backend ab und setzt es für weitere Requests.
     * Bei Erfolg wird ein Callback zur Aktualisierung der UI aufgerufen.
     *
     * @param ui Aktuelle UI-Instanz zur sicheren Ausführung von UI-Code
     * @param onComplete Wird nach Abschluss (egal ob erfolgreich oder fehlerhaft) aufgerufen
     */

    public void fetchCsrfTokenAndThen(UI ui, Runnable onComplete) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(backendBaseUrl + "/api/csrf-token"))
                .GET()
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> ui.access(() -> {
                    try {
                        if (response.statusCode() == 200) {
                            JsonNode rootNode = objectMapper.readTree(response.body());
                            if (rootNode.has("headerName") && rootNode.has("token")) {
                                this.csrfHeaderName = rootNode.get("headerName").asText();
                                this.csrfTokenValue = rootNode.get("token").asText();
                                Notification.show("CSRF-Token erfolgreich geladen.", 2000, Notification.Position.BOTTOM_START);
                            } else {
                                Notification.show("Fehler beim CSRF-Token: Antwort unvollständig.", 6000, Notification.Position.MIDDLE);
                                this.csrfTokenValue = null;
                            }
                        } else {
                            Notification.show("Fehler beim Abrufen des CSRF-Tokens vom Backend.", 5000, Notification.Position.MIDDLE);
                            this.csrfTokenValue = null;
                        }
                    } catch (Exception e) {
                        Notification.show("Fehler beim Verarbeiten des CSRF-Tokens.", 7000, Notification.Position.MIDDLE);
                        this.csrfTokenValue = null;
                    } finally {
                        onComplete.run();
                    }
                }))
                .exceptionally(exception -> {
                    Throwable cause = exception instanceof CompletionException ? exception.getCause() : exception;
                    ui.access(() -> {
                        if (cause instanceof ConnectException) {
                            Notification.show("Netzwerkfehler: Backend nicht erreichbar.", 10000, Notification.Position.MIDDLE);
                        } else {
                            Notification.show("Fehler beim Abrufen des CSRF-Tokens.", 8000, Notification.Position.MIDDLE);
                        }
                    });
                    return null;
                });
    }

    /**
     * Sendet eine Passwort-Reset-Anfrage an das Backend für die angegebene E-Mail-Adresse.
     * Erfordert ein gültiges CSRF-Token. Erfolgs- oder Fehlermeldungen werden über die
     * übergebenen Callback-Methoden behandelt.
     *
     * @param email E-Mail-Adresse des Benutzers
     * @param ui Aktuelle UI-Instanz für Benachrichtigungen
     * @param onSuccess Wird bei erfolgreicher Anfrage aufgerufen
     * @param onError Wird bei Fehlern mit Fehlermeldung aufgerufen
     */

    public void requestPasswordReset(String email, UI ui, Runnable onSuccess, Consumer<String> onError) {
        if (this.csrfHeaderName == null || this.csrfTokenValue == null) {
            ui.access(() -> Notification.show("CSRF-Token nicht verfügbar. Bitte versuchen Sie es erneut.", 5000, Notification.Position.MIDDLE));
            return;
        }

        try {
            String jsonRequest = objectMapper.writeValueAsString(Map.of("email", email));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(backendBaseUrl + "/api/password/request-reset"))
                    .header("Content-Type", "application/json")
                    .header(this.csrfHeaderName, this.csrfTokenValue)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> ui.access(() -> {
                        if (response.statusCode() == 200) {
                            onSuccess.run();
                        } else {
                            onError.accept("Fehler beim Anfordern des Passwort-Resets: " + response.body());
                        }
                    }))
                    .exceptionally(exception -> {
                        ui.access(() -> onError.accept("Fehler beim Anfordern des Passwort-Resets: " + exception.getMessage()));
                        return null;
                    });
        } catch (Exception e) {
            ui.access(() -> onError.accept("Interner Fehler beim Vorbereiten der Anfrage: " + e.getMessage()));
        }
    }
}
