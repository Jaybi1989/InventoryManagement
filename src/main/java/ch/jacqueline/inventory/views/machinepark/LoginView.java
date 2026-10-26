package ch.jacqueline.inventory.views.machinepark;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.component.UI; // Für UI.getCurrent().navigate

/**
 * Die Login-Ansicht für die Anwendung.
 * Diese Ansicht ist für die Benutzeranmeldung über Spring Security vorgesehen.
 * Sie ist öffentlich zugänglich (@AnonymousAllowed) und leitet bei erfolgreichem Login um.
 */
@Route("login")
@PageTitle("Login")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm login = new LoginForm();

    public LoginView(){
        addClassName("login-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        login.setAction("login");

        LoginI18n i18n = LoginI18n.createDefault();
        LoginI18n.Form i18nForm = i18n.getForm();
        i18nForm.setUsername("E-Mail");
        i18nForm.setForgotPassword("Passwort vergessen?");
        login.setI18n(i18n);

        login.setForgotPasswordButtonVisible(true);
        login.addForgotPasswordListener(e -> UI.getCurrent().navigate(ForgotPasswordView.class));

        H1 welcomeHeader = new H1();
        Span line1 = new Span("Willkommen beim Maschinenpark von ");
        Span line2 = new Span("Firma Beispielhaft GmbH");

        line1.getStyle().set("display", "block");
        line2.getStyle().set("display", "block");
        welcomeHeader.getStyle().set("text-align", "center");

        welcomeHeader.add(line1, line2);
        add(welcomeHeader, login);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        if(beforeEnterEvent.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")){
            login.setError(true);
        }
    }
}
