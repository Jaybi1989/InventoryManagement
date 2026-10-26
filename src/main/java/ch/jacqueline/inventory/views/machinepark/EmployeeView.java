package ch.jacqueline.inventory.views.machinepark;

import ch.jacqueline.inventory.entity.*;
import ch.jacqueline.inventory.entity.Article;
import ch.jacqueline.inventory.entity.Location;
import ch.jacqueline.inventory.export.ArticleCsvExporter;
import ch.jacqueline.inventory.repository.UserRepository;
import ch.jacqueline.inventory.services.*;
import ch.jacqueline.inventory.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Mitarbeiter")
@Route(value = "/employee", layout = MainLayout.class)
@RouteAlias("/")
@PermitAll
@Menu(order = 0, icon = LineAwesomeIconUrl.PENCIL_RULER_SOLID)
@Uses(Icon.class)
public class EmployeeView extends Composite<VerticalLayout> implements AfterNavigationObserver {
    private final ArticleService articleService;
    private final BrandService brandService;
    private final CategoryService categoryService;
    private final OperatingModeService operatingModeService;
    private final ContactService contactService;
    private final LocationService locationService;
    private final UserService userService;
    private final ArticleCsvExporter articleCsvExporter;
    private final UserRepository userRepository;

    private Grid<Article> articleGrid;

    private TextField articleIdFilterField;
    private TextField articlenameFilterField;
    private ComboBox<Brand> brandFilter;
    private ComboBox<Category> categoryFilter;
    private ComboBox<OperatingMode> operatingModeFilter;
    private DatePicker purchaseDateFilter;
    private IntegerField warrantyDurationFilter;
    private ComboBox<String> deletedFilter;
    private Button userManagementButton;

    @Autowired
    private ArticleLabelService articleLabelService;
    /**
     * Konstruktor für die {@code EmployeeView}-Klasse.
     * Funktionen:
     * - Initialisiert die Ansicht für Mitarbeiter.
     * - Bindet die Services zur Verwaltung von Artikeln, Marken, Kategorien, Betriebsarten, Kontakten, Standorten und Benutzern.
     * - Setzt das Grundlayout (Header, Buttons, Filter, Grid).
     * - Konfiguriert das Artikel-Grid.
     * - Lädt Artikel und füllt die Filter-ComboBoxen.
     *
     * @param articleService       Service zur Verwaltung von Artikeln
     * @param brandService         Service zur Verwaltung von Marken
     * @param categoryService      Service zur Verwaltung von Kategorien
     * @param operatingModeService Service zur Verwaltung von Betriebsarten
     * @param contactService       Service zur Verwaltung von Kontakten
     * @param locationService      Service zur Verwaltung von Standorten
     * @param userService          Service zur Verwaltung von Benutzern
     */
    public EmployeeView(ArticleService articleService, BrandService brandService,
                        CategoryService categoryService, OperatingModeService operatingModeService,
                        ContactService contactService, LocationService locationService,
                        UserService userService, ArticleCsvExporter articleCsvExporter, UserRepository userRepository,
                        ArticleLabelService articleLabelService) {
        this.articleService = articleService;
        this.brandService = brandService;
        this.categoryService = categoryService;
        this.operatingModeService = operatingModeService;
        this.contactService = contactService;
        this.locationService = locationService;
        this.userService = userService;
        this.articleCsvExporter = articleCsvExporter;
        this.userRepository = userRepository;
        this.articleLabelService = articleLabelService;

        setupUIComponents();
    }

