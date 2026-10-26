package ch.jacqueline.inventory.services;

import ch.jacqueline.inventory.entity.Article;
import ch.jacqueline.inventory.entity.Brand;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

@Service
public class ArticleLabelService {

    private static final String PRINTER_IP = "10.33.100.22";
    private static final int PRINTER_PORT = 9100;

    private static final String LOGO_ZPL_COMMAND = "";

    /**
     * Testing the connection to the network printer.
     * This method attempts to establish a TCP connection to the printer's IP address and port.
     * If the connection is successful, it returns true; otherwise, it returns false.
     */
    public boolean testNetworkPrinterConnection() {
        try (Socket socket = new Socket()) {
            socket.connect(new java.net.InetSocketAddress(PRINTER_IP, PRINTER_PORT), 3000);
            System.out.println("INFO: Verbindung zum Drucker " + PRINTER_IP + ":" + PRINTER_PORT + " erfolgreich hergestellt.");
            return true;
        } catch (IOException e) {
            System.err.println("FEHLER: Verbindung zum Drucker " + PRINTER_IP + ":" + PRINTER_PORT + " fehlgeschlagen: " + e.getMessage());
            return false;
        }
    }

    /**
     * Generated ZPL code for an article label.
     * @param article The article for which the label is generated.
     *                @return The ZPL code as a string.
     */
    public String generateLabelZpl(Article article) {
        StringBuilder zpl = new StringBuilder();

        zpl.append("^XA\r\n");

        int labelWidth = 385;
        int labelHeight = 240;

        zpl.append("^PW").append(labelWidth).append("\r\n");
        zpl.append("^LL").append(labelHeight).append("\r\n");
        zpl.append("^LH0,0\r\n");

        int generalStartX = 45;

        String articleIdString = String.valueOf(article.getArticleId());
        int idFontSizeHeight = 40;
        int idFontSizeWidth = 24;
        int estimatedIdTextWidth = articleIdString.length() * idFontSizeWidth;
        int idXPosition = labelWidth - estimatedIdTextWidth - 10;
        int idYPosition = 15;
        if (idXPosition < 0) idXPosition = 0;
        zpl.append("^FO").append(idXPosition).append(",").append(idYPosition)
                .append("^A0N,").append(idFontSizeHeight).append(",").append(idFontSizeWidth)
                .append("^FD").append(articleIdString).append("^FS\r\n");

        int articleNameX = generalStartX;
        int articleNameY = idYPosition + idFontSizeHeight + 8;
        int articleNameFontHeight = 30;
        int articleNameFontWidth = 18;
        zpl.append("^FO").append(articleNameX).append(",").append(articleNameY)
                .append("^A0N,").append(articleNameFontHeight).append(",").append(articleNameFontWidth)
                .append("^FD").append(article.getArticlename()).append("^FS\r\n");

        Brand brand = article.getBrand();
        int brandNameY;
        int brandNameFontHeight = 24;
        int brandNameFontWidth = 14;
        if (brand != null && brand.getName() != null && !brand.getName().isEmpty()) {
            int brandNameX = generalStartX;
            brandNameY = articleNameY + articleNameFontHeight + 6;
            zpl.append("^FO").append(brandNameX).append(",").append(brandNameY)
                    .append("^A0N,").append(brandNameFontHeight).append(",").append(brandNameFontWidth)
                    .append("^FD").append(brand.getName()).append("^FS\r\n");
        } else {
            brandNameY = articleNameY + articleNameFontHeight + 6;
        }

        String companyName = "Firma Beispielhaft GmbH";
        String addressLine = "Beispielstrasse 14, 4538 Beispielstadt";
        String contactLine = "032 100 00 00 || info@example.ch";

        int firmFontHeight = 24;
        int firmFontWidth = 16;

        int brandToCompanyOffset = 20;
        int firmBlockYStart = brandNameY + brandNameFontHeight + brandToCompanyOffset;

        int companyNameXPos = generalStartX;
        zpl.append("^FO").append(companyNameXPos).append(",").append(firmBlockYStart)
                .append("^A0N,").append(firmFontHeight).append(",").append(firmFontWidth)
                .append("^FD").append(companyName).append("^FS\r\n");

        int addressLineY = firmBlockYStart + firmFontHeight + 5;
        zpl.append("^FO").append(generalStartX).append(",").append(addressLineY)
                .append("^A0N,").append(firmFontHeight).append(",").append(firmFontWidth)
                .append("^FD").append(addressLine).append("^FS\r\n");

        int contactLineY = addressLineY + firmFontHeight + 5;
        zpl.append("^FO").append(generalStartX).append(",").append(contactLineY)
                .append("^A0N,").append(firmFontHeight).append(",").append(firmFontWidth)
                .append("^FD").append(contactLine).append("^FS\r\n");

        zpl.append("^XZ\r\n");

        System.out.println("DEBUG: Generierter ZPL-Code:\n" + zpl.toString());
        return zpl.toString();
    }

    /**
     * Sends a ZPL string directly to the network printer.
     * @param zplCode The ZPL code as a string.
     * @throws IOException If an error occurs during communication with the printer.
     */
    public void sendZplToNetworkPrinter(String zplCode) throws IOException {
        Socket socket = null;
        OutputStream os = null;
        try {
            System.out.println("DEBUG: Starte Sendevorgang für ZPL an Drucker " + PRINTER_IP + ":" + PRINTER_PORT + "...");

            socket = new Socket(PRINTER_IP, PRINTER_PORT);
            System.out.println("DEBUG: Verbindung zum Drucker " + PRINTER_IP + ":" + PRINTER_PORT + " erfolgreich aufgebaut.");

            os = socket.getOutputStream();
            System.out.println("DEBUG: OutputStream geöffnet. Sende " + zplCode.length() + " Bytes ZPL-Code.");

            os.write(zplCode.getBytes(StandardCharsets.US_ASCII));
            System.out.println("DEBUG: ZPL-Code-Bytes in den OS-Netzwerkpuffer geschrieben.");

            os.flush();
            System.out.println("DEBUG: Daten zum OS-Netzwerkpuffer geflushed. Warte auf Übertragung.");

            try {
                System.out.println("DEBUG: Warte 1000ms (1 Sekunde), um eine vollständige Netzwerkübertragung sicherzustellen...");
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                System.err.println("WARN: Thread-Sleep während ZPL-Sendeverzögerung unterbrochen.");
            }
            System.out.println("DEBUG: Verzögerung beendet.");

            System.out.println("INFO: ZPL-Code erfolgreich an Drucker " + PRINTER_IP + " gesendet.");

        } catch (IOException e) {
            System.err.println("FEHLER: Konnte ZPL-Code nicht an den Drucker senden. Netzwerk-/Druckerproblem? Details: " + e.getMessage());
            throw e;
        } finally {
            System.out.println("DEBUG: Schliesse Ressourcen (OutputStream, Socket)...");
            if (os != null) {
                try {
                    os.close();
                    System.out.println("DEBUG: OutputStream geschlossen.");
                } catch (IOException e) {
                    System.err.println("WARN: Fehler beim Schliessen des OutputStream: " + e.getMessage());
                }
            }
            if (socket != null) {
                try {
                    socket.close();
                    System.out.println("DEBUG: Socket geschlossen.");
                } catch (IOException e) {
                    System.err.println("WARN: Fehler beim Schliessen des Sockets: " + e.getMessage());
                }
            }
        }
    }
}