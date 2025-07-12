package juno.installer;

import juno.detector.PlatformPathResolver;
import juno.utils.FileDownloader;
import juno.utils.ZipExtractor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class EspIdfInstaller {

    public static boolean isInstalled() {
        Path idfPath = PlatformPathResolver.getIDFPath();
        return Files.exists(idfPath) && Files.exists(idfPath.resolve("install.bat")); // Windows check
    }

    public static void downloadAndInstall() {
        String url = "https://github.com/naasif08/JunoProject/releases/download/idf-v5.4.2/esp-idf-v5.4.2.zip";
        Path zipPath = PlatformPathResolver.getJunoFolder().resolve("esp-idf.zip");
        Path targetDir = PlatformPathResolver.getIDFPath();

        try {
            System.out.println("📥 Downloading ESP-IDF...");
            FileDownloader.downloadFile(url, zipPath);

            System.out.println("📦 Extracting...");
            ZipExtractor.extract(zipPath, targetDir.getParent()); // extract into /Juno/

            Files.delete(zipPath); // optional cleanup
            System.out.println("✅ ESP-IDF installed at: " + targetDir);
        } catch (IOException e) {
            throw new RuntimeException("❌ Installation failed: " + e.getMessage());
        }
    }
}
