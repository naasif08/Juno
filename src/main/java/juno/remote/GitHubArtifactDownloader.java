package juno.remote;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class GitHubArtifactDownloader {

    /**
     * Downloads a file from a GitHub artifact URL and saves it to the specified output location.
     *
     * @param artifactUrl The direct URL to the artifact (must be a .bin or .zip file)
     * @param outputPath  Path to save the downloaded file
     * @param token       GitHub Personal Access Token (with repo permissions) for private repos
     * @throws IOException if any I/O error occurs
     */
    public static void downloadArtifact(String artifactUrl, Path outputPath, String token) throws IOException {
        System.out.println("⬇️ Downloading artifact from GitHub...");
        URL url = new URL(artifactUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Required for private repos
        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setRequestProperty("Accept", "application/octet-stream");

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            throw new IOException("❌ Failed to download artifact. HTTP response: " + responseCode);
        }

        try (InputStream in = connection.getInputStream();
             OutputStream out = Files.newOutputStream(outputPath)) {
            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }

        System.out.println("✅ Artifact downloaded to: " + outputPath.toAbsolutePath());
    }
}

