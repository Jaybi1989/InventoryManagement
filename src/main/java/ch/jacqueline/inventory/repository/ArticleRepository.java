package ch.jacqueline.inventory.repository;

import ch.jacqueline.inventory.entity.Article;
import ch.jacqueline.inventory.entity.Brand;
import ch.jacqueline.inventory.entity.Category;
import ch.jacqueline.inventory.entity.OperatingMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long>, JpaSpecificationExecutor<Article> {
    List<Article> findByBrand(Brand brand);
    List<Article> findByCategory(Category category);
    List<Article> findByOperatingMode(OperatingMode operatingMode);
    /**
     * JPQL: Selektiert alle Article Entities und fetched eager die Brand-Relation
     * via JOIN FETCH für performante Paginierung.
     * (Paginierung ist die Aufteilung großer Datenmengen in Seiten, um die Benutzerfreundlichkeit und die Performance,
     * von Anwendungen zu verbessern sowie Ressourcen zu schonen.)
     */
    @Query("SELECT a FROM Article a JOIN FETCH a.brand")
    Page<Article> findAll(Pageable pageable);

    /**
     * JPQL: Selektiert eine Article Entity per articleId und fetched eager die
     * Locations-Relation via LEFT JOIN FETCH.
     */
    @Query("SELECT a FROM Article a LEFT JOIN FETCH a.locations WHERE a.articleId = :id")
    Optional<Article> findByIdWithLocations(@Param("id") Long id);
}




