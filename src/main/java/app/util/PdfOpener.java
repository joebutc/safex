package app.util;

import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class PdfOpener {
    public static void openResourcePdf(String resourceName) {
        try (InputStream in = Objects.requireNonNull(PdfOpener.class.getResourceAsStream("/" + resourceName), "Resource not found: " + resourceName)) {
            Path temp = Files.createTempFile(resourceName, ".pdf");
            Files.copy(in, temp, StandardCopyOption.REPLACE_EXISTING);
            temp.toFile().deleteOnExit();
            openFile(temp);
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    private static void openFile(Path path) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(path.toFile());
            } else {
                System.err.println("Desktop API is not supported on this platform.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
