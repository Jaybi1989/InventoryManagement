package ch.jacqueline.inventory.config;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

/**
 * Kennzeichnet den aktuellen authentifizierten Benutzer in einem Spring MVC Controller.
 * Diese Annotation wird verwendet, um den authentifizierten Benutzer automatisch zu injizieren,
 * indem sie mit dem {@link AuthenticationPrincipal} annotiert wird. Spring Security löst
 * den {@link Authentication} Principal des aktuell authentifizierten Benutzers und
 * stellt diesen dem markierten Parameter zur Verfügung.
 * Die Annotation sorgt dafür, dass der Parameter im Controller automatisch mit dem
 * aktuellen Benutzer gefüllt wird, ohne dass eine manuelle Authentifizierung erforderlich ist.
 * Die {@link Retention} Annotation stellt sicher, dass diese Annotation zur Laufzeit verfügbar ist,
 * was für die Bearbeitung durch Spring Security erforderlich ist.
 * </p>
 */
@AuthenticationPrincipal
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUser {
}