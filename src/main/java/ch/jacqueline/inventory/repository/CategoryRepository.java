package ch.jacqueline.inventory.repository;

import ch.jacqueline.inventory.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface für die Category-Entität.
 * This interface extends JpaRepository, was die grundlegenden CRUD-Operationen bereitstellt.
 * Es ist mit der @Repository-Annotation annotiert, um es als Spring-Repository zu kennzeichnen.
 * @see JpaRepository
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    public boolean existsByNameIgnoreCase(String name);
}
