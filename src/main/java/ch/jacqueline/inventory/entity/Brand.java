package ch.jacqueline.inventory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity-Klasse für die Brand-Entität.
 * Diese Klasse repräsentiert die Brand-Tabelle in der Datenbank.
 * Sie enthält die Attribute, die den Spalten der Tabelle entsprechen.
 * @see jakarta.persistence.Entity
 * @see jakarta.persistence.Table
 */
@Entity
@Table(name = "Brand")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Brand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BrandID")
    private Integer brandId;

    @Column(name = "Name", nullable = false, unique = true)
    private String name;
}
