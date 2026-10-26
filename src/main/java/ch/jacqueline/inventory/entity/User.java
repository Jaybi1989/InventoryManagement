package ch.jacqueline.inventory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Collections;

/**
 * Entity für Benutzer, implementiert {@link UserDetails} für Spring Security.
 * Entity-Klasse für die Benutzer.
 * Diese Klasse repräsentiert die Benutzer in der Datenbank und implementiert die UserDetails-Schnittstelle,
 * um die Authentifizierung und Autorisierung zu unterstützen.
 * @see UserDetails
 */
@Entity
@Table(name = "User")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User implements UserDetails {
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_EMPLOYEE = "ROLE_EMPLOYEE";
    public static final String ADMIN = "admin";
    public static final String EMPLOYEE = "employee";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserID")
    private Long UserID;

    private String username;

    @ManyToOne
    @JoinColumn(name = "ContactID", nullable = false)
    @ToString.Exclude
    private Contact contact;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;

    @PrePersist
    @PreUpdate
    public void hashPassword() {
        if (password != null && !password.startsWith("$2a$")) {
            this.password = new BCryptPasswordEncoder().encode(password);
        }
    }

    /**
     * Setzt die Rolle eines Benutzers.
     */
    public void setRole(String role) {
        if (role != null) {
            if (role.equalsIgnoreCase("admin")) {
                this.role = "ROLE_ADMIN";
            } else {
                this.role = "ROLE_EMPLOYEE";
            }
        }
    }

    /**
     * Gibt die Berechtigungen des Benutzers zurück.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(this.role));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

