package ch.jacqueline.inventory.views.machinepark;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.component.AttachEvent;
import ch.jacqueline.inventory.services.PasswordForgotService;

@Route("/forgot-password")
@AnonymousAllowed
public class ForgotPasswordView extends VerticalLayout {

    private final TextField emailField = new TextField("E-Mail-Adresse");
    private final Button sendResetButton = new Button("Zurücksetzen anfordern");

    private final PasswordForgotService passwordForgotService;

    public ForgotPasswordView() {
        this.passwordForgotService = new PasswordForgotService("http://localhost:8082"); // Backend-URL
//        this.passwordForgotService = new PasswordForgotService( "http://10.33.100.25:8082");


        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        addClassName("forgot-password-view");

        add(new H3("Passwort vergessen?"), emailField, sendResetButton);

        emailField.setPlaceholder("Ihre E-Mail-Adresse");
        emailField.setWidth("300px");

        sendResetButton.addClickListener(event -> {
            final String email = emailField.getValue();
            if (email.isEmpty()) {
                Notification.show("Bitte geben Sie Ihre E-Mail-Adresse ein.", 3000, Notification.Position.MIDDLE);
                return;
            }

            passwordForgotService.requestPasswordReset(email, UI.getCurrent(),
                    () -> {
                        Notification.show("Du erhälst in Kürze eine E-Mail mit einem Link, um dein neues Passwort zu setzen.", 3000, Notification.Position.MIDDLE);
                        UI.getCurrent().navigate("/login");
                    },
                    error -> Notification.show("Fehler: " + error, 5000, Notification.Position.MIDDLE)
            );
        });

    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        passwordForgotService.fetchCsrfTokenAndThen(UI.getCurrent(), this::updateButtonStates);
    }

    private void updateButtonStates() {
        boolean enabled = (passwordForgotService.csrfTokenValue != null && !passwordForgotService.csrfTokenValue.isEmpty());
        sendResetButton.setEnabled(enabled);
        if (!enabled) {
            Notification.show("Der 'Zurücksetzen anfordern'-Button ist deaktiviert, weil kein gültiges CSRF-Token geladen werden konnte.", 4000, Notification.Position.BOTTOM_END);
        }
    }
}
