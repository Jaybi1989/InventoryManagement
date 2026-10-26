package ch.jacqueline.inventory.config;

import ch.jacqueline.inventory.views.machinepark.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Konfiguration der Sicherheitsimplementierung für die Anwendung.
 * Diese Klasse erweitert {@link VaadinWebSecurity} und konfiguriert die Sicherheitsrichtlinien für die Anwendung.
 * Sie definiert, welche Routen öffentlich zugänglich sind, und welche Authentifizierungsmethoden verwendet werden.
 * Diese Version ist korrekt strukturiert, um alle Kompilierungsfehler zu vermeiden
 * und sich auf die Standardfunktionalität von VaadinWebSecurity zu verlassen,
 * die den UserDetailsService Bean (CustomUserRepositoryUserDetailsService) und den PasswordEncoder Bean
 * automatisch finden und für die Authentifizierung verwenden sollte, wenn sie als Spring Beans vorhanden sind.
 *
 * @see VaadinWebSecurity
 * @see HttpSecurity
 * @see PasswordEncoder
 * @see BCryptPasswordEncoder
 * @see LoginView
 * @see EnableWebSecurity
 * @see Configuration
 * @see Bean
 * @see Override
 * @see Exception
 */
@Configuration
@EnableWebSecurity
public class CustomSecurityImplementation extends VaadinWebSecurity {

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                new AntPathRequestMatcher("/VAADIN/**"),
                                new AntPathRequestMatcher("/images/**"),
                                new AntPathRequestMatcher("/css/**"),
                                new AntPathRequestMatcher("/js/**"),
                                new AntPathRequestMatcher("/static/**"),
                                new AntPathRequestMatcher("/META-INF/resources/icons/Logo.webp"),
                                new AntPathRequestMatcher("/Logo.webp"),
                                new AntPathRequestMatcher("/login"),
                                new AntPathRequestMatcher("/error"),
                                new AntPathRequestMatcher("/forgot-password"),
                                new AntPathRequestMatcher("/reset-password"),
                                new AntPathRequestMatcher("/"),
                                new AntPathRequestMatcher("/api/csrf-token"),
                                new AntPathRequestMatcher("/api/password/request-reset"),
                                new AntPathRequestMatcher("/api/password/reset")
                        ).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/api/sensor/**")).permitAll()
                );

        http.csrf(csrf -> csrf
                .ignoringRequestMatchers(new AntPathRequestMatcher("/api/sensor/**"))
        );

        http.logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll()
        );
        super.configure(http);

        setLoginView(http, LoginView.class);

    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:8082", "http:10.33.100.25:8082"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/sensor/**", configuration);
        return source;
    }
}
