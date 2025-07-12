package juno.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;

public class FileDownloader {

    public static void downloadFile(String urlString, Path destination) throws IOException {
        URL url = new URL(urlString);

        // Ensure parent directories exist
        Files.createDirectories(destination.getParent());

        // Open connection to get content length
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("HEAD");
        int responseCode = connection.getResponseCode();

        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Failed to get file info. Server returned HTTP " + responseCode);
        }

        long expectedSize = connection.getContentLengthLong();
        if (expectedSize <= 0) {
            System.out.println("Warning: Unable to determine expected file size.");
        } else {
            System.out.println("Expected file size: " + (expectedSize / (1024 * 1024)) + " MB");
        }
        connection.disconnect();

        // Download to temp file first
        Path tempFile = destination.resolveSibling(destination.getFileName() + ".part");

        try (InputStream in = url.openStream();
             OutputStream out = Files.newOutputStream(tempFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalRead = 0;
            long lastPrintTime = System.currentTimeMillis();

            System.out.println("⬇️ Downloading: " + urlString);

            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                totalRead += bytesRead;

                // Print progress every 500ms if expectedSize known
                if (expectedSize > 0 && System.currentTimeMillis() - lastPrintTime > 500) {
                    int percent = (int) ((totalRead * 100) / expectedSize);
                    String bar = buildProgressBar(percent);
                    System.out.print("\r" + bar + " " + percent + "%");
                    lastPrintTime = System.currentTimeMillis();
                }
            }
        }

        // Check downloaded file size
        long actualSize = Files.size(tempFile);
        if (expectedSize > 0 && actualSize != expectedSize) {
            Files.delete(tempFile);
            throw new IOException("Download failed: incomplete file. Expected " + expectedSize + " bytes, got " + actualSize + " bytes.");
        }

        // Rename temp file to final destination
        Files.move(tempFile, destination, StandardCopyOption.REPLACE_EXISTING);
        if (expectedSize > 0) {
            System.out.print("\r" + buildProgressBar(100) + " 100%\n");
        }
        System.out.println("\n✅ Download complete: " + destination);
    }

    private static String buildProgressBar(int percent) {
        int totalBars = 40;
        int filledBars = (percent * totalBars) / 100;
        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < filledBars; i++) bar.append("▇");
        for (int i = filledBars; i < totalBars; i++) bar.append("-");
        bar.append("]");
        return bar.toString();
    }
}
