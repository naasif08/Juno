package juno.installer;

import juno.detector.JunoDetector;
import juno.detector.JunoOS;
import juno.logger.JunoLogger;
import juno.utils.FileDownloader;
import juno.utils.ZipExtractor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class EspIdfInstaller {

    public static boolean isInstalled() {
        if (JunoDetector.detectIdfPath() != null) {
            Path idfPath = Path.of(JunoDetector.detectIdfPath());
            return Files.exists(idfPath) && Files.exists(idfPath.resolve("install.bat")); // Windows check
        }
        return false;
    }

    public static void downloadAndInstall() {
        String url = "https://github.com/naasif08/JunoProject/releases/download/idf-v5.4.2/esp-idf-v5.4.2.zip";
        Path zipPath = getJunoFolder().resolve("esp-idf.zip");
        Path targetDir = zipPath.getParent();

        try {
            JunoLogger.info("Downloading ESP-IDF...");
            FileDownloader.downloadWithResume(url, zipPath);

            JunoLogger.info("Extracting...");
            ZipExtractor.extract(zipPath, targetDir); // extract into /Juno/

            Files.delete(zipPath); // optional cleanup
            JunoLogger.success("ESP-IDF installed at: " + targetDir);
        } catch (IOException e) {
            throw new RuntimeException("❌ Installation failed: " + e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static Path getJunoFolder() {
        JunoOS os = getOSType();
        switch (os) {
            case WINDOWS:
                return Paths.get("C:", "Juno");
            case MACOS:
            case LINUX:
                return Paths.get(System.getProperty("user.home"), "Juno");
            default:
                throw new UnsupportedOperationException("Unsupported OS for Juno setup.");
        }
    }

    public static JunoOS getOSType() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) return JunoOS.WINDOWS;
        if (os.contains("mac")) return JunoOS.MACOS;
        if (os.contains("nix") || os.contains("nux") || os.contains("aix")) return JunoOS.LINUX;
        return JunoOS.UNKNOWN;
    }
}
