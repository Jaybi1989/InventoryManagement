package ch.jacqueline.inventory.services;

import ch.jacqueline.inventory.entity.Brand;
import ch.jacqueline.inventory.repository.BrandRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service-Klasse für die Verwaltung der Brand-Entität.
 * <p>
 * Diese Klasse stellt Methoden zur Verfügung, um Marken zu speichern, abzurufen und zu verwalten.
 * Sie interagiert mit dem {@link BrandRepository}, um CRUD-Operationen auf den Marken durchzuführen.
 * <p>
 * Zu den grundlegenden Funktionen gehören das Abrufen aller Marken sowie das Speichern neuer oder
 * bestehender Marken.
 */
@Service
public class BrandService {

    @Autowired
    private BrandRepository brandRepository;

    public List<Brand> findAll() {
        return brandRepository.findAll();
    }

    public void save(Brand brand) {
        brandRepository.save(brand);
    }

    // Neue Methode zum Löschen einer Marke
    public void delete(Brand brand) {
        brandRepository.delete(brand);
    }

    public boolean existsByName(String name) {
        return brandRepository.existsByNameIgnoreCase(name);
    }
}
