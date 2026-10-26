package ch.jacqueline.inventory.services;

import ch.jacqueline.inventory.entity.Article;
import ch.jacqueline.inventory.entity.Brand;
import ch.jacqueline.inventory.entity.Category;
import ch.jacqueline.inventory.entity.Location;
import ch.jacqueline.inventory.entity.OperatingMode;
import ch.jacqueline.inventory.repository.ArticleRepository;
import ch.jacqueline.inventory.repository.LocationRepository;

import com.vaadin.hilla.crud.filter.Filter;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementierung des ArticleService, der CRUD-Operationen für Artikel bereitstellt.
 * Diese Klasse verwaltet Artikel und deren zugehörige Ausleih-Einträge.
 */
@Service
public class ArticleServiceImpl implements ArticleService {
    private static final Logger logger = LoggerFactory.getLogger(ArticleServiceImpl.class);

    private final ArticleRepository articleRepository;
    private final LocationRepository locationRepository;

    /**
     * Konstruktor für die ArticleServiceImpl-Klasse.
     * Initialisiert den Service mit den erforderlichen Repositories.
     *
     * @param articleRepository  das Repository für Artikel
     * @param locationRepository das Repository für Ausleih-Einträge
     */
    public ArticleServiceImpl(ArticleRepository articleRepository, LocationRepository locationRepository) {
        this.articleRepository = articleRepository;
        this.locationRepository = locationRepository;
    }

    /**
     * Findet alle Artikel, die zu einer bestimmten Marke gehören.
     *
     * @param brand die Marke, nach der gefiltert werden soll
     * @return eine Liste von Artikeln, die der angegebenen Marke zugeordnet sind
     */
    @Override
    public List<Article> findByBrand(Brand brand) {
        return articleRepository.findByBrand(brand);
    }

    /**
     * Findet alle Artikel, die zu einer bestimmten Kategorie gehören.
     *
     * @param category die Kategorie, nach der gefiltert werden soll
     * @return eine Liste von Artikeln, die der angegebenen Kategorie zugeordnet sind
     */
    @Override
    public List<Article> findByCategory(Category category) {
        return articleRepository.findByCategory(category);
    }

    /**
     * Findet alle Artikel, die einem bestimmten Betriebsmodus zugeordnet sind.
     *
     * @param operatingMode der Betriebsmodus, nach dem gefiltert werden soll
     * @return eine Liste von Artikeln, die dem angegebenen Betriebsmodus zugeordnet sind
     */
    @Override
    public List<Article> findByOperatingMode(OperatingMode operatingMode) {
        return articleRepository.findByOperatingMode(operatingMode);
    }

    /**
     * Listet alle Artikel mit Paginierung.
     *
     * @param pageable die Paginierungsinformationen
     * @return eine Seite von Artikeln
     */
    @Override
    public Page<Article> list(Pageable pageable) {
        return articleRepository.findAll(pageable);
    }

    /**
     * Speichert einen Artikel in der Datenbank.
     *
     * @param article der zu speichernde Artikel
     * @return der gespeicherte Artikel
     */
    @Override
    @Transactional
    public Article save(Article article) {
        logger.info("Speichere Artikel mit ID: {}", article.getArticleId());
        return articleRepository.save(article);
    }

