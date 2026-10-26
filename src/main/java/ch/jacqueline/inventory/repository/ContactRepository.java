package ch.jacqueline.inventory.repository;

import ch.jacqueline.inventory.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface für die Contact-Entität.
 * This interface extends JpaRepository, was die grundlegenden CRUD-Operationen bereitstellt.
 * Es ist mit der @Repository-Annotation annotiert, um es als Spring-Repository zu kennzeichnen.
 * @see JpaRepository
 */
@Repository
public interface ContactRepository extends JpaRepository<Contact, Integer> {
    Optional<Contact> findByEmailAddress(String emailAddress);
    Optional<Contact> findById(Integer id);
}