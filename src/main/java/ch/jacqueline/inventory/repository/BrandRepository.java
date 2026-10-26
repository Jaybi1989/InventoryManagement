package ch.jacqueline.inventory.repository;

import ch.jacqueline.inventory.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface für die Brand-Entität.
 * This interface extends JpaRepository, was die grundlegenden CRUD-Operationen bereitstellt.
 * Es ist mit der @Repository-Annotation annotiert, um es als Spring-Repository zu kennzeichnen.
 * @see JpaRepository
 */
@Repository
public interface BrandRepository extends JpaRepository<Brand, Integer> {
    boolean existsByNameIgnoreCase(String name);
}
