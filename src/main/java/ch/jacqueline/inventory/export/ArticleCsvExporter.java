package ch.jacqueline.inventory.export;

import ch.jacqueline.inventory.entity.Article;
import ch.jacqueline.inventory.entity.Contact;
import ch.jacqueline.inventory.entity.Location;
import ch.jacqueline.inventory.services.ArticleService;
import ch.jacqueline.inventory.services.LocationService;
import com.opencsv.CSVWriter;
import com.vaadin.flow.server.StreamResource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
public class ArticleCsvExporter {

    private final ArticleService articleService;
    private final LocationService locationService;

    public ArticleCsvExporter(ArticleService articleService, LocationService locationService) {
        this.articleService = articleService;
        this.locationService = locationService;
    }

    public StreamResource exportAllArticlesToCsv() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(bos, StandardCharsets.UTF_8);
        CSVWriter csvWriter = new CSVWriter(writer);

        String[] headers = {
                "ID", "Artikelname", "Marke", "Kategorie", "Betriebsart",
                "Kaufdatum", "Garantie in Monaten", "Status",
                "Letztes Ausleih Datum", "Name", "Strasse",
                "Hausnummer", "PLZ", "Ort","E-Mail"
        };
        csvWriter.writeNext(headers);

        List<Article> allArticles = articleService.findAll();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        DateTimeFormatter loanDateOnlyFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        for (Article article : allArticles) {
            List<String> rowData = new ArrayList<>();
            rowData.add(article.getArticleId() != null ? article.getArticleId().toString() : "");
            rowData.add(article.getArticlename() != null ? article.getArticlename() : "");
            rowData.add(article.getBrand() != null ? article.getBrand().getName() : "");
            rowData.add(article.getCategory() != null ? article.getCategory().getName() : "");
            rowData.add(article.getOperatingMode() != null ? article.getOperatingMode().getName() : "");
            rowData.add(article.getPurchaseDate() != null ? article.getPurchaseDate().format(dateFormatter) : "");
            rowData.add(article.getWarrantyDuration() != null ? article.getWarrantyDuration().toString() : "");
            rowData.add(article.getDeleted() != null && article.getDeleted() ? "geloescht" : "aktiv");

            List<Location> articleLocations = locationService.findByArticle(article);

            Optional<Location> lastLoanLocationOptional = articleLocations.stream()
                    .filter(loc -> loc.getLoanDate() != null)
                    .max(Comparator.comparing(Location::getLoanDate));

            if (lastLoanLocationOptional.isPresent()) {
                Location lastLocation = lastLoanLocationOptional.get();
                Contact loanContact = lastLocation.getContact();

                rowData.add(lastLocation.getLoanDate() != null ? lastLocation.getLoanDate().format(loanDateOnlyFormatter) : "");
                if (loanContact != null) {
                    rowData.add((loanContact.getFirstname() != null ? loanContact.getFirstname() : "") + " " +
                            (loanContact.getLastname() != null ? loanContact.getLastname() : ""));
                    rowData.add(loanContact.getStreetname() != null ? loanContact.getStreetname() : "");
                    rowData.add(loanContact.getHousenumber() != null ? loanContact.getHousenumber() : "");
                    rowData.add(loanContact.getPostcode() != null ? loanContact.getPostcode() : "");
                    rowData.add(loanContact.getCity() != null ? loanContact.getCity() : "");
                    rowData.add(loanContact.getEmailAddress() != null ? loanContact.getEmailAddress() : "");
                } else {
                    rowData.add(""); // Name
                    rowData.add(""); // Strasse
                    rowData.add(""); // Hausnummer
                    rowData.add(""); // PLZ
                    rowData.add(""); // Ort
                    rowData.add(""); // E-Mail
                }
            } else {
                rowData.add(""); // Datum
                rowData.add(""); // Name
                rowData.add(""); // Strasse
                rowData.add(""); // Hausnummer
                rowData.add(""); // PLZ
                rowData.add(""); // Ort
                rowData.add(""); // E-Mail
            }

            csvWriter.writeNext(rowData.toArray(new String[0]));
        }

        csvWriter.close();
        writer.close();

        StreamResource resource = new StreamResource("maschinenpark_export.csv",
                () -> new ByteArrayInputStream(bos.toByteArray()));
        resource.setContentType("text/csv");
        resource.setCacheTime(0);
        return resource;
    }
}