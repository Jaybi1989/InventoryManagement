package ch.jacqueline.inventory.config;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

/**
 * Interface für Sicherheitskonfigurations definition.
 * Methoden der Konfiguration der Spring Security Filter Chain {@link SecurityFilterChain}
 * Bereitstellung der BCryptPasswortEncoders {@link BCryptPasswordEncoder}
 */
public interface CustomSecurityInterface {
    /**
     * Spring Security konfigurieren, Methode definiert welche Ressourcen gesichert werden, welche Authentifizierungsmethoden verwendet werden.
     * @param http Der {@link HttpSecurity} Builder, konfiguriert den verwendeten Web Security.
     * @param introspector Der {@link HandlerMappingIntrospector} zuständig für die MVC-Matcher-Anforderungen.
     * @return {@link SecurityFilterChain} konfiguration
     * @throws Exception Wenn ein Fehler während der Konfiguration auftritt.
     */
    SecurityFilterChain configureSecurityFilterChain(HttpSecurity http, HandlerMappingIntrospector introspector) throws Exception;

    /**
     * Erstellt und liefert Instanz des {@link BCryptPasswordEncoder} wird zum Hashen von Passwörtern verwendet.
     * @return Eine neue Instanz des {@link BCryptPasswordEncoder}
     */
    BCryptPasswordEncoder passwordEncoder();
}