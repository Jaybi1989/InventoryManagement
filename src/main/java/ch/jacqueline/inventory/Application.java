package ch.jacqueline.inventory;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Locale;
import java.util.TimeZone;

/**
 * The entry point of the Spring Boot application.
 * and some desktop browsers.
 *
 */
@SpringBootApplication
@Theme(value = "machinepark")
@Push
public class Application implements AppShellConfigurator {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Zurich"));
        Locale.setDefault(new Locale.Builder().setLanguage("de").setRegion("CH").build());
        SpringApplication.run(Application.class, args);
    }
}