    /**
     * Löscht einen Artikel basierend auf seiner ID.
     * Wenn der Artikel bereits als gelöscht markiert ist, wird er nur als gelöscht markiert.
     * Andernfalls wird er dauerhaft gelöscht, sofern keine aktiven Ausleihen vorhanden sind.
     *
     * @param id die ID des zu löschenden Artikels
     */
    @Override
    @Transactional
    public void delete(@NotNull Long id) {
        Optional<Article> managedArticleOptional = articleRepository.findById(id);
        if (managedArticleOptional.isEmpty()) {
            logger.warn("Artikel mit ID {} konnte nicht gelöscht werden, da er nicht gefunden wurde.", id);
            return;
        }
        Article managedArticle = managedArticleOptional.get();

        if (Boolean.TRUE.equals(managedArticle.getDeleted())) {
            managedArticle.setDeletionDate(LocalDate.now());
            articleRepository.save(managedArticle);
            logger.info("Artikel '{}' (ID: {}) als gelöscht markiert.", managedArticle.getArticlename(), managedArticle.getArticleId());
            return;
        }

        logger.info("Versuche Artikel '{}' (ID: {}) dauerhaft zu löschen (Hard Delete).", managedArticle.getArticlename(), managedArticle.getArticleId());

        List<Location> activeLoans = locationRepository.findAllByArticleAndContactIsNotNull(managedArticle);
        if (!activeLoans.isEmpty()) {
            logger.warn("Löschen verhindert: Artikel mit ID {} hat noch aktive Ausleihen.", managedArticle.getArticleId());
            throw new IllegalStateException("Der Artikel '" + managedArticle.getArticlename() + "' kann nicht gelöscht werden, da er noch aktive Ausleihen besitzt.");
        }

        List<Location> allAssociatedLocations = locationRepository.findAllByArticle(managedArticle);
        if (!allAssociatedLocations.isEmpty()) {
            logger.info("Lösche {} zugeordnete Ausleih-Einträge für Artikel mit ID {}.", allAssociatedLocations.size(), managedArticle.getArticleId());
            locationRepository.deleteAll(allAssociatedLocations);
        } else {
            logger.info("Keine zu löschenden Ausleih-Einträge für Artikel mit ID {} gefunden.", id);
        }

        try {
            articleRepository.delete(managedArticle);
            logger.info("Artikel mit ID {} erfolgreich dauerhaft gelöscht.", id);
        } catch (DataIntegrityViolationException e) {
            logger.error("Datenbank-Integritätsfehler beim Löschen des Artikels mit ID {}: {}", id, e.getMessage());
            throw new IllegalStateException("Kann Artikel nicht löschen! Es existieren noch abhängige Daten (z.B. Ausleihen), die nicht entfernt werden konnten. Details: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unerwarteter Fehler beim Löschen des Artikels mit ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Ein unerwarteter Fehler ist beim Löschen des Artikels aufgetreten.", e);
        }
    }

    /**
     * Findet einen Artikel anhand seiner ID und lädt die zugehörigen Ausleih-Einträge.
     *
     * @param id die ID des zu suchenden Artikels
     * @return ein Optional, das den gefundenen Artikel enthält, oder leer, wenn kein Artikel gefunden wurde
     */
    @Override
    public Optional<Article> findByIdWithLocations(Long id) {
        return articleRepository.findById(id);
    }

    /** Findet Artikel basierend auf verschiedenen Filtern.
     *
     */
    @Override
    public List<Article> findArticlesByFilters(
            String articleIdFilter,
            String articlenameFilter,
            Brand brandFilter,
            Category categoryFilter,
            OperatingMode operatingModeFilter,
            LocalDate purchaseDateFilter,
            Integer warrantyDurationFilter,
            String deletedStatusFilter
    ) {
        return articleRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (articleIdFilter != null && !articleIdFilter.isEmpty()) {
                predicates.add(cb.equal(root.get("articleId"), Long.valueOf(articleIdFilter)));
            }
            if (articlenameFilter != null && !articlenameFilter.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("articlename")), "%" + articlenameFilter.toLowerCase() + "%"));
            }
            if (brandFilter != null) {
                predicates.add(cb.equal(root.get("brand"), brandFilter));
            }
            if (categoryFilter != null) {
                predicates.add(cb.equal(root.get("category"), categoryFilter));
            }
            if (operatingModeFilter != null) {
                predicates.add(cb.equal(root.get("operatingMode"), operatingModeFilter));
            }
            if (purchaseDateFilter != null) {
                predicates.add(cb.equal(root.get("purchaseDate"), purchaseDateFilter));
            }
            if (warrantyDurationFilter != null) {
                predicates.add(cb.equal(root.get("warrantyDuration"), warrantyDurationFilter));
            }

            if ("Aktiv".equals(deletedStatusFilter)) {
                predicates.add(cb.isFalse(root.get("deleted")));
            } else if ("Inaktiv".equals(deletedStatusFilter)) {
                predicates.add(cb.isTrue(root.get("deleted")));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        });
    }

    /**
     * Findet alle Artikel in der Datenbank.
     *
     * @return eine Liste aller Artikel
     */
    @Override
    public List<Article> findAll() {
        return articleRepository.findAll();
    }

    /**
     * Listet Artikel mit Paginierung und optionalen Filtern.
     *
     * @param pageable die Paginierungsinformationen
     * @param filter   optionale Filterkriterien
     * @return eine Liste von Artikeln, die den Paginierungs- und Filterkriterien entsprechen
     */
    @Override
    public @NonNull List<@NonNull Article> list(@NotNull Pageable pageable, @Nullable Filter filter) {
        return List.of();
    }
}