package ch.jacqueline.inventory.filters;

import ch.jacqueline.inventory.entity.Article;
import ch.jacqueline.inventory.entity.Brand;
import ch.jacqueline.inventory.entity.Category;
import ch.jacqueline.inventory.entity.OperatingMode;
import ch.jacqueline.inventory.services.ArticleService;
import com.vaadin.flow.component.grid.Grid;

import java.time.LocalDate;
import java.util.List;

public class ArticleFilter {
    private final ArticleService articleService;
    private final Grid<Article> grid;

    private String articleIdFilter;
    private String articlenameFilter;
    private Brand brandFilter;
    private Category categoryFilter;
    private OperatingMode operatingModeFilter;
    private LocalDate purchaseDateFilter;
    private Integer warrantyDurationFilter;
    private String deletedStatusFilter;


    public ArticleFilter(ArticleService articleService, Grid<Article> grid) {
        this.articleService = articleService;
        this.grid = grid;
        this.deletedStatusFilter = "Alle";
        applyFilter();
    }
    public void setArticleIdFilter(String articleIdFilter) {
        this.articleIdFilter = articleIdFilter;
        applyFilter();
    }

    public void setArticlenameFilter(String articlenameFilter) {
        this.articlenameFilter = articlenameFilter;
        applyFilter();
    }

    public void setBrandFilter(Brand brandFilter) {
        this.brandFilter = brandFilter;
        applyFilter();
    }

    public void setCategoryFilter(Category categoryFilter) {
        this.categoryFilter = categoryFilter;
        applyFilter();
    }

    public void setOperatingModeFilter(OperatingMode operatingModeFilter) {
        this.operatingModeFilter = operatingModeFilter;
        applyFilter();
    }
    public void setPurchaseDateFilter(LocalDate purchaseDateFilter) {
        this.purchaseDateFilter = purchaseDateFilter;
        applyFilter();
    }
    public void setWarrantyDurationFilter(Integer warrantyDateFilter) {
        this.warrantyDurationFilter = warrantyDateFilter;
        applyFilter();
    }
    public void setDeletedStatusFilter(String deletedStatusFilter) {
        this.deletedStatusFilter = deletedStatusFilter;
        applyFilter();
    }

    public void applyFilter() {
        List<Article> filteredArticles = articleService.findArticlesByFilters(
                articleIdFilter,
                articlenameFilter,
                brandFilter,
                categoryFilter,
                operatingModeFilter,
                purchaseDateFilter,
                warrantyDurationFilter,
                deletedStatusFilter);
        grid.setItems(filteredArticles);
    }

    public void clearFilter() {
        articleIdFilter = null;
        articlenameFilter = null;
        brandFilter = null;
        categoryFilter = null;
        operatingModeFilter = null;
        purchaseDateFilter = null;
        warrantyDurationFilter = null;
        deletedStatusFilter = "Alle";
        applyFilter();
    }
}