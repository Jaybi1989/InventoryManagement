package ch.jacqueline.inventory.repository;

import ch.jacqueline.inventory.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import ch.jacqueline.inventory.entity.User;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
/**
 * Repository interface für die User-Entität.
 * This interface extends JpaRepository, was die grundlegenden CRUD-Operationen bereitstellt.
 * Es ist mit der @Repository-Annotation annotiert, um es als Spring-Repository zu kennzeichnen.
 * @see JpaRepository
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByContact(Contact contact);

    List<User> findAllByContact(Contact contact);

     Optional<User> findByContact_EmailAddress(String emailAddress);

}
