package ch.jacqueline.inventory.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

/**
 * Entity für Artikel.
 * Diese Klasse repräsentiert die Artikel in der Datenbank.
 * Sie enthält Attribute wie Artikelname, Kaufdatum, Garantiezeitraum und Löschstatus.
 * @see Brand
 * @see Category
 * @see OperatingMode
 */
@Entity
@Table(name = "Article")
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonFormat(pattern = "dd.MM.yyyy")
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ArticleID")
    private Long articleId;

    @ManyToOne
    @JoinColumn(name = "BrandID", nullable = false)
    @ToString.Exclude
    private Brand brand;

    @ManyToOne
    @JoinColumn(name = "CategoryID", nullable = false)
    @ToString.Exclude
    private Category category;

    @ManyToOne
    @JoinColumn(name = "OperatingModeID", nullable = false)
    @ToString.Exclude
    private OperatingMode operatingMode;

    @Column(name = "Articlename", nullable = false)
    private String articlename;

    @JsonFormat(pattern = "dd.MM.yyyy")
    @Column(name = "PurchaseDate")
    private LocalDate purchaseDate;

    @Column(name = "WarrantyDuration")
    private Integer warrantyDuration;

    @Column(name = "IsDeleted")
    private Boolean deleted;

    @Column(name = "DeletionDate")
    private LocalDate deletionDate;

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Location> locations;
}

