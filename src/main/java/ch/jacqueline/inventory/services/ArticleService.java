package ch.jacqueline.inventory.services;

import ch.jacqueline.inventory.entity.Article;
import ch.jacqueline.inventory.entity.Brand;
import ch.jacqueline.inventory.entity.Category;
import ch.jacqueline.inventory.entity.OperatingMode;
import com.vaadin.hilla.crud.CrudService; // Stelle sicher, dass dieser Import korrekt ist

import org.springframework.data.domain.Page; // Für die Page-basierte Liste
import org.springframework.data.domain.Pageable; // Für die Pageable-Parameter

import java.time.LocalDate;
import java.util.List;
import java.util.Optional; // Für Optional<Article> findByIdWithLocations

/**
 * Service-Klasse für die Verwaltung von Artikeln.
 * <p>
 * Diese Klasse stellt Methoden zur Verfügung, um Artikel zu listen, zu speichern und
 * einzelne Artikel zusammen mit deren zugehörigen Ausleihdaten (Locations) abzurufen.
 * <p>
 * Sie interagiert mit dem {@link ch.jacqueline.inventory.repository.ArticleRepository}, um CRUD-Operationen auf den Artikeln durchzuführen.
 */
public interface ArticleService extends CrudService<Article, Long> {

    /**
     * Sucht Artikel basierend auf verschiedenen Filterkriterien.
     *
     * @param articlenameFilter Filter für den Artikelnamen (teilweise Übereinstimmung, Groß-/Kleinschreibung ignoriert).
     * @param brandFilter Filter nach Marke.
     * @param categoryFilter Filter nach Kategorie.
     * @param operatingModeFilter Filter nach Betriebsart.
     * @param deletedStatusFilter Filter nach Löschstatus ("Alle", "Aktiv", "Gelöscht").
     * @return Eine Liste der gefilterten Artikel.
     */
    List<Article> findArticlesByFilters(
            String articleIdFilter,
            String articlenameFilter,
            Brand brandFilter,
            Category categoryFilter,
            OperatingMode operatingModeFilter,
            LocalDate purchaseDateFilter,
            Integer warrantyDateFilter,
            String deletedStatusFilter
    );

    List<Article> findByBrand(Brand brand);
    List<Article> findByCategory(Category category);
    List<Article> findByOperatingMode(OperatingMode operatingMode);
    Page<Article> list(Pageable pageable);
    Article save(Article article);
    Optional<Article> findByIdWithLocations(Long id);

    List<Article> findAll();
}
