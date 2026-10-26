package ch.jacqueline.inventory.services;

import ch.jacqueline.inventory.entity.OperatingMode;
import ch.jacqueline.inventory.repository.OperatingModeRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service-Klasse für die Verwaltung von Betriebsarten.
 * Diese Klasse stellt Methoden zur Verfügung, um Betriebsarten zu speichern und abzurufen.
 * Sie interagiert mit dem {@link OperatingModeRepository}, um CRUD-Operationen auf den Betriebsarten
 * durchzuführen.
 * Zu den grundlegenden Funktionen gehören das Abrufen aller Betriebsarten sowie das Speichern neuer
 * oder bestehender Betriebsarten.
 */
@Service
public class OperatingModeService {

    @Autowired
    private OperatingModeRepository operatingModeRepository;

    /**
     * Gibt eine Liste aller Betriebsarten zurück.
     *
     * @return eine Liste von {@link OperatingMode} Objekten
     */
    public List<OperatingMode> findAll() {
        return operatingModeRepository.findAll();
    }

    /**
     * Speichert eine Betriebsart in der Datenbank.
     *
     * @param operatingMode die zu speichernde Betriebsart
     */
    public void save(OperatingMode operatingMode) {
        operatingModeRepository.save(operatingMode);
    }

    public boolean existsByName(String name) {
        return operatingModeRepository.existsByNameIgnoreCase(name);
    }

    public void delete(OperatingMode operatingMode) {
        operatingModeRepository.delete(operatingMode);
    }
}
