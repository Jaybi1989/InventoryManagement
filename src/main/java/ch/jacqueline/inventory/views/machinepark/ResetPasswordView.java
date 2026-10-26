package ch.jacqueline.inventory.views.machinepark;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import ch.jacqueline.inventory.services.ResetPasswordService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Route("/reset-password")
@AnonymousAllowed
public class ResetPasswordView extends VerticalLayout implements BeforeEnterObserver {

    private final PasswordField newPasswordField = new PasswordField("Neues Passwort");
    private final PasswordField confirmPasswordField = new PasswordField("Neues Passwort bestätigen");
    private final Button resetPasswordButton = new Button("Passwort setzen");
    private final Button backToLoginButton = new Button("Zurück zum Login");

    private Optional<String> currentTokenOpt = Optional.empty();

    private final ResetPasswordService resetPasswordService = new ResetPasswordService();

    /**
     * Konstruktor der ResetPasswordView.
     * Initialisiert die Ansicht mit den erforderlichen Komponenten und deren Layout.
     * Stellt sicher, dass die Ansicht für die Eingabe eines neuen Passworts bereit ist.
     * Die Schaltflächen sind zunächst deaktiviert und werden aktiviert, sobald ein gültiger Token empfangen wurde.
     * Die Logik zum Setzen des Passworts und zur Validierung der Eingaben ist ebenfalls enthalten.
     * Die Methode `beforeEnter` wird verwendet, um den Token aus den URL-Parametern zu extrahieren und die Schaltflächen entsprechend zu aktivieren.
     * Die Ansicht enthält auch eine Rückschaltfläche, um zum Login zurückzukehren.
     * Die Ansicht ist so gestaltet, dass sie eine klare Benutzerführung bietet und sicherstellt, dass der Benutzer ein neues Passwort setzen kann, bevor er sich erneut anmeldet.
     * Die Eingabefelder für das Passwort und die Bestätigung des Passworts sind vorhanden, um sicherzustellen, dass der Benutzer sein neues Passwort korrekt eingibt.
     * Die Validierung der Passworteingaben stellt sicher, dass das neue Passwort nicht leer ist, die Bestätigung übereinstimmt und eine Mindestlänge von 6 Zeichen hat.
     */
    public ResetPasswordView() {
        System.out.println(">>> ResetPasswordView: Konstruktor betreten.");

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        addClassName("reset-password-view");

        H3 mainTitle = new H3("Neues Passwort setzen");
        Paragraph hintParagraph = new Paragraph("Bitte beachte, dass du nach dem Setzen des Passworts automatisch zum Login geleitet wirst.");

        hintParagraph.getStyle().set("color", "var(--lumo-success-color2");
        hintParagraph.getStyle().set("text-align", "center");
        hintParagraph.getStyle().set("margin-top", "0.5em");
        hintParagraph.getStyle().set("margin-bottom", "1em");

        VerticalLayout headerGroup = new VerticalLayout(mainTitle, hintParagraph);
        headerGroup.setSpacing(false);
        headerGroup.setPadding(false);
        headerGroup.setAlignItems(Alignment.CENTER);

        add(headerGroup, newPasswordField, confirmPasswordField, resetPasswordButton, backToLoginButton);

        newPasswordField.setWidth("300px");
        confirmPasswordField.setWidth("300px");

        resetPasswordButton.setEnabled(false);
        resetPasswordButton.addClickListener(event -> {
            String newPw = newPasswordField.getValue();
            String confirmPw = confirmPasswordField.getValue();

            if (newPw.isEmpty() || confirmPw.isEmpty()) {
                Notification.show("Bitte geben Sie Ihr neues Passwort ein und bestätigen Sie es.", 3000, Notification.Position.MIDDLE);
                return;
            }
            if (!newPw.equals(confirmPw)) {
                Notification.show("Passwörter stimmen nicht überein.", 3000, Notification.Position.MIDDLE);
                return;
            }
            if (newPw.length() < 6) {
                Notification.show("Das Passwort muss mindestens 6 Zeichen lang sein.", 3000, Notification.Position.MIDDLE);
                return;
            }
            currentTokenOpt.ifPresent(token -> resetPasswordService.resetPasswordBackend(token, newPw));
        });

        backToLoginButton.addClickListener(event -> UI.getCurrent().navigate("/login"));
    }

    /**
     * Diese Methode wird aufgerufen, bevor die Ansicht betreten wird.
     * Sie extrahiert den Token aus den URL-Parametern und lädt das CSRF-Token vom Backend.
     * Wenn der Token gültig ist, werden die Schaltflächen aktiviert, andernfalls wird eine Fehlermeldung angezeigt und zur Login-Seite weitergeleitet.
     *
     * @param event das Ereignis, das ausgelöst wird, bevor die Ansicht betreten wird
     */
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        System.out.println(">>> ResetPasswordView: beforeEnter Methode betreten.");

        QueryParameters queryParameters = event.getLocation().getQueryParameters();
        Map<String, List<String>> parametersMap = queryParameters.getParameters();

        if (parametersMap.containsKey("token") && !parametersMap.get("token").isEmpty()) {
            currentTokenOpt = Optional.of(parametersMap.get("token").get(0));

            resetPasswordService.fetchCsrfTokenAndThen(this::updateButtonStates);
        } else {
            Notification.show("Ungültiger oder fehlender Passwort-Reset-Token.", 3000, Notification.Position.MIDDLE);
            event.forwardTo(LoginView.class);
        }
    }

    /**
     * Aktualisiert den Status der Schaltflächen basierend auf dem aktuellen Token und dem CSRF-Token.
     * Wenn ein gültiger Token vorhanden ist und das CSRF-Token geladen wurde, wird die Schaltfläche zum Zurücksetzen des Passworts aktiviert.
     * Andernfalls bleibt die Schaltfläche deaktiviert.
     */
    private void updateButtonStates() {
        resetPasswordButton.setEnabled(currentTokenOpt.isPresent() && resetPasswordService.csrfTokenValue != null && !resetPasswordService.csrfTokenValue.isEmpty());
    }
}