//package ch.jacqueline.inventory.repository;
//
//import ch.jacqueline.inventory.entity.Article;
//import ch.jacqueline.inventory.entity.Contact;
//import ch.jacqueline.inventory.entity.Location;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//
///**
// * Repository interface für die Location-Entität.
// * This interface extends JpaRepository, was die grundlegenden CRUD-Operationen bereitstellt.
// * Es ist mit der @Repository-Annotation annotiert, um es als Spring-Repository zu kennzeichnen.
// * @see JpaRepository
// */
//@Repository
//public interface LocationRepository extends JpaRepository<Location, Integer> {
//    boolean existsByContactAndLoanDateIsNotNull(Contact contact);
//
//    @Query("SELECT l FROM Location l WHERE l.contact = :contact AND l.loanDate IS NOT NULL AND " +
//            "NOT EXISTS (SELECT 1 FROM Location l2 WHERE l2.article = l.article AND l2.loanDate > l.loanDate)")
//
//    List<Location> findByArticle(Article article);
//    List<Location> findByContact(Contact contact);
//    List<Location> findByContactAndLoanDateIsNotNull(Contact contact);
//}
package ch.jacqueline.inventory.repository;

import ch.jacqueline.inventory.entity.Article;
import ch.jacqueline.inventory.entity.Contact;
import ch.jacqueline.inventory.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, Integer> {
    boolean existsByContactAndLoanDateIsNotNull(Contact contact);
    /**
     * Findet alle AKTIVEN Ausleihen für einen bestimmten Kontakt.
     * Eine Ausleihe ist aktiv, wenn:
     * 1. Sie ein LoanDate hat UND KEIN ReturnDate (d.h. sie wurde ausgeliehen, aber nicht zurückgegeben).
     * 2. Es der NEUESTE Eintrag für den jeweiligen Artikel ist (d.h. es gibt keinen späteren Location-Eintrag
     * für diesen Artikel, basierend auf dem LoanDate).
     */
    @Query("SELECT l FROM Location l WHERE l.contact = :contact " +
            "AND l.loanDate IS NOT NULL AND l.returnDate IS NULL AND " +
            "NOT EXISTS (SELECT 1 FROM Location l2 WHERE l2.article = l.article AND l2.loanDate > l.loanDate)")
    List<Location> findActiveLoansByContact(@Param("contact") Contact contact);

    List<Location> findByArticle(Article article);

    List<Location> findAllByContact(Contact contact);

    List<Location> findByContactAndReturnDateIsNotNull(Contact contact);
    List<Location> findAllByArticleAndContactIsNotNull(Article article);
    List<Location> findAllByArticle(Article article);
}