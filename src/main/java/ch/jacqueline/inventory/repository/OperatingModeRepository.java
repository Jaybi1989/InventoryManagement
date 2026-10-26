package ch.jacqueline.inventory.repository;

import ch.jacqueline.inventory.entity.OperatingMode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface für die OperatingMode-Entität.
 * This interface extends JpaRepository, was die grundlegenden CRUD-Operationen bereitstellt.
 * Es ist mit der @Repository-Annotation annotiert, um es als Spring-Repository zu kennzeichnen.
 * @see JpaRepository
 */
@Repository
public interface OperatingModeRepository extends JpaRepository<OperatingMode, Integer> {
    public boolean existsByNameIgnoreCase(String name);
}
