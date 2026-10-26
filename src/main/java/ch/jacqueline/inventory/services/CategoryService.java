package ch.jacqueline.inventory.services;

import ch.jacqueline.inventory.entity.Category;
import ch.jacqueline.inventory.repository.CategoryRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service-Klasse für die Verwaltung der Kategorie-Entität.
 * Diese Klasse stellt Methoden zur Verfügung, um Kategorien zu speichern, abzurufen und zu verwalten.
 * Sie interagiert mit dem {@link CategoryRepository}, um CRUD-Operationen auf den Kategorien durchzuführen.
 * Zu den grundlegenden Funktionen gehören das Abrufen aller Kategorien sowie das Speichern neuer oder
 * bestehender Kategorien.
 */
@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public void save(Category category) {
        categoryRepository.save(category);
    }

    public boolean existsByName(String name) {
        return categoryRepository.existsByNameIgnoreCase(name);
    }

    public void delete(Category category) {
        categoryRepository.delete(category);
    }
}