    private void setupUIComponents() {
        Image logo = new Image("icons/Logo.webp", "Illustration");
        logo.setWidth("50px");
        logo.setHeight("50px");

        H1 h1 = new H1("Maschinen Park");
        h1.setWidth("max-content");

        HorizontalLayout headerLayout = new HorizontalLayout(logo, h1);
        headerLayout.setAlignItems(Alignment.CENTER);
        headerLayout.setSpacing(true);

        // --- Management Buttons ---
        Button addArticleButton = new Button("Artikel erfassen");
        addArticleButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button contactButton = new Button("Kontakt verwalten");
        contactButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button editAttributesButton = new Button("Attribute verwalten");
        editAttributesButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button exportButton = new Button("Exportieren");
        exportButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        userManagementButton = new Button("Benutzer verwalten");
        userManagementButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        userManagementButton.addClickListener(event -> openUserDialog(null));

//        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
//                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
//        userManagementButton.setVisible(isAdmin);

        HorizontalLayout managementButtonsLayout = new HorizontalLayout(
                addArticleButton,
                contactButton,
                userManagementButton,
                editAttributesButton,
                exportButton
        );

        exportButton.addClickListener(event -> {
            try {
                StreamResource resource = articleCsvExporter.exportAllArticlesToCsv();
                Anchor downloadLink = new Anchor(resource, "");
                downloadLink.getElement().setAttribute("download", true);
                downloadLink.getStyle().set("display", "none");

                UI.getCurrent().add(downloadLink);
                UI.getCurrent().getPage().executeJs("setTimeout(() => $0.click(), 50);", downloadLink.getElement());

                UI.getCurrent().addAfterNavigationListener(navigationEvent -> UI.getCurrent().remove(downloadLink));

                Notification.show("Export erfolgreich! Download wird gestartet...", 3000, Notification.Position.MIDDLE);

            } catch (Exception e) {
                Notification.show("Fehler beim Exportieren: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
                e.printStackTrace();
            }
        });

        managementButtonsLayout.addClassName("managementButtonsLayout");
        managementButtonsLayout.setWidthFull();
        managementButtonsLayout.setJustifyContentMode(JustifyContentMode.START);
        managementButtonsLayout.setAlignItems(Alignment.BASELINE);

        articleGrid = new Grid<>(Article.class, false);
        articleGrid.addClassName("attributeGridLayout");
        articleGrid.setWidthFull();
        articleGrid.addThemeVariants(GridVariant.LUMO_COMPACT);
        articleGrid.setColumnReorderingAllowed(true);
        articleGrid.setAllRowsVisible(true);

        Grid.Column<Article> articleIdColumn = articleGrid.addColumn(Article::getArticleId)
                .setHeader("ID")
                .setFlexGrow(0)
                .setSortable(true)
                .setResizable(true);
        Grid.Column<Article> articlenameColumn = articleGrid.addColumn(Article::getArticlename)
                .setHeader("Artikel")
                .setFlexGrow(1)
                .setSortable(true)
                .setResizable(true);
        Grid.Column<Article> brandColumn = articleGrid.addColumn(article ->
                        article.getBrand() != null ? article.getBrand().getName() : "")
                .setHeader("Marke")
                .setFlexGrow(1)
                .setSortable(true)
                .setResizable(true);
        Grid.Column<Article> categoryColumn = articleGrid.addColumn(article ->
                        article.getCategory() != null ? article.getCategory().getName() : "")
                .setHeader("Typ")
                .setFlexGrow(1)
                .setSortable(true)
                .setResizable(true);
        Grid.Column<Article> operatingModeColumn = articleGrid.addColumn(article ->
                        article.getOperatingMode() != null ? article.getOperatingMode().getName() : "")
                .setHeader("Modus")
                .setFlexGrow(1)
                .setSortable(true)
                .setResizable(true);
        Grid.Column<Article> purchaseDateColumn = articleGrid.addColumn(new LocalDateRenderer<>
                        (Article::getPurchaseDate, "dd.MM.yyyy"))
                .setHeader("Erwerb")
                .setFlexGrow(1)
                .setSortable(true)
                .setResizable(true);
        Grid.Column<Article> warrantyDurationColumn = articleGrid.addColumn(Article::getWarrantyDuration)
                .setHeader("Garantie/Mt.")
                .setFlexGrow(1)
                .setSortable(true)
                .setResizable(true);
        Grid.Column<Article> statusColumn = articleGrid.addColumn(new ComponentRenderer<>(article -> {
                    Icon icon;
                    if (article.getDeleted() != null && article.getDeleted()) {
                        icon = VaadinIcon.CLOSE_CIRCLE.create();
                        icon.setColor("red");
                        icon.getElement().getStyle().setWidth("20px");
                        icon.getElement().getStyle().setHeight("20px");
                        icon.getElement().setAttribute("title", "Inaktiv");
                    } else {
                        icon = VaadinIcon.CHECK_CIRCLE.create();
                        icon.setColor("green");
                        icon.getElement().getStyle().setWidth("20px");
                        icon.getElement().getStyle().setHeight("20px");
                        icon.getElement().setAttribute("title", "Aktiv");
                    }
                    return icon;
                }))
                .setHeader("Status")
                .setFlexGrow(0)
                .setSortable(true)
                .setResizable(true);


        Grid.Column<Article> printColumn = articleGrid.addColumn(new ComponentRenderer<>(item -> {
                    Button printButton = new Button(new Icon(VaadinIcon.PRINT));
                    printButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
                    printButton.getElement().setAttribute("aria-label", "Etikett drucken");

                    printButton.addClickListener(event -> {
                        printArticleLabel(item);
                    });

                    return printButton;
                }))
                .setHeader("Drucken")
                .setFlexGrow(0)
                .setSortable(false)
                .setResizable(true);

        articleGrid.setSelectionMode(Grid.SelectionMode.SINGLE);


        HeaderRow filterRow = articleGrid.appendHeaderRow();

        articleIdFilterField = new TextField();
        articleIdFilterField.setPlaceholder("Suche");
        articleIdFilterField.setWidthFull();
        articleIdFilterField.setValueChangeMode(ValueChangeMode.LAZY);
        articleIdFilterField.addValueChangeListener(event -> filterArticles());
        filterRow.getCell(articleIdColumn).setComponent(createFilterWrapper(articleIdFilterField));

        articlenameFilterField = new TextField();
        articlenameFilterField.setPlaceholder("Suche");
        articlenameFilterField.setWidthFull();
        articlenameFilterField.setValueChangeMode(ValueChangeMode.LAZY);
        articlenameFilterField.addValueChangeListener(event -> filterArticles());
        filterRow.getCell(articlenameColumn).setComponent(articlenameFilterField);

        brandFilter = new ComboBox<>();
        brandFilter.setPlaceholder("Filtern");
        brandFilter.setWidthFull();
        brandFilter.setClearButtonVisible(true);
        brandFilter.addValueChangeListener(event -> filterArticles());
        filterRow.getCell(brandColumn).setComponent(brandFilter);

        categoryFilter = new ComboBox<>();
        categoryFilter.setPlaceholder("Filtern");
        categoryFilter.setWidthFull();
        categoryFilter.setClearButtonVisible(true);
        categoryFilter.addValueChangeListener(event -> filterArticles());
        filterRow.getCell(categoryColumn).setComponent(categoryFilter);

        operatingModeFilter = new ComboBox<>();
        operatingModeFilter.setPlaceholder("Filtern");
        operatingModeFilter.setWidthFull();
        operatingModeFilter.setClearButtonVisible(true);
        operatingModeFilter.addValueChangeListener(event -> filterArticles());
        filterRow.getCell(operatingModeColumn).setComponent(operatingModeFilter);

        purchaseDateFilter = new DatePicker();
        purchaseDateFilter.setPlaceholder("Datum eingeben");
        purchaseDateFilter.setWidthFull();
        purchaseDateFilter.setClearButtonVisible(true);
        purchaseDateFilter.addValueChangeListener(event -> filterArticles());
        filterRow.getCell(purchaseDateColumn).setComponent(purchaseDateFilter);

        warrantyDurationFilter = new IntegerField();
        warrantyDurationFilter.setPlaceholder("Suche");
        warrantyDurationFilter.setWidthFull();
        warrantyDurationFilter.setClearButtonVisible(true);
        warrantyDurationFilter.addValueChangeListener(event -> filterArticles());
        filterRow.getCell(warrantyDurationColumn).setComponent(warrantyDurationFilter);


        deletedFilter = new ComboBox<>();
        deletedFilter.setPlaceholder("Filtern");
        deletedFilter.setWidthFull();
        List<String> deletedStatusOptions = Arrays.asList("Aktiv", "Inaktiv", "Alle");
        deletedFilter.setItems(deletedStatusOptions);
        deletedFilter.setClearButtonVisible(true);
        deletedFilter.setValue("Aktiv");
        deletedFilter.setItemLabelGenerator(item -> item);
        deletedFilter.addValueChangeListener(event -> filterArticles());
        filterRow.getCell(statusColumn).setComponent(deletedFilter);

        addArticleButton.addClickListener(event -> {
            Article newArticle = new Article();
            openArticleDialog(newArticle);
        });

        articleGrid.asSingleSelect().addValueChangeListener(event -> {
            Article selectedArticle = event.getValue();
            if (selectedArticle != null) {
                openArticleDialog(selectedArticle);
            }
        });

        contactButton.addClickListener(event -> openContactDialog());
        editAttributesButton.addClickListener(event -> openAttributeSelectionDialog());

        getContent().addClassName(Gap.SMALL);
        getContent().addClassName(Padding.SMALL);
        getContent().setWidthFull();
        getContent().setHeightFull();
        getContent().setJustifyContentMode(JustifyContentMode.START);
        getContent().setAlignItems(Alignment.START);

        getContent().add(headerLayout, articleGrid, managementButtonsLayout);
    }


    private Component createFilterWrapper(Component filterComponent) {
        VerticalLayout layout = new VerticalLayout(filterComponent);
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.setMargin(false);
        layout.setSizeFull();
        return layout;
    }

    private void loadFilterOptions() {
        brandFilter.setItems(brandService.findAll());
        brandFilter.setItemLabelGenerator(Brand::getName);
        categoryFilter.setItems(categoryService.findAll());
        categoryFilter.setItemLabelGenerator(Category::getName);
        operatingModeFilter.setItems(operatingModeService.findAll());
        operatingModeFilter.setItemLabelGenerator(OperatingMode::getName);

    }

    private void filterArticles() {
        String articlenameFilterValue = articlenameFilterField.getValue();
        String articleIdFilterValue = (articleIdFilterField != null) ? articleIdFilterField.getValue() : null;

        Brand selectedBrand = brandFilter.getValue();
        Category selectedCategory = categoryFilter.getValue();
        OperatingMode selectedOperatingMode = operatingModeFilter.getValue();
        LocalDate selectedPurchaseDate = purchaseDateFilter.getValue();
        Integer selectedWarrantyDuration = warrantyDurationFilter.getValue();
        String selectedDeletedStatus = deletedFilter.getValue();

        List<Article> filteredArticles = articleService.findArticlesByFilters(
                articleIdFilterValue,
                articlenameFilterValue,
                selectedBrand,
                selectedCategory,
                selectedOperatingMode,
                selectedPurchaseDate,
                selectedWarrantyDuration,
                selectedDeletedStatus
        );

        articleGrid.setItems(filteredArticles);
        articleGrid.getDataProvider().refreshAll();
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        loadFilterOptions();
        filterArticles();
        updateUserManagementButtonVisibility();
    }

    public void updateUserManagementButtonVisibility() {
        // **DIREKTER ZUGRIFF AUF DAS KLASSENFELD userManagementButton**
        if (userManagementButton != null) { // Sicherheitscheck, falls aus irgendeinem Grund null
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            userManagementButton.setVisible(isAdmin);
        } else {
            System.err.println("WARN: userManagementButton ist null in updateUserManagementButtonVisibility(). Kann Sichtbarkeit nicht setzen.");
        }
    }

    /**
     * Öffnet einen Dialog zur Anzeige und Bearbeitung der Details eines Artikels.
     * Der Dialog bietet Formularfelder zur Bearbeitung von Artikeldaten sowie eine Übersicht über bestehende und neue Ausleihvorgänge (Historie).
     * Über Buttons können neue Ausleihen hinzugefügt sowie die Änderungen gespeichert oder abgebrochen werden.
     *
     * @param article Das zu bearbeitende oder anzuzeigende {@link Article}-Objekt.
     */
    private void openArticleDialog(Article article) {
        Dialog articledialog = new Dialog();
        HorizontalLayout dialogLayout = new HorizontalLayout();
        dialogLayout.setSizeFull();
        dialogLayout.addClassName("topLayout");
        VerticalLayout leftLayout = new VerticalLayout();
        leftLayout.addClassName("leftLayout");
        leftLayout.setSpacing(false);

        H2 dialogTitleArticle = new H2("Artikeldetails");
        leftLayout.add(dialogTitleArticle);

        TextField articlenameField = new TextField("Artikel Name");
        articlenameField.addValueChangeListener(event -> {
            String value = event.getValue();
            if (value != null && !value.isEmpty()) {
                String formattedValue = value.substring(0, 1).toUpperCase() + value.substring(1);
                articlenameField.setValue(formattedValue);
            }
        });
        ComboBox<Brand> brandField = new ComboBox<>("Marke");
        ComboBox<Category> categoryField = new ComboBox<>("Typ");
        ComboBox<OperatingMode> operatingModeField = new ComboBox<>("Modus");
        DatePicker purchaseDateField = new DatePicker("Erwerb");
        IntegerField warrantyDurationField = new IntegerField("Garantiedauer in Mt.");
        ComboBox<Boolean> deletedField = new ComboBox<>("Status");

        brandField.setItems(brandService.findAll());
        brandField.setItemLabelGenerator(Brand::getName);
        categoryField.setItems(categoryService.findAll());
        categoryField.setItemLabelGenerator(Category::getName);
        operatingModeField.setItems(operatingModeService.findAll());
        operatingModeField.setItemLabelGenerator(OperatingMode::getName);
        deletedField.setItems(true, false);
        deletedField.setItemLabelGenerator(deleted -> deleted ? "Inaktiv" : "Aktiv");

        leftLayout.add(
                articlenameField,
                brandField,
                categoryField,
                operatingModeField,
                purchaseDateField,
                warrantyDurationField,
                deletedField
        );
        leftLayout.setAlignItems(Alignment.STRETCH);
        articlenameField.setWidthFull();
        brandField.setWidthFull();
        categoryField.setWidthFull();
        operatingModeField.setWidthFull();
        purchaseDateField.setWidthFull();
        warrantyDurationField.setWidthFull();
        deletedField.setWidthFull();

        VerticalLayout rightLayout = new VerticalLayout();
        rightLayout.addClassName("rightLayout");
        rightLayout.setSpacing(false);
        rightLayout.setAlignItems(Alignment.STRETCH);

        H2 historyTitle = new H2("Historie");
        rightLayout.add(historyTitle);
        Grid<Location> historyGrid = new Grid<>(Location.class, false);
        historyGrid.addColumn(location -> location.getContact().getFirstname() + " " + location.getContact().getLastname()).setHeader("Ausgeliehen an").setAutoWidth(true);
        historyGrid.addColumn(Location::getLoanDate)
                .setHeader("Ausleihdatum")
                .setAutoWidth(true)
                .setResizable(true)
                .setRenderer(new LocalDateTimeRenderer<>(
                        Location::getLoanDate,
                        "dd.MM.yyyy HH:mm"
                )).setAutoWidth(true).setResizable(true);
        historyGrid.setWidthFull();

        final List<Location> newLocations = new ArrayList<>();

        List<Location> initialGridItems = new ArrayList<>();

        if (article.getArticleId() == null){
            purchaseDateField.setValue(LocalDate.now());
            deletedField.setValue(false);

            // KORREKTUR HIER: findById erwartet Integer
            Optional<Contact> defaultContactOptional = contactService.findById(1); // ÄNDERUNG: 1L zu 1
            if (defaultContactOptional.isPresent()) {
                Contact defaultContact = defaultContactOptional.get();
                Location defaultLocation = new Location();
                defaultLocation.setContact(defaultContact);
                defaultLocation.setLoanDate(LocalDateTime.now());
                newLocations.add(defaultLocation);
                initialGridItems.addAll(newLocations);
            } else {
                Notification.show("Standardkontakt 'Firma Beispielhaft' nicht gefunden.", 3000, Notification.Position.MIDDLE);
            }
        } else {
            initialGridItems.addAll(locationService.findByArticle(article));
        }

        historyGrid.setItems(initialGridItems);

        Button loanButton = new Button("Ausleihen");
        Button cancelButton = new Button("Abbrechen");
        Button saveButton = new Button("Speichern");

        HorizontalLayout rightButtonsLayout = new HorizontalLayout(loanButton, cancelButton, saveButton);
        rightButtonsLayout.addClassName("managementButtonsLayout");
        rightButtonsLayout.setWidthFull();
        rightButtonsLayout.setJustifyContentMode(JustifyContentMode.END);

        loanButton.addClickListener(event -> {
            Dialog loanDialog = new Dialog();
            H2 dialogTitle = new H2("Ausleihe");
            loanDialog.add(dialogTitle);

            ComboBox<Contact> contactField = new ComboBox<>("Ausleihender");
            contactField.setItems(contactService.findAll());
            contactField.setItemLabelGenerator(contact -> contact.getFirstname() + " " + contact.getLastname());
            contactField.setRequired(true);

            // KORREKTUR HIER: findById erwartet Integer
            Optional<Contact> defaultContactOptional = contactService.findById(1); // ÄNDERUNG: 1L zu 1
            if (defaultContactOptional.isPresent()) {
                Contact defaultContact = defaultContactOptional.get();
                contactField.setValue(defaultContact);
            } else {
                Notification.show("Standardkontakt 'Firma Beispielhaft' (ID 1) nicht gefunden.", 3000, Notification.Position.MIDDLE);
            }

            DatePicker loanDateField = new DatePicker("Datum");
            loanDateField.setValue(LocalDate.now());

            VerticalLayout loanInputLayout = new VerticalLayout(
                    contactField,
                    loanDateField
            );
            loanInputLayout.addClassName("loanDialogLayout");
            loanInputLayout.setAlignItems(Alignment.STRETCH);
            loanInputLayout.setSpacing(false);

            Button loanCancelButton = new Button("Abbrechen");
            Button loanOkButton = new Button("OK");
            HorizontalLayout loanButtonsLayout = new HorizontalLayout(loanCancelButton, loanOkButton);
            loanButtonsLayout.addClassName("managementButtonsLayout");
            loanButtonsLayout.setJustifyContentMode(JustifyContentMode.END);

            VerticalLayout loanDialogLayout = new VerticalLayout(loanInputLayout, loanButtonsLayout);
            loanDialogLayout.setAlignItems(Alignment.STRETCH);
            loanDialogLayout.setSpacing(true);

            loanDialog.add(loanDialogLayout);

            loanOkButton.addClickListener(e -> {
                Contact selectedContact = contactField.getValue();
                LocalDate selectedDate = loanDateField.getValue();

                if (selectedContact != null && selectedDate != null) {
                    Contact currentDefaultContact = defaultContactOptional.orElse(null);

                    if (article.getArticleId() == null && newLocations.size() == 1 && currentDefaultContact != null) {
                        Location currentDefaultLocation = newLocations.get(0);

                        if (!selectedContact.equals(currentDefaultContact)) {
                            currentDefaultLocation.setContact(selectedContact);
                            currentDefaultLocation.setLoanDate(LocalDateTime.of(selectedDate, LocalDateTime.now().toLocalTime()));
                        } else {
                            currentDefaultLocation.setLoanDate(LocalDateTime.of(selectedDate, LocalDateTime.now().toLocalTime()));
                        }
                    } else {
                        Location newLocation = new Location();
                        newLocations.add(newLocation);
                        newLocation.setContact(selectedContact);
                        newLocation.setLoanDate(LocalDateTime.of(selectedDate, LocalDateTime.now().toLocalTime()));
                    }

                    List<Location> updatedDisplayedLocations = new ArrayList<>();
                    if (article.getArticleId() != null) {
                        updatedDisplayedLocations.addAll(locationService.findByArticle(article));
                    }
                    updatedDisplayedLocations.addAll(newLocations);
                    historyGrid.setItems(updatedDisplayedLocations);
                    loanDialog.close();
                } else {
                    Notification.show("Bitte wähle einen Ausleihenden und ein Datum!", 3000, Notification.Position.MIDDLE);
                }
            });
            loanCancelButton.addClickListener(e -> loanDialog.close());

            loanDialog.open();
        });


        Binder<Article> binder = new Binder<>(Article.class);
        binder.forField(articlenameField)
                .asRequired("Gebe einen Artikelnamen an.")
                .bind(Article::getArticlename, Article::setArticlename);
        binder.forField(brandField)
                .asRequired("Gebe eine Marke an.")
                .bind(Article::getBrand, Article::setBrand);
        binder.forField(categoryField)
                .asRequired("Gebe einen Typ an.")
                .bind(Article::getCategory, Article::setCategory);
        binder.forField(operatingModeField)
                .asRequired("Gebe einen Modus an.")
                .bind(Article::getOperatingMode, Article::setOperatingMode);
        binder.bind(purchaseDateField, Article::getPurchaseDate, Article::setPurchaseDate);
        binder.forField(warrantyDurationField)
                .asRequired("Gebe eine Garantiedauer in Monaten an.")
                .bind(Article::getWarrantyDuration, Article::setWarrantyDuration);
        binder.bind(deletedField, Article::getDeleted, Article::setDeleted);

        binder.setBean(article);



        if (article.getArticleId() == null) {
            purchaseDateField.setValue(LocalDate.now());
            deletedField.setValue(false);
        }

        cancelButton.addClickListener(event -> articledialog.close());


        saveButton.addClickListener(event -> {
            if (article.getArticleId() == null && newLocations.isEmpty()) {
                Notification.show("Bitte erfasse zuerst eine Ausleihe!", 5000, Notification.Position.MIDDLE);
                return;
            }

            try {
                binder.writeBean(article);

                Article savedArticle = articleService.save(article);

                newLocations.forEach(l -> {
                    l.setArticle(savedArticle);
                    locationService.save(l);
                });

                articledialog.close();
                getUI().ifPresent(ui -> ui.getPage().reload());

            } catch (ValidationException e) {
                Notification.show("Bitte fülle alle erforderlichen Felder korrekt aus!", 5000, Notification.Position.MIDDLE);
                System.err.println("Validierungsfehler beim Speichern des Artikels: " + e.getMessage());
            } catch (RuntimeException e) {
                Notification.show("Ein unerwarteter Fehler ist aufgetreten: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
                System.err.println("Unerwarteter Fehler beim Speichern des Artikels: " + e.getMessage());
                e.printStackTrace();
            }
        });

        rightLayout.add(historyTitle, historyGrid, rightButtonsLayout);

        HorizontalLayout mainLayout = new HorizontalLayout(leftLayout, rightLayout);
        mainLayout.addClassName("topLayout");
        mainLayout.setWidthFull();

        articledialog.add(mainLayout);
        articledialog.open();
    }


    /**
     * Öffnet einen Dialog zur Anzeige und Bearbeitung der Benutzerverwaltung.
     * Der Dialog bietet Formularfelder zur Bearbeitung von Benutzerdaten sowie eine Übersicht über alle Benutzer.
     * Über Buttons können neue Benutzer hinzugefügt, bestehende Benutzer gelöscht oder die Änderungen gespeichert werden.
     */

    private void openContactDialog() {
        Dialog contactDialog = new Dialog();
        contactDialog.addClassName("topLayout");

        HorizontalLayout dialogLayout = new HorizontalLayout();
        dialogLayout.setSizeFull();
        dialogLayout.setSpacing(false);
        dialogLayout.setPadding(false);
        dialogLayout.getStyle().set("width", "fit-content");
        dialogLayout.getStyle().set("max-width", "none");

        VerticalLayout leftLayout = new VerticalLayout();
        leftLayout.addClassName("leftLayout");
        leftLayout.setSpacing(false);

        H2 contactTitle = new H2("Kontakt erfassen / verwalten");
        leftLayout.add(contactTitle);

        ComboBox<Contact> contactSelection = new ComboBox<>("Kontakt auswählen");
        contactSelection.setItems(contactService.findAll());
        contactSelection.setItemLabelGenerator(contact -> {
            String firstName = contact.getFirstname() != null ? contact.getFirstname() : "";
            String lastName = contact.getLastname() != null ? contact.getLastname() : "";
            return firstName + " " + lastName;
        });
        contactSelection.setWidthFull();

        TextField lastnameField = new TextField("Nachname");
        lastnameField.setRequired(true);
        lastnameField.setRequiredIndicatorVisible(true);
        lastnameField.addValueChangeListener(event -> {
            String value = event.getValue();
            if (value != null && !value.isEmpty()) {
                String formattedValue = value.substring(0, 1).toUpperCase() + value.substring(1);
                lastnameField.setValue(formattedValue);
            }
        });
        TextField firstnameField = new TextField("Vorname");
        firstnameField.setRequired(true);
        firstnameField.setRequiredIndicatorVisible(true);
        firstnameField.addValueChangeListener(event -> {
            String stringValue = event.getValue();
            if (stringValue != null && !stringValue.isEmpty()) {
                String formattedValue = stringValue.substring(0, 1).toUpperCase() + stringValue.substring(1);
                firstnameField.setValue(formattedValue);
            }
        });
        IntegerField postcodeField = new IntegerField("PLZ");
        postcodeField.setMax(9999);
        postcodeField.setMin(1000);

        postcodeField.addValueChangeListener(event -> {
            Integer enteredValue = event.getValue();

            if (enteredValue != null) {
                String stringValue = String.valueOf(enteredValue);
                if (stringValue.length() != 4 || enteredValue < postcodeField.getMin() || enteredValue > postcodeField.getMax()) {
                    postcodeField.setErrorMessage("Die Postleitzahl muss eine 4-stellige Zahl zwischen 1000 und 9999 sein.");
                    postcodeField.setInvalid(true);
                } else {
                    postcodeField.setErrorMessage(null);
                    postcodeField.setInvalid(false);
                }
            } else {
                postcodeField.setErrorMessage(null);
                postcodeField.setInvalid(false);
            }
        });

        TextField cityField = new TextField("Ort");
        cityField.setRequired(true);
        cityField.setRequiredIndicatorVisible(true);
        cityField.addValueChangeListener(event -> {
            String value = event.getValue();
            if (value != null && !value.isEmpty()) {
                String formattedValue = value.substring(0, 1).toUpperCase() + value.substring(1);
                cityField.setValue(formattedValue);
            }
        });
        TextField streetField = new TextField("Strasse");
        streetField.setRequired(true);
        streetField.setRequiredIndicatorVisible(true);
        streetField.addValueChangeListener(event -> {
            String value = event.getValue();
            if (value != null && !value.isEmpty()) {
                String formattedValue = value.substring(0, 1).toUpperCase() + value.substring(1);
                streetField.setValue(formattedValue);
            }
        });
        TextField housenumberField = new TextField("Hausnummer");
        housenumberField.setRequired(true);
        housenumberField.setRequiredIndicatorVisible(true);
        housenumberField.setErrorMessage("Hausnummer ist erforderlich");

        TextField emailAddressField = new TextField("E-Mail Adresse");
        emailAddressField.setRequired(true);
        emailAddressField.setRequiredIndicatorVisible(true);
        emailAddressField.setErrorMessage("E-Mail Adresse ist erforderlich");


        Binder<Contact> contactBinder = new Binder<>(Contact.class);

        contactBinder.forField(lastnameField)
                .asRequired("Nachname ist erforderlich")
                .withValidator(
                        name -> name.matches("^[\\p{L}\\p{M}}'\\- ]+$"),
                        "Bitte nur Buchstaben, Leerzeichen, Bindestriche und Apostroph verwenden")
                .bind(Contact::getLastname, Contact::setLastname);

        contactBinder.forField(firstnameField)
                .asRequired("Vorname ist erforderlich")
                .withValidator(
                        name -> name.matches("^[\\p{L}\\p{M}}'\\- ]+$"),
                        "Bitte nur Buchstaben, Leerzeichen, Bindestriche und Apostroph verwenden")
                .bind(Contact::getFirstname, Contact::setFirstname);

        contactBinder.forField(postcodeField)
                .asRequired("Postleitzahl ist erforderlich")
                .withConverter(
                        integerValue -> integerValue != null ? String.valueOf(integerValue) : null,
                        stringValue -> {
                            if (stringValue == null || stringValue.isEmpty()) {
                                return null;
                            }
                            try {
                                Integer parsedInt = Integer.parseInt(stringValue);
                                if (parsedInt < postcodeField.getMin() || parsedInt > postcodeField.getMax()) {
                                    throw new NumberFormatException("Die Postleitzahl muss zwischen " + postcodeField.getMin() + " und " + postcodeField.getMax() + " liegen.");
                                }
                                return parsedInt;
                            } catch (NumberFormatException e) {
                                throw new RuntimeException("Ungültiges Zahlenformat für Postleitzahl.");
                            }
                        },
                        "Postleitzahl muss eine 4-stellige Zahl sein."
                )
                .withValidator(
                        postcode -> postcode != null && String.valueOf(postcode).matches("\\d{4}"),
                        "Die Postleitzahl muss genau 4 Zahlen enthalten."
                )
                .bind(Contact::getPostcode, Contact::setPostcode);

        contactBinder.forField(cityField)
                .asRequired("Ort ist erforderlich")
                .withValidator(
                        name -> name.matches("^[\\p{L}\\p{M}}'\\- ]+$"),
                        "Bitte nur Buchstaben, Leerzeichen, Bindestriche und Apostroph verwenden")
                .bind(Contact::getCity, Contact::setCity);

        contactBinder.forField(streetField)
                .asRequired("Strasse ist erforderlich")
                .withValidator(
                        name -> name.matches("^[\\p{L}\\p{M}}'\\- ]+$"),
                        "Bitte nur Buchstaben, Leerzeichen, Bindestriche und Apostroph verwenden")
                .bind(Contact::getStreetname, Contact::setStreetname);

        contactBinder.forField(housenumberField)
                .asRequired("Hausnummer ist erforderlich")
                .withValidator(
                        housenumber -> housenumber.matches("^[\\p{L}\\p{N}\\-/ ]+$"),
                        "Die Hausnummer darf nur Buchstaben, Zahlen, Bindestriche und Schrägstriche enthalten.")
                .bind(Contact::getHousenumber, Contact::setHousenumber);

        contactBinder.forField(emailAddressField)
                .asRequired("E-Mail Adresse ist erforderlich")
                .withValidator(email -> email.contains("@") && email.contains("."), "Ungültige E-Mail Adresse")
                .bind(Contact::getEmailAddress, Contact::setEmailAddress);


        lastnameField.setValueChangeMode(ValueChangeMode.ON_CHANGE);
        firstnameField.setValueChangeMode(ValueChangeMode.ON_CHANGE);
        postcodeField.setValueChangeMode(ValueChangeMode.ON_CHANGE);
        cityField.setValueChangeMode(ValueChangeMode.ON_CHANGE);
        streetField.setValueChangeMode(ValueChangeMode.ON_CHANGE);
        housenumberField.setValueChangeMode(ValueChangeMode.ON_CHANGE);
        emailAddressField.setValueChangeMode(ValueChangeMode.ON_CHANGE);

        leftLayout.add(
                contactSelection,
                lastnameField,
                firstnameField,
                postcodeField,
                cityField,
                streetField,
                housenumberField,
                emailAddressField
        );
        leftLayout.setAlignItems(Alignment.STRETCH);

        Button contactSaveButton = new Button("Speichern");
        Button deleteContactButton = new Button("Löschen");
        Button contactCancelButton = new Button("Abbrechen");
        HorizontalLayout buttonsLayout = new HorizontalLayout(contactSaveButton, deleteContactButton, contactCancelButton);
        buttonsLayout.addClassName("managementButtonsLayout");
        buttonsLayout.setJustifyContentMode(JustifyContentMode.END);
        leftLayout.add(buttonsLayout);

        VerticalLayout rightLayout = new VerticalLayout();
        rightLayout.addClassName("rightLayout");
        rightLayout.setSpacing(false);
        rightLayout.setAlignItems(Alignment.STRETCH);

        H2 allContactsTitle = new H2("Alle Kontakte");
        rightLayout.add(allContactsTitle);


        Grid<Contact> allContactsGrid = new Grid<>(Contact.class, false);
        allContactsGrid.addColumn(Contact::getFirstname)
                .setHeader("Vorname")
                .setAutoWidth(true);
        allContactsGrid.addColumn(Contact::getLastname)
                .setHeader("Nachname")
                .setAutoWidth(true);
        allContactsGrid.addColumn(Contact::getPostcode)
                .setHeader("PLZ")
                .setAutoWidth(true);
        allContactsGrid.addColumn(Contact::getCity)
                .setHeader("Ort")
                .setAutoWidth(true);
        allContactsGrid.addColumn(Contact::getEmailAddress)
                .setHeader("E-Mail")
                .setAutoWidth(true);

        allContactsGrid.setWidthFull();
        allContactsGrid.setHeight("300px");
        List<Contact> allContacts = contactService.findAll();
        allContactsGrid.setItems(allContacts);

        allContactsGrid.asSingleSelect().addValueChangeListener(event -> {
            Contact selectedContactFromGrid = event.getValue();
            if (selectedContactFromGrid != null) {
                contactSelection.setValue(selectedContactFromGrid);
                lastnameField.setValue(selectedContactFromGrid.getLastname() != null ? selectedContactFromGrid.getLastname() : "");
                firstnameField.setValue(selectedContactFromGrid.getFirstname() != null ? selectedContactFromGrid.getFirstname() : "");
                try {
                    if (selectedContactFromGrid.getPostcode() != null && !selectedContactFromGrid.getPostcode().isEmpty()) {
                        postcodeField.setValue(Integer.parseInt(selectedContactFromGrid.getPostcode()));
                    } else {
                        postcodeField.clear();
                    }
                } catch (NumberFormatException e) {
                    postcodeField.clear();
                    Notification.show("Ungültiges PLZ-Format!");
                }
                cityField.setValue(selectedContactFromGrid.getCity() != null ? selectedContactFromGrid.getCity() : "");
                streetField.setValue(selectedContactFromGrid.getStreetname() != null ? selectedContactFromGrid.getStreetname() : "");
                housenumberField.setValue(selectedContactFromGrid.getHousenumber() != null ? selectedContactFromGrid.getHousenumber() : "");
                emailAddressField.setValue(selectedContactFromGrid.getEmailAddress() != null ? selectedContactFromGrid.getEmailAddress() : "");

                deleteContactButton.setEnabled(true);
            } else {
                contactSelection.clear();
                lastnameField.clear();
                firstnameField.clear();
                postcodeField.clear();
                cityField.clear();
                streetField.clear();
                housenumberField.clear();
                emailAddressField.clear();
                deleteContactButton.setEnabled(false);
            }
        });

        rightLayout.add(allContactsTitle, allContactsGrid);

        contactSelection.addValueChangeListener(event -> {
            Contact selectedContact = event.getValue();
            if (selectedContact != null) {
                lastnameField.setValue(selectedContact.getLastname() != null ? selectedContact.getLastname() : "");
                firstnameField.setValue(selectedContact.getFirstname() != null ? selectedContact.getFirstname() : "");
                try {
                    if (selectedContact.getPostcode() != null && !selectedContact.getPostcode().isEmpty()) {
                        postcodeField.setValue(Integer.parseInt(selectedContact.getPostcode()));
                    } else {
                        postcodeField.clear();
                    }
                } catch (NumberFormatException e) {
                    postcodeField.clear();
                    Notification.show("Ungültiges PLZ-Format!");
                }
                cityField.setValue(selectedContact.getCity() != null ? selectedContact.getCity() : "");
                streetField.setValue(selectedContact.getStreetname() != null ? selectedContact.getStreetname() : "");
                housenumberField.setValue(selectedContact.getHousenumber() != null ? selectedContact.getHousenumber() : "");
                emailAddressField.setValue(selectedContact.getEmailAddress() != null ? selectedContact.getEmailAddress() : "");

                deleteContactButton.setEnabled(true);
            } else {
                deleteContactButton.setEnabled(false);
                lastnameField.clear();
                firstnameField.clear();
                postcodeField.clear();
                cityField.clear();
                streetField.clear();
                housenumberField.clear();
                emailAddressField.clear();
            }
        });

        deleteContactButton.setEnabled(false);

        contactCancelButton.addClickListener(event -> contactDialog.close());
        contactSaveButton.addClickListener(event -> {
            Contact contactToSave;
            if (contactSelection.getValue() != null) {
                contactToSave = contactSelection.getValue();
            } else {
                contactToSave = new Contact();
            }

            try {
                contactBinder.writeBean(contactToSave);

                contactService.save(contactToSave);
                contactSelection.setItems(contactService.findAll());
                allContactsGrid.setItems(contactService.findAll());
                Notification.show("Kontakt gespeichert.", 3000, Notification.Position.MIDDLE);
                contactSelection.setValue(contactToSave);
                contactDialog.close();

            } catch (com.vaadin.flow.data.binder.ValidationException e) {
                if (e.getValidationErrors().stream().anyMatch(error -> error.getErrorMessage().endsWith("ist erforderlich"))) {
                    Notification.show("Bitte fülle alle Felder aus!", 5000, Notification.Position.MIDDLE);
                } else {
                    Notification.show("Bitte korrigiere die markierten Felder.", 5000, Notification.Position.MIDDLE);
                }
            } catch (RuntimeException e) {
                Notification.show("Fehler bei der PLZ-Eingabe: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
            } catch (Exception e) {
                Notification.show("Fehler beim Speichern des Kontakts: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
                e.printStackTrace();
            }
        });

        deleteContactButton.addClickListener(event -> {
            Contact selectedContactFromGrid = allContactsGrid.asSingleSelect().getValue();

            if (selectedContactFromGrid != null) {
                ConfirmDialog confirmDialog = new ConfirmDialog();
                confirmDialog.setHeader("Kontakt löschen?");
                confirmDialog.setText("Möchtest Du den Kontakt '" + (selectedContactFromGrid.getFirstname() != null ? selectedContactFromGrid.getFirstname() : "") + " " + (selectedContactFromGrid.getLastname() != null ? selectedContactFromGrid.getLastname() : "") + "' wirklich löschen? Dies kann nicht rückgängig gemacht werden."); // Klartext-Handling hier
                confirmDialog.setCancelable(true);
                confirmDialog.setConfirmText("Löschen");
                confirmDialog.setConfirmButtonTheme("error primary");
                confirmDialog.addConfirmListener(confirmEvent -> {
                    try {
                        contactService.delete(selectedContactFromGrid);

                        Notification.show("Kontakt erfolgreich gelöscht!", 3000, Notification.Position.MIDDLE);
                        allContactsGrid.setItems(contactService.findAll());
                        contactSelection.clear();
                        lastnameField.clear();
                        firstnameField.clear();
                        postcodeField.clear();
                        cityField.clear();
                        streetField.clear();
                        housenumberField.clear();
                        emailAddressField.clear();
                        deleteContactButton.setEnabled(false);

                    } catch (DataIntegrityViolationException e) {
                        List<User> connectedUsers = userRepository.findAllByContact(selectedContactFromGrid);
                        String userDetails = "";
                        if (!connectedUsers.isEmpty()) {
                            StringBuilder sb = new StringBuilder();
                            for (User user : connectedUsers) {
                                String userName = (user.getContact() != null && user.getContact().getFirstname() != null ? user.getContact().getFirstname() : "N/A");
                                String userLastName = (user.getContact() != null && user.getContact().getLastname() != null ? user.getContact().getLastname() : "N/A");
                                sb.append(userName).append(" ").append(userLastName).append("; ");
                            }
                            userDetails = " Kontakt ist noch mit folgendem Benutzer verbunden: " + sb.toString().trim();
                        } else {
                            userDetails = " Es sind noch verbundene Einträge vorhanden, die das Löschen verhindern.";
                        }

                        Notification.show(
                                "Fehler beim Löschen des Kontakts! Lösche zuerst den Benutzer:" + userDetails,
                                7000, Notification.Position.MIDDLE);
                        e.printStackTrace();
                    } catch (Exception e) {
                        Notification.show("Ein unerwarteter Fehler ist aufgetreten: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
                        e.printStackTrace();
                    }
                });
                confirmDialog.open();
            } else {
                Notification.show("Bitte wähle zuerst einen Kontakt zum Löschen aus.", 3000, Notification.Position.MIDDLE);
            }
        });


        HorizontalLayout mainLayout = new HorizontalLayout(leftLayout, rightLayout);
        mainLayout.addClassName("topLayout");
        mainLayout.setWidthFull();
        mainLayout.setSpacing(false);
        mainLayout.setPadding(false);
        mainLayout.setAlignItems(Alignment.STRETCH);
        mainLayout.getStyle().set("gap", "0");

        contactDialog.add(mainLayout);
        contactDialog.open();
    }

    /**
     * Öffnet einen Dialog zur Auswahl für die Verwaltung von Attributen wie Marke, Typ und Modus.
     * Der Dialog bietet Buttons für die Verwaltung der einzelnen Attribute.
     * Die Attribute können über separate Dialoge verwaltet werden, die jeweils eine Übersicht und Bearbeitungsmöglichkeiten bieten.
     * Die Attribute werden in einem Grid angezeigt, das eine Filterfunktion bietet.
     * Die Attribute können hinzugefügt, bearbeitet und gelöscht werden.
     */
    private void openAttributeSelectionDialog() {
        Dialog selectionDialog = new Dialog();
        selectionDialog.setCloseOnEsc(true);
        selectionDialog.setCloseOnOutsideClick(true);

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(true);
        dialogLayout.setAlignItems(Alignment.CENTER);
        dialogLayout.addClassName("attributeLayout");

        H2 title = new H2("Welche Attribute möchtest Du verwalten?");


        Button brandButton = new Button("Marke", event -> {
            selectionDialog.close();
            openBrandManagementDialog();
        });
        brandButton.setWidthFull();
        brandButton.addClassName("managementButtonsLayout");

        Button categoryButton = new Button("Typ", event -> {
            selectionDialog.close();
            openCategoryManagementDialog();
        });
        categoryButton.setWidthFull();
        categoryButton.addClassName("managementButtonsLayout");

        Button operatingModeButton = new Button("Modus", event -> {
            selectionDialog.close();
            openOperatingModeManagementDialog();
        });
        operatingModeButton.setWidthFull();
        operatingModeButton.addClassName("managementButtonsLayout");

        dialogLayout.add(title, brandButton, categoryButton, operatingModeButton);
        selectionDialog.add(dialogLayout);
        selectionDialog.open();
    }

    /* * Öffnet einen Dialog zur Verwaltung von Marken.
     * Der Dialog ermöglicht das Hinzufügen, Bearbeiten und Löschen von Marken.
     * Es gibt eine Filterfunktion, um Marken zu suchen.
     * Die Marken werden in einem Grid angezeigt, das eine Übersicht aller Marken bietet.
     */
    private void openBrandManagementDialog() {
        Dialog brandDialog = new Dialog();
        brandDialog.setCloseOnEsc(true);
        brandDialog.setCloseOnOutsideClick(true);

        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setPadding(true);
        contentLayout.setSpacing(true);
        contentLayout.setSizeFull();
        contentLayout.addClassName("attributeGridLayout");

        H2 allBrandsTitle = new H2("Übersicht aller Marken");
        contentLayout.add(allBrandsTitle);

        Button addBrandButton = new Button("Neu", new Icon(VaadinIcon.PLUS));
        addBrandButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addBrandButton.addClassName("managementButtonsLayout");

        HorizontalLayout addBrandButtonLayout = new HorizontalLayout(addBrandButton);
        addBrandButtonLayout.setWidthFull();
        addBrandButtonLayout.setJustifyContentMode(JustifyContentMode.START);
        addBrandButtonLayout.setAlignItems(Alignment.BASELINE);
        contentLayout.add(addBrandButtonLayout);

        TextField brandNameField = new TextField("Marken-Name");
        brandNameField.setWidthFull();
        brandNameField.addValueChangeListener(event -> {
            String currentValue = event.getValue();
            if (currentValue != null && !currentValue.isEmpty()) {
                String formattedValue = currentValue.substring(0, 1).toUpperCase() + currentValue.substring(1);
                brandNameField.setValue(formattedValue);
            }
        });

        Button saveBrandButton = new Button("Speichern", new Icon(VaadinIcon.CHECK));
        saveBrandButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancelEditButton = new Button("Abbrechen", new Icon(VaadinIcon.CLOSE_CIRCLE));

        HorizontalLayout editFormLayout = new HorizontalLayout(brandNameField, saveBrandButton, cancelEditButton);
        editFormLayout.addClassName("managementButtonsLayout");
        editFormLayout.setAlignItems(Alignment.END);
        editFormLayout.setWidthFull();
        editFormLayout.setSpacing(true);
        editFormLayout.setVisible(false);
        editFormLayout.addClassName("hidden-collapsed");

        contentLayout.add(editFormLayout);

        AtomicReference<Brand> currentEditingBrand = new AtomicReference<>();

        Grid<Brand> brandGrid = new Grid<>(Brand.class, false);
        Grid.Column<Brand> brandNameColumn = brandGrid.addColumn(Brand::getName)
                .setHeader("Marke")
                .setAutoWidth(true)
                .setKey("name")
                .setResizable(true);
        brandGrid.setWidthFull();
        brandGrid.addClassName("attributeGridLayout");
        brandGrid.setItems(brandService.findAll());

        HeaderRow filterRow = brandGrid.appendHeaderRow();

        ComboBox<Brand> brandDialogFilter = new ComboBox<>();
        brandDialogFilter.setPlaceholder("Filtern");
        brandDialogFilter.setWidthFull();
        brandDialogFilter.setClearButtonVisible(true);
        brandDialogFilter.setItems(brandService.findAll());
        brandDialogFilter.setItemLabelGenerator(Brand::getName);
        brandDialogFilter.addValueChangeListener(event -> {
            Brand selectedBrand = event.getValue();
            List<Brand> filteredBrands;
            if (selectedBrand == null) {
                filteredBrands = brandService.findAll();
            } else {
                filteredBrands = brandService.findAll().stream()
                        .filter(brand -> brand.equals(selectedBrand))
                        .collect(Collectors.toList());
            }
            brandGrid.setItems(filteredBrands);
        });
        filterRow.getCell(brandNameColumn).setComponent(createFilterWrapper(brandDialogFilter));

        brandGrid.addComponentColumn(brand -> {
            Button editButton = new Button(new Icon(VaadinIcon.EDIT));
            editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editButton.addClickListener(e -> {
                brandNameField.setValue(brand.getName());
                editFormLayout.setVisible(true);
                editFormLayout.removeClassName("hidden-collapsed");
                currentEditingBrand.set(brand);
                brandNameField.focus();
            });

            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            deleteButton.addClickListener(e -> confirmAndDeleteBrand(brand, brandGrid));
            return new HorizontalLayout(editButton, deleteButton);
        }).setHeader("Aktion").setFlexGrow(0).setWidth("100px");

        contentLayout.add(brandGrid);
        contentLayout.setFlexGrow(1, brandGrid);

        Button closeDialogButton = new Button("Schliessen", new Icon(VaadinIcon.CLOSE));
        closeDialogButton.addClassName("managementButtonsLayout");
        closeDialogButton.addClickListener(event -> brandDialog.close());
        HorizontalLayout dialogFooter = new HorizontalLayout(closeDialogButton);
        dialogFooter.setWidthFull();
        dialogFooter.addClassName("managementButtonsLayout");
        dialogFooter.setJustifyContentMode(JustifyContentMode.END);
        contentLayout.add(dialogFooter);

        brandDialog.add(contentLayout);
        brandDialog.open();

        addBrandButton.addClickListener(e -> {
            brandNameField.clear();
            brandNameField.setPlaceholder("Marke eingeben");
            editFormLayout.setVisible(true);
            editFormLayout.removeClassName("hidden-collapsed");
            currentEditingBrand.set(null);
            brandNameField.focus();
        });

        saveBrandButton.addClickListener(event -> {
            String brandName = brandNameField.getValue().trim();
            if (brandName.isEmpty()) {
                Notification.show("Bitte gebe einen Marken-Namen ein.", 3000, Notification.Position.MIDDLE);
                return;
            }

            boolean nameExists = brandService.existsByName(brandName);
            boolean isEditingSameName = (currentEditingBrand.get() != null && currentEditingBrand.get().getName().equalsIgnoreCase(brandName));

            if (nameExists && !isEditingSameName) {
                Notification.show("Die Marke '" + brandName + "' ist bereits vorhanden.", 5000, Notification.Position.MIDDLE);
            } else {
                Brand brandToSave;
                if (currentEditingBrand.get() == null) {
                    brandToSave = new Brand();
                    brandToSave.setName(brandName);
                    Notification.show("Marke '" + brandName + "' gespeichert!");
                } else {
                    brandToSave = currentEditingBrand.get();
                    brandToSave.setName(brandName);
                    Notification.show("Marke '" + brandName + "' aktualisiert!");
                }
                brandService.save(brandToSave);
                brandGrid.setItems(brandService.findAll());
                brandFilter.setItems(brandService.findAll());
                brandDialogFilter.setItems(brandService.findAll());
                brandDialogFilter.clear();
                editFormLayout.setVisible(false);
                editFormLayout.addClassName("hidden-collapsed");
                currentEditingBrand.set(null);
            }

        });

        brandGrid.asSingleSelect().addValueChangeListener(event -> {
            Brand selectedBrand = event.getValue();
            if (selectedBrand != null) {
                brandNameField.setValue(selectedBrand.getName());
                editFormLayout.setVisible(true);
                editFormLayout.removeClassName("hidden-collapsed");
                currentEditingBrand.set(selectedBrand);
                brandNameField.focus();
            }
        });

        cancelEditButton.addClickListener(e -> {
            brandNameField.clear();
            brandNameField.setPlaceholder("Marken-Name eingeben");
            editFormLayout.setVisible(false);
            editFormLayout.addClassName("hidden-collapsed");
            currentEditingBrand.set(null);
        });
    }

    private void confirmAndDeleteBrand(Brand brand, Grid<Brand> brandGrid) {
        List<Article> linkedArticles = articleService.findByBrand(brand);

        if (!linkedArticles.isEmpty()) {
            Dialog errorDialog = new Dialog();
            errorDialog.setCloseOnEsc(true);
            errorDialog.setCloseOnOutsideClick(true);
            VerticalLayout errorLayout = new VerticalLayout();
            errorLayout.addClassName("attributeGridLayout");
            errorLayout.add(new H2("Löschen nicht möglich!"));
            errorLayout.add(new Text("Die Marke '" + brand.getName() + "' kann nicht gelöscht werden, da noch folgende Artikel damit verknüpft sind:"));

            UnorderedList articleList = new UnorderedList();
            linkedArticles.forEach(article -> articleList.add(new ListItem("ID: " + article.getArticleId() + ", Name: " + article.getArticlename())));
            errorLayout.add(articleList);

            Button closeErrorButton = new Button("OK", e -> errorDialog.close());
            errorLayout.add(closeErrorButton);
            errorDialog.add(errorLayout);
            errorDialog.addClassName("managementButtonsLayout");
            errorDialog.open();
        } else {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setHeader("Marke löschen?");
            dialog.setText("Möchtest Du die Marke '" + brand.getName() + "' wirklich löschen?");

            dialog.setCancelable(true);
            dialog.setConfirmText("Löschen");
            dialog.setConfirmButtonTheme("error primary");
            dialog.setCancelText("Abbrechen");

            dialog.addConfirmListener(event -> {
                brandService.delete(brand);
                brandGrid.setItems(brandService.findAll());
                brandFilter.setItems(brandService.findAll());
                Notification.show("Marke '" + brand.getName() + "' wurde gelöscht.", 3000, Notification.Position.MIDDLE);
                event.getSource().close();
            });

            dialog.addCancelListener(event -> event.getSource().close());

            dialog.open();
        }
    }

    private void openCategoryManagementDialog() {
        Dialog categoryDialog = new Dialog();
        categoryDialog.setCloseOnEsc(true);
        categoryDialog.setCloseOnOutsideClick(true);
        categoryDialog.addClassName("attributeDialog");

        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setPadding(true);
        contentLayout.setSpacing(true);
        contentLayout.setSizeFull();
        contentLayout.addClassName("attributeGridLayout");

        H2 allCategoriesTitle = new H2("Übersicht aller Typen");
        contentLayout.add(allCategoriesTitle);

        Button addCategoryButton = new Button("Neu", new Icon(VaadinIcon.PLUS));
        addCategoryButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addCategoryButton.addClassName("managementButtonsLayout");

        HorizontalLayout addCategoryButtonLayout = new HorizontalLayout(addCategoryButton);
        addCategoryButtonLayout.setWidthFull();
        addCategoryButtonLayout.setJustifyContentMode(JustifyContentMode.START);
        addCategoryButtonLayout.setAlignItems(Alignment.BASELINE);
        contentLayout.add(addCategoryButtonLayout);

        TextField categoryNameField = new TextField("Typ-Name");
        categoryNameField.setWidthFull();
        categoryNameField.addValueChangeListener(event -> {
            String currentValue = event.getValue();
            if (currentValue != null && !currentValue.isEmpty()) {
                String formattedValue = currentValue.substring(0, 1).toUpperCase() + currentValue.substring(1);
                categoryNameField.setValue(formattedValue);
            }
        });

        Button saveCategoryButton = new Button("Speichern", new Icon(VaadinIcon.CHECK));
        saveCategoryButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancelEditButton = new Button("Abbrechen", new Icon(VaadinIcon.CLOSE_CIRCLE));

        HorizontalLayout editFormLayout = new HorizontalLayout(categoryNameField, saveCategoryButton, cancelEditButton);
        editFormLayout.addClassName("managementButtonsLayout");
        editFormLayout.setAlignItems(Alignment.END);
        editFormLayout.setWidthFull();
        editFormLayout.setSpacing(true);
        editFormLayout.setVisible(false);
        editFormLayout.addClassName("hidden-collapsed");

        contentLayout.add(editFormLayout);

        AtomicReference<Category> currentEditingCategory = new AtomicReference<>();

        Grid<Category> categoryGrid = new Grid<>(Category.class, false);
        Grid.Column<Category> categoryNameColumn = categoryGrid.addColumn(Category::getName)
                .setHeader("Typ")
                .setAutoWidth(true)
                .setKey("name")
                .setResizable(true);
        categoryGrid.setWidthFull();
        categoryGrid.addClassName("attributeGridLayout");
        categoryGrid.setItems(categoryService.findAll());

        HeaderRow filterRow = categoryGrid.appendHeaderRow();

        ComboBox<Category> categoryDialogFilter = new ComboBox<>();
        categoryDialogFilter.setPlaceholder("Filtern");
        categoryDialogFilter.setWidthFull();
        categoryDialogFilter.setClearButtonVisible(true);
        categoryDialogFilter.setItems(categoryService.findAll()); // Alle Kategorien laden
        categoryDialogFilter.setItemLabelGenerator(Category::getName);
        categoryDialogFilter.addValueChangeListener(event -> {
            Category selectedCategory = event.getValue();
            List<Category> filteredCategories;
            if (selectedCategory == null) {
                filteredCategories = categoryService.findAll();
            } else {
                filteredCategories = categoryService.findAll().stream()
                        .filter(category -> category.equals(selectedCategory))
                        .collect(Collectors.toList());
            }
            categoryGrid.setItems(filteredCategories);
        });
        filterRow.getCell(categoryNameColumn).setComponent(createFilterWrapper(categoryDialogFilter));


        categoryGrid.addComponentColumn(category -> {
            Button editButton = new Button(new Icon(VaadinIcon.EDIT));
            editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editButton.addClickListener(e -> {
                categoryNameField.setValue(category.getName());
                editFormLayout.setVisible(true);
                editFormLayout.removeClassName("hidden-collapsed");
                currentEditingCategory.set(category);
                categoryNameField.focus();
            });

            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            deleteButton.addClickListener(e -> confirmAndDeleteCategory(category, categoryGrid));
            return new HorizontalLayout(editButton, deleteButton);
        }).setHeader("Aktion").setFlexGrow(0).setWidth("100px");

        contentLayout.add(categoryGrid);
        contentLayout.setFlexGrow(1, categoryGrid);

        Button closeDialogButton = new Button("Schliessen", new Icon(VaadinIcon.CLOSE));
        closeDialogButton.addClassName("managementButtonsLayout");
        closeDialogButton.addClickListener(event -> categoryDialog.close());
        HorizontalLayout dialogFooter = new HorizontalLayout(closeDialogButton);
        dialogFooter.setWidthFull();
        dialogFooter.addClassName("managementButtonsLayout");
        dialogFooter.setJustifyContentMode(JustifyContentMode.END);
        contentLayout.add(dialogFooter);

        categoryDialog.add(contentLayout);
        categoryDialog.open();

        addCategoryButton.addClickListener(e -> {
            categoryNameField.clear();
            categoryNameField.setPlaceholder("Typ eingeben");
            editFormLayout.setVisible(true);
            editFormLayout.removeClassName("hidden-collapsed");
            currentEditingCategory.set(null);
            categoryNameField.focus();
        });

        saveCategoryButton.addClickListener(event -> {
            String categoryName = categoryNameField.getValue().trim();
            if (categoryName.isEmpty()) {
                Notification.show("Bitte gebe einen Typ-Namen ein.", 3000, Notification.Position.MIDDLE);
                return;
            }

            boolean nameExists = categoryService.existsByName(categoryName);
            boolean isEditingSameName = (currentEditingCategory.get() != null && currentEditingCategory.get().getName().equalsIgnoreCase(categoryName));

            if (nameExists && !isEditingSameName) {
                Notification.show("Der Typ '" + categoryName + "' ist bereits vorhanden.", 5000, Notification.Position.MIDDLE);
            } else {
                Category categoryToSave;
                if (currentEditingCategory.get() == null) {
                    categoryToSave = new Category();
                    categoryToSave.setName(categoryName);
                    Notification.show("Typ '" + categoryName + "' gespeichert!");
                } else {
                    categoryToSave = currentEditingCategory.get();
                    categoryToSave.setName(categoryName);
                    Notification.show("Typ '" + categoryName + "' aktualisiert!");
                }
                categoryService.save(categoryToSave);
                categoryGrid.setItems(categoryService.findAll());
                categoryDialogFilter.setItems(categoryService.findAll());
                categoryFilter.setItems(categoryService.findAll());
                categoryDialogFilter.clear();
                editFormLayout.setVisible(false);
                editFormLayout.addClassName("hidden-collapsed");
                currentEditingCategory.set(null);
            }
        });

        categoryGrid.asSingleSelect().addValueChangeListener(event -> {
            Category selectedCategory = event.getValue();
            if (selectedCategory != null) {
                categoryNameField.setValue(selectedCategory.getName());
                editFormLayout.setVisible(true);
                editFormLayout.removeClassName("hidden-collapsed");
                currentEditingCategory.set(selectedCategory);
                categoryNameField.focus();
            }
        });

        cancelEditButton.addClickListener(e -> {
            categoryNameField.clear();
            categoryNameField.setPlaceholder("Typ-Name eingeben");
            editFormLayout.setVisible(false);
            editFormLayout.addClassName("hidden-collapsed");
            currentEditingCategory.set(null);
        });
    }

    private void confirmAndDeleteCategory(Category category, Grid<Category> categoryGrid) {
        List<Article> linkedArticles = articleService.findByCategory(category);

        if (!linkedArticles.isEmpty()) {
            Dialog errorDialog = new Dialog();
            errorDialog.setCloseOnEsc(true);
            errorDialog.setCloseOnOutsideClick(true);
            VerticalLayout errorLayout = new VerticalLayout();
            errorLayout.add(new H2("Löschen nicht möglich!"));
            errorLayout.add(new Text("Der Typ '" + category.getName() + "' kann nicht gelöscht werden, da noch folgende Artikel damit verknüpft sind:"));

            UnorderedList articleList = new UnorderedList();
            linkedArticles.forEach(article -> articleList.add(new ListItem("ID: " + article.getArticleId() + ", Name: " + article.getArticlename())));
            errorLayout.add(articleList);

            Button closeErrorButton = new Button("OK", e -> errorDialog.close());
            errorLayout.add(closeErrorButton);
            errorDialog.add(errorLayout);
            errorDialog.open();
        } else {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setHeader("Typ löschen?");
            dialog.setText("Möchtest Du den Typ '" + category.getName() + "' wirklich löschen?");

            dialog.setCancelable(true);
            dialog.setConfirmText("Löschen");
            dialog.setConfirmButtonTheme("error primary");
            dialog.setCancelText("Abbrechen");

            dialog.addConfirmListener(event -> {
                categoryService.delete(category);
                categoryGrid.setItems(categoryService.findAll());
                categoryFilter.setItems(categoryService.findAll());
                Notification.show("Typ '" + category.getName() + "' wurde gelöscht.", 3000, Notification.Position.MIDDLE);
                event.getSource().close();
            });

            dialog.addCancelListener(event -> event.getSource().close());

            dialog.open();
        }
    }


    private void openOperatingModeManagementDialog() {
        Dialog operatingModeDialog = new Dialog();
        operatingModeDialog.setCloseOnEsc(true);
        operatingModeDialog.setCloseOnOutsideClick(true);

        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setPadding(true);
        contentLayout.setSpacing(true);
        contentLayout.setSizeFull();
        contentLayout.addClassName("attributeGridLayout");

        H2 allOperatingModesTitle = new H2("Übersicht vom Modus");
        contentLayout.add(allOperatingModesTitle);

        Button addOperatingModeButton = new Button("Neu", new Icon(VaadinIcon.PLUS));
        addOperatingModeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addOperatingModeButton.addClassName("managementButtonsLayout");

        HorizontalLayout addOperatingModeButtonLayout = new HorizontalLayout(addOperatingModeButton);
        addOperatingModeButtonLayout.setWidthFull();
        addOperatingModeButtonLayout.setJustifyContentMode(JustifyContentMode.START);
        addOperatingModeButtonLayout.setAlignItems(Alignment.BASELINE);
        contentLayout.add(addOperatingModeButtonLayout);

        TextField operatingModeNameField = new TextField("Modus-Name");
        operatingModeNameField.setWidthFull();
        operatingModeNameField.addValueChangeListener(event -> {
            String currentValue = event.getValue();
            if (currentValue != null && !currentValue.isEmpty()) {
                String formattedValue = currentValue.substring(0, 1).toUpperCase() + currentValue.substring(1);
                operatingModeNameField.setValue(formattedValue);
            }
        });

        Button saveOperatingModeButton = new Button("Speichern", new Icon(VaadinIcon.CHECK));
        saveOperatingModeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancelEditButton = new Button("Abbrechen", new Icon(VaadinIcon.CLOSE_CIRCLE));

        HorizontalLayout editFormLayout = new HorizontalLayout(operatingModeNameField, saveOperatingModeButton, cancelEditButton);
        editFormLayout.addClassName("managementButtonsLayout");
        editFormLayout.setAlignItems(Alignment.END);
        editFormLayout.setWidthFull();
        editFormLayout.setSpacing(true);
        editFormLayout.setVisible(false);
        editFormLayout.addClassName("hidden-collapsed");

        contentLayout.add(editFormLayout);

        AtomicReference<OperatingMode> currentEditingOperatingMode = new AtomicReference<>();

        Grid<OperatingMode> operatingModeGrid = new Grid<>(OperatingMode.class, false);
        Grid.Column<OperatingMode> operatingModeNameColumn = operatingModeGrid.addColumn(OperatingMode::getName)
                .setHeader("Modus")
                .setAutoWidth(true)
                .setKey("name")
                .setResizable(true);
        operatingModeGrid.setWidthFull();
        operatingModeGrid.addClassName("attributeGridLayout");
        operatingModeGrid.setItems(operatingModeService.findAll());

        HeaderRow filterRow = operatingModeGrid.appendHeaderRow();

        ComboBox<OperatingMode> operatingModeDialogFilter = new ComboBox<>();
        operatingModeDialogFilter.setPlaceholder("Filtern");
        operatingModeDialogFilter.setWidthFull();
        operatingModeDialogFilter.setClearButtonVisible(true);
        operatingModeDialogFilter.setItems(operatingModeService.findAll());
        operatingModeDialogFilter.setItemLabelGenerator(OperatingMode::getName);
        operatingModeDialogFilter.addValueChangeListener(event -> {
            OperatingMode selectedOperatingMode = event.getValue();
            List<OperatingMode> filteredOperatingModes;
            if (selectedOperatingMode == null) {
                filteredOperatingModes = operatingModeService.findAll();
            } else {
                filteredOperatingModes = operatingModeService.findAll().stream()
                        .filter(operatingMode -> operatingMode.equals(selectedOperatingMode))
                        .collect(Collectors.toList());
            }
            operatingModeGrid.setItems(filteredOperatingModes);
        });
        filterRow.getCell(operatingModeNameColumn).setComponent(createFilterWrapper(operatingModeDialogFilter));

        operatingModeGrid.addComponentColumn(operatingMode -> {
            Button editButton = new Button(new Icon(VaadinIcon.EDIT));
            editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editButton.addClickListener(e -> {
                operatingModeNameField.setValue(operatingMode.getName());
                editFormLayout.setVisible(true);
                editFormLayout.removeClassName("hidden-collapsed");
                editFormLayout.setFlexGrow(0);
                currentEditingOperatingMode.set(operatingMode);
                operatingModeNameField.focus();
            });

            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            deleteButton.addClickListener(e -> confirmAndDeleteOperatingMode(operatingMode, operatingModeGrid));

            HorizontalLayout actionsLayout = new HorizontalLayout(editButton, deleteButton);
            actionsLayout.setSpacing(false);
            actionsLayout.setPadding(false);
            return actionsLayout;
        }).setHeader("Aktion").setFlexGrow(0).setWidth("100px");

        contentLayout.add(operatingModeGrid);
        contentLayout.setFlexGrow(1, operatingModeGrid);

        Button closeDialogButton = new Button("Schliessen", new Icon(VaadinIcon.CLOSE));
        closeDialogButton.addClassName("managementButtonsLayout");
        closeDialogButton.addClickListener(event -> operatingModeDialog.close());
        HorizontalLayout dialogFooter = new HorizontalLayout(closeDialogButton);
        dialogFooter.setWidthFull();
        dialogFooter.addClassName("managementButtonsLayout");
        dialogFooter.setJustifyContentMode(JustifyContentMode.END);
        contentLayout.add(dialogFooter);

        operatingModeDialog.add(contentLayout);
        operatingModeDialog.open();

        addOperatingModeButton.addClickListener(e -> {
            operatingModeNameField.clear();
            operatingModeNameField.setPlaceholder("Modus eingeben");
            editFormLayout.setVisible(true);
            editFormLayout.removeClassName("hidden-collapsed");
            currentEditingOperatingMode.set(null);
            operatingModeNameField.focus();
        });

        saveOperatingModeButton.addClickListener(event -> {
            String operatingModeName = operatingModeNameField.getValue().trim();
            if (operatingModeName.isEmpty()) {
                Notification.show("Bitte gebe einen Modus-Namen ein.", 3000, Notification.Position.MIDDLE);
                return;
            }

            boolean nameExists = operatingModeService.existsByName(operatingModeName);
            boolean isEditingSameName = (currentEditingOperatingMode.get() != null && currentEditingOperatingMode.get().getName().equalsIgnoreCase(operatingModeName));

            if (nameExists && !isEditingSameName) {
                Notification.show("Der Modus '" + operatingModeName + "' ist bereits vorhanden.", 5000, Notification.Position.MIDDLE);
            } else {
                OperatingMode operatingModeToSave;
                if (currentEditingOperatingMode.get() == null) {
                    operatingModeToSave = new OperatingMode();
                    operatingModeToSave.setName(operatingModeName);
                    Notification.show("Modus '" + operatingModeName + "' gespeichert!");
                } else {
                    operatingModeToSave = currentEditingOperatingMode.get();
                    operatingModeToSave.setName(operatingModeName);
                    Notification.show("Modus '" + operatingModeName + "' aktualisiert!");
                }
                operatingModeService.save(operatingModeToSave);
                operatingModeGrid.setItems(operatingModeService.findAll());
                operatingModeDialogFilter.setItems(operatingModeService.findAll());
                operatingModeFilter.setItems(operatingModeService.findAll());
                operatingModeDialogFilter.clear();
                editFormLayout.setVisible(false);
                editFormLayout.addClassName("hidden-collapsed");
                currentEditingOperatingMode.set(null);
            }
        });

        operatingModeGrid.asSingleSelect().addValueChangeListener(event -> {
            OperatingMode selectedOperatingMode = event.getValue();
            if (selectedOperatingMode != null) {
                operatingModeNameField.setValue(selectedOperatingMode.getName());
                editFormLayout.setVisible(true);
                editFormLayout.removeClassName("hidden-collapsed");
                currentEditingOperatingMode.set(selectedOperatingMode);
                operatingModeNameField.focus();
            }
        });

        cancelEditButton.addClickListener(e -> {
            operatingModeNameField.clear();
            operatingModeNameField.setPlaceholder("Modusname eingeben");
            editFormLayout.setVisible(false);
            editFormLayout.addClassName("hidden-collapsed");
            currentEditingOperatingMode.set(null);
        });
    }

    private void confirmAndDeleteOperatingMode(OperatingMode operatingMode, Grid<OperatingMode> operatingModeGrid) {
        List<Article> linkedArticles = articleService.findByOperatingMode(operatingMode);

        if (!linkedArticles.isEmpty()) {
            Dialog errorDialog = new Dialog();
            errorDialog.setCloseOnEsc(true);
            errorDialog.setCloseOnOutsideClick(true);
            VerticalLayout errorLayout = new VerticalLayout();
            errorLayout.add(new H2("Löschen nicht möglich!"));
            errorLayout.add(new Text("Der Modus '" + operatingMode.getName() + "' kann nicht gelöscht werden, da noch folgende Artikel damit verknüpft sind:"));

            UnorderedList articleList = new UnorderedList();
            linkedArticles.forEach(article -> articleList.add(new ListItem("ID: " + article.getArticleId() + ", Name: " + article.getArticlename())));
            errorLayout.add(articleList);

            Button closeErrorButton = new Button("OK", e -> errorDialog.close());
            errorLayout.add(closeErrorButton);
            errorDialog.add(errorLayout);
            errorDialog.open();

        } else {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setHeader("Modus löschen?");
            dialog.setText("Möchtest Du den Modus '" + operatingMode.getName() + "' wirklich löschen?");

            dialog.setCancelable(true);
            dialog.setConfirmText("Löschen");
            dialog.setConfirmButtonTheme("error primary");
            dialog.setCancelText("Abbrechen");

            dialog.addConfirmListener(event -> {
                operatingModeService.delete(operatingMode);
                operatingModeGrid.setItems(operatingModeService.findAll());
                operatingModeFilter.setItems(operatingModeService.findAll());
                Notification.show("Modus '" + operatingMode.getName() + "' wurde gelöscht.", 3000, Notification.Position.MIDDLE);
                event.getSource().close();
            });

            dialog.addCancelListener(event -> event.getSource().close());
            dialog.open();
        }
    }

    private void printArticleLabel(Article article) {
        try {
            if (!articleLabelService.testNetworkPrinterConnection()) {
                Notification.show("Drucker ist nicht erreichbar oder hat Verbindung abgelehnt. Bitte Netzwerkverbindung und Druckerkonfiguration prüfen.",
                        8000, Notification.Position.MIDDLE);
                return; // Abbruch, wenn keine Verbindung besteht
            }
            String zplCode = articleLabelService.generateLabelZpl(article);

            articleLabelService.sendZplToNetworkPrinter(zplCode);

            Notification.show("Etikett für Artikel '" + article.getArticlename() + "' (ID: " + article.getArticleId() + ") wurde **an den Netzwerkdrucker gesendet**.",
                    5000, Notification.Position.MIDDLE);

        } catch (IOException e) {
            Notification.show("Fehler beim Senden des Druckauftrags über das Netzwerk: " + e.getMessage(),
                    8000, Notification.Position.MIDDLE);
            System.err.println("Druckfehler (IOException): " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            Notification.show("Ein unerwarteter Fehler ist beim Drucken aufgetreten: " + e.getMessage(),
                    8000, Notification.Position.MIDDLE);
            System.err.println("Druckfehler (unerwartet): " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void openUserDialog(User userToEdit) {
        Dialog userDialog = new Dialog();
        userDialog.setCloseOnEsc(true);
        userDialog.setCloseOnOutsideClick(true);

        VerticalLayout leftLayout = new VerticalLayout();
        leftLayout.setSpacing(false);
        leftLayout.addClassName("leftLayout");

        H2 dialogTitle = new H2("Benutzer erfassen");
        leftLayout.add(dialogTitle);

        ComboBox<Contact> contactField = new ComboBox<>("Kontakt auswählen");
        contactField.setItems(contactService.findAll());
        contactField.setItemLabelGenerator(contact -> contact.getFirstname() + " " + contact.getLastname());
        contactField.setRequired(true);

        TextField usernameField = new TextField("Benutzername");
        usernameField.setRequired(true);

        PasswordField passwordField = new PasswordField("Passwort");
        passwordField.setRequired(userToEdit == null);
        passwordField.setPlaceholder(userToEdit != null ? "Leer lassen für keine Änderung" : "");


        ComboBox<String> roleField = new ComboBox<>("Rolle auswählen");
        roleField.setItems(User.ADMIN, User.EMPLOYEE);
        roleField.setRequired(true);

        if (userToEdit != null) {
            dialogTitle.setText("Benutzer bearbeiten");
            contactField.setValue(userToEdit.getContact());
            usernameField.setValue(userToEdit.getUsername());
            roleField.setValue(userToEdit.getRole());

            contactField.setEnabled(false);
            usernameField.setEnabled(true);
        } else {
            dialogTitle.setText("Benutzer erfassen");
            contactField.setEnabled(true);
            usernameField.setEnabled(true);
        }

        leftLayout.add(
                contactField,
                usernameField,
                passwordField,
                roleField
        );
        leftLayout.setAlignItems(Alignment.STRETCH);
        contactField.setWidthFull();
        usernameField.setWidthFull();
        passwordField.setWidthFull();
        roleField.setWidthFull();

        User finalUserToSave = userToEdit == null ? new User() : userToEdit;

        Button saveButton = new Button("Speichern", event -> {
            if (contactField.getValue() == null || usernameField.getValue().isEmpty() || roleField.getValue() == null) {
                Notification.show("Bitte fülle alle erforderlichen Felder aus!", 3000, Notification.Position.MIDDLE);
                return;
            }

            if (userToEdit == null) {
                if (passwordField.getValue().isEmpty()) {
                    Notification.show("Bitte gebe ein Passwort für den neuen Benutzer ein!", 3000, Notification.Position.MIDDLE);
                    return;
                }

                List<User> existingUsersForContact = new ArrayList<>();
                Optional<User> foundUser = userService.findByContact(contactField.getValue());
                if (foundUser.isPresent()) {
                    existingUsersForContact.add(foundUser.get());
                }
                if (!existingUsersForContact.isEmpty()) {
                    Notification.show("Für den Kontakt '" + contactField.getValue().getFirstname() + " " + contactField.getValue().getLastname() + "' existiert bereits ein Benutzer!", 5000, Notification.Position.MIDDLE);
                    return;
                }
            }

            finalUserToSave.setContact(contactField.getValue());
            finalUserToSave.setUsername(usernameField.getValue());
            finalUserToSave.setRole(roleField.getValue());
            if (!passwordField.getValue().isEmpty()) {
                finalUserToSave.setPassword(passwordField.getValue());
            }
            try {
                userService.save(finalUserToSave);
                if (userToEdit == null) {
                    Notification.show("Benutzer '" + finalUserToSave.getUsername() + "' erfasst!", 5000, Notification.Position.MIDDLE);
                } else {
                    Notification.show("Benutzer '" + finalUserToSave.getUsername() + "' aktualisiert!", 5000, Notification.Position.MIDDLE);
                }
                userDialog.close();
                refreshUserGrid();
            } catch (DuplicateKeyException e) {
                Notification.show("Fehler beim Speichern: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
            } catch (Exception e) {
                Notification.show("Ein unerwarteter Fehler ist aufgetreten: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
                e.printStackTrace();
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Abbrechen", event -> userDialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        buttonLayout.addClassName("managementButtonsLayout");
        buttonLayout.setJustifyContentMode(JustifyContentMode.END);
        leftLayout.add(buttonLayout);

        List<User> users = userService.findAll();
        Grid<User> userGridInDialog = new Grid<>(User.class, false);

        userGridInDialog.addColumn(user -> user.getContact().getFirstname() + " " + user.getContact().getLastname())
                .setHeader("Kontakt")
                .setAutoWidth(true)
                .setFlexGrow(1)
                .setResizable(true);

        userGridInDialog.addColumn(user -> {
            if (user.getRole() != null) {
                if (user.getRole().equals(User.ADMIN)) {
                    return "AD";
                } else if (user.getRole().equals(User.EMPLOYEE)) {
                    return "MA";
                }
            }
            return "";
        }).setHeader("Rolle").setFlexGrow(0).setWidth("70px").setResizable(true);


        userGridInDialog.addColumn(new ComponentRenderer<>(user -> {
            HorizontalLayout actionsLayout = new HorizontalLayout();
            actionsLayout.setSpacing(false);
            actionsLayout.setPadding(false);
            actionsLayout.setFlexGrow(0);

            Button editButton = new Button(new Icon(VaadinIcon.EDIT));
            editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editButton.getElement().setAttribute("title", "Benutzer bearbeiten");
            editButton.addClickListener(e -> {
                userDialog.close();
                openUserDialog(user);
            });

            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            deleteButton.getElement().setAttribute("title", "Benutzer löschen");
            deleteButton.addClickListener(e -> confirmDeleteUser(user, userDialog));

            actionsLayout.add(editButton, deleteButton);
            return actionsLayout;
        })).setHeader("").setAutoWidth(true).setFlexGrow(0);

        userGridInDialog.setItems(users);

        H2 allUsersTitle = new H2("Übersicht aller Benutzer");

        VerticalLayout rightLayout = new VerticalLayout(allUsersTitle, userGridInDialog);
        rightLayout.addClassName("rightLayout");
        rightLayout.setSpacing(false);
        rightLayout.setAlignItems(Alignment.STRETCH);


        HorizontalLayout mainLayout = new HorizontalLayout(leftLayout, rightLayout);
        mainLayout.addClassName("topLayout");

        mainLayout.setWidthFull();
        userDialog.add(mainLayout);
        userDialog.open();


    }

    private void refreshUserGrid() {
    }


    private void confirmDeleteUser(User user, Dialog parentUserDialog) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Benutzer löschen?");
        dialog.setText("Bist Du sicher, dass Du den Benutzer '"
                + user.getUsername() + "' (" + user.getContact().getFirstname() + " "
                + user.getContact().getLastname() + ") dauerhaft löschen möchtest?");
        dialog.setCancelable(true);
        dialog.setCloseOnEsc(true);
        dialog.setConfirmText("Löschen");
        dialog.setConfirmButtonTheme("error primary");
        dialog.setCancelText("Abbrechen");
        dialog.addConfirmListener(event -> {
            try {
                userService.delete(user.getUserID());
                Notification.show("Benutzer '" + user.getUsername() + "' erfolgreich gelöscht.", 3000, Notification.Position.MIDDLE);
                event.getSource().close();
                if (parentUserDialog != null && parentUserDialog.isOpened()) {
                    parentUserDialog.close();
                }
                refreshUserGrid();
            } catch (Exception e) {
                Notification.show("Fehler beim Löschen des Benutzers: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
                e.printStackTrace();
            }
        });
        dialog.open();
    }
}