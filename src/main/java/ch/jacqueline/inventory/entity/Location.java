package ch.jacqueline.inventory.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity für die Location.
 * Diese Klasse repräsentiert die Standorte in der Datenbank.
 * Sie enthält Attribute wie Standort-ID, Artikel-ID, Kontakt-ID und Ausleihdatum.
 * @see Article
 * @see Contact
 */
@Entity
@Table(name = "Location")
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonFormat(pattern = "dd.MM.yyyy HH:mm")
public class Location {
    private LocalDate returnDate;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LocationID")
    private Integer locationId;

    @ManyToOne
    @JoinColumn(name = "ArticleID", nullable = false)
    private Article article;

    @ManyToOne
    @JoinColumn(name = "ContactID", nullable = false)
    private Contact contact;

    @Column(name = "LoanDate")
    private LocalDateTime loanDate;
}
