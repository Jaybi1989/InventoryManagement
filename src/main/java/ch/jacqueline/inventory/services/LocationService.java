package ch.jacqueline.inventory.services;

import ch.jacqueline.inventory.entity.Article;
import ch.jacqueline.inventory.entity.Contact;
import ch.jacqueline.inventory.entity.Location;
import ch.jacqueline.inventory.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Service-Klasse für die Verwaltung von Standorten.
 * Diese Klasse stellt Methoden zur Verfügung, um Standorte zu speichern, zu überprüfen,
 * ob ein Kontakt aktive Ausleihen hat, und Standorte anhand eines Artikels zu finden.
 * Sie interagiert mit dem {@link LocationRepository}, um CRUD-Operationen auf den Standorten
 * durchzuführen.
 * Zu den grundlegenden Funktionen gehören das Speichern von Standorten, das Überprüfen von
 * aktiven Ausleihen und das Finden von Standorten basierend auf einem Artikel.
 */
@Service
public class LocationService {

    @Autowired
    private LocationRepository locationRepository;

    /**
     * Speichert einen Standort in der Datenbank.
     *
     * @param location der zu speichernde Standort
     * @return der gespeicherte Standort
     */
    public Location save(Location location) {
        return locationRepository.save(location);
    }

    /**
     * Überprüft, ob ein Kontakt aktive Ausleihen hat.
     * Eine Ausleihe gilt als aktiv, wenn sie ein LoanDate hat und kein ReturnDate.
     * Es wird die spezialisierte Repository-Methode verwendet.
     *
     * @param contact der zu überprüfende Kontakt
     * @return true, wenn der Kontakt aktive Ausleihen hat, andernfalls false
     */
    public boolean hasActiveLoans(Contact contact) {
        // Nutzt die bereits definierte Methode, die die komplexe Logik für "aktiv" enthält
        return !locationRepository.findActiveLoansByContact(contact).isEmpty();
    }

    /**
     * Findet alle Standorte, die mit einem bestimmten Artikel verknüpft sind.
     *
     * @param article der Artikel, dessen Standorte gefunden werden sollen
     * @return eine Liste von {@link Location} Objekten, die mit dem Artikel verknüpft sind
     */
    public List<Location> findByArticle(Article article) {
        return locationRepository.findByArticle(article);
    }

    /**
     * Findet alle Ausleihvorgänge (aktiv und historisch) für einen bestimmten Kontakt.
     *
     * @param contact der Kontakt, dessen Ausleihvorgänge gefunden werden sollen
     * @return eine Liste von {@link Location} Objekten für den Kontakt
     */
    public List<Location> findByContact(Contact contact) {
        return locationRepository.findAllByContact(contact);
    }

    /**
     * Findet alle AKTIVEN Ausleihvorgänge für einen bestimmten Kontakt.
     * Eine Ausleihe gilt als aktiv, wenn sie ein LoanDate hat und kein ReturnDate.
     * Diese Methode nutzt die `@Query`-Methode im Repository, die auch die "neueste" Logik enthält.
     *
     * @param contact der Kontakt, dessen aktive Ausleihvorgänge gefunden werden sollen
     * @return eine Liste von {@link Location} Objekten, die aktive Ausleihvorgänge darstellen
     */
    public List<Location> findActiveLoansByContact(Contact contact) {
        return locationRepository.findActiveLoansByContact(contact);
    }

    /**
     * Findet alle HISTORISCHEN/ABGESCHLOSSENEN Ausleihvorgänge für einen bestimmten Kontakt.
     * Eine Ausleihe gilt als historisch/abgeschlossen, wenn sie ein LoanDate und ein ReturnDate hat.
     *
     * @param contact der Kontakt, dessen historische Ausleihvorgänge gefunden werden sollen
     * @return eine Liste von {@link Location} Objekten, die historische Ausleihvorgänge darstellen
     */
    public List<Location> findHistoricalLoansByContact(Contact contact) {
        return locationRepository.findByContactAndReturnDateIsNotNull(contact);
    }
}