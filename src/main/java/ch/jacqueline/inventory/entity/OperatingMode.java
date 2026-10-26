package ch.jacqueline.inventory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity-Klasse für die Kategorie.
 * Diese Klasse repräsentiert eine Kategorie in der Datenbank.
 * Sie enthält die Attribute und deren Zuordnung zu den Datenbankspalten.
 * @see jakarta.persistence.Entity
 * @see jakarta.persistence.Table
 */
@Entity
@Table(name = "OperatingMode")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OperatingMode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OperatingModeID")
    private Integer operatingModeId;

    @Column(name = "Name", nullable = false, unique = true)
    private String name;
}
