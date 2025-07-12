package juno.installer;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.Locale;

public class GitInstaller {

    // ESP-IDF base folder
    private static final Path ESP_IDF_PATH = getEspIdfPath();

    // Downloaded archive saved here
    private static final Path DOWNLOAD_FILE = ESP_IDF_PATH.resolve(getDownloadFilename());

    // On Windows, Git executable is at esp-idf-v5.4.2/cmd/git.exe
    // On Unix, at esp-idf-v5.4.2/bin/git
    private static final Path GIT_EXECUTABLE_PATH = getGitExecutablePath();

    public static void ensureGitInstalled() throws IOException, InterruptedException {
        if (isGitPresent()) {
            System.out.println("✅ Portable Git already installed at: " + GIT_EXECUTABLE_PATH);
            return;
        }

        System.out.println("⬇️  Installing Portable Git for OS: " + detectOS());

        if (Files.exists(DOWNLOAD_FILE)) {
            System.out.println("⚠️  Previous archive found. Deleting...");
            Files.delete(DOWNLOAD_FILE);
        }

        // Download archive
        String downloadUrl = getDownloadUrl();
        System.out.println("🌐 Downloading from: " + downloadUrl);
        downloadFile(downloadUrl, DOWNLOAD_FILE);

        // Clean old Git folder (cmd or bin)
        Path oldGitFolder = detectOS().equals("windows")
                ? ESP_IDF_PATH.resolve("cmd")
                : ESP_IDF_PATH.resolve("bin");

        if (Files.exists(oldGitFolder)) {
            System.out.println("🧹 Cleaning old Git folder: " + oldGitFolder);
            deleteRecursively(oldGitFolder);
        }

        // Extract archive
        if (isWindows()) {
            System.out.println("📦 Extracting Windows Git (.7z.exe)...");
            extract7zExe(DOWNLOAD_FILE, ESP_IDF_PATH);
        } else {
            System.out.println("📦 Extracting Unix Git (.tar.xz)...");
            extractTarXZ(DOWNLOAD_FILE, ESP_IDF_PATH);
        }

        Files.deleteIfExists(DOWNLOAD_FILE);

        System.out.println("✅ Portable Git ready at: " + GIT_EXECUTABLE_PATH);
    }

    public static boolean isGitPresent() {
        if (GIT_EXECUTABLE_PATH == null || !Files.exists(GIT_EXECUTABLE_PATH)) {
            System.out.println("Git executable not found on disk.");
            return false;
        }

        try {
            ProcessBuilder pb = new ProcessBuilder(GIT_EXECUTABLE_PATH.toAbsolutePath().toString(), "--version");
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                System.out.println("Git --version output: " + line);
                if (line != null && line.toLowerCase().contains("git version")) {
                    System.out.println("Git version detected successfully.");
                    return true;
                } else {
                    System.out.println("Git --version output did NOT contain expected text.");
                }
            }
            process.waitFor();
        } catch (Exception e) {
            System.out.println("Exception when running git --version:");
            e.printStackTrace();
            return false;
        }

        System.out.println("Git executable found but did not behave as expected.");
        return false;
    }

    public static Path getGitExecutablePath() {
        if (isWindows()) {
            return ESP_IDF_PATH.resolve("cmd").resolve("git.exe");
        } else {
            return ESP_IDF_PATH.resolve("bin").resolve("git");
        }
    }

    // ----------------- OS & Paths -----------------

    private static boolean isWindows() {
        return detectOS().equals("windows");
    }

    private static String detectOS() {
        String os = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        if (os.contains("win")) return "windows";
        if (os.contains("mac")) return "macos";
        if (os.contains("nix") || os.contains("nux") || os.contains("aix")) return "linux";
        return "unknown";
    }

    private static Path getEspIdfPath() {
        if (detectOS().equals("windows")) {
            return Paths.get("C:", "Juno", "esp-idf-v5.4.2");
        } else {
            return Paths.get(System.getProperty("user.home"), "Juno", "esp-idf-v5.4.2");
        }
    }

    // ----------------- Download Info -----------------

    private static String getDownloadFilename() {
        return isWindows()
                ? "PortableGit-2.42.0-64-bit.7z.exe"
                : "git-2.42.0.tar.xz";
    }

    private static String getDownloadUrl() {
        return isWindows()
                ? "https://github.com/git-for-windows/git/releases/download/v2.42.0.windows.1/PortableGit-2.42.0-64-bit.7z.exe"
                : "https://mirrors.edge.kernel.org/pub/software/scm/git/git-2.42.0.tar.xz";
    }

    // ----------------- Download -----------------

    private static void downloadFile(String urlStr, Path output) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        int contentLength = conn.getContentLength();

        try (InputStream in = conn.getInputStream();
             OutputStream out = Files.newOutputStream(output)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalRead = 0;

            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                totalRead += bytesRead;
                if (contentLength > 0) {
                    int progress = (int) ((totalRead * 100) / contentLength);
                    System.out.print("\rDownloading: " + progress + "%");
                }
            }
            System.out.println("\r✅ Download complete.");
        }
    }

    // ----------------- Extract -----------------

    private static void extract7zExe(Path exePath, Path outputDir) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                exePath.toAbsolutePath().toString(),
                "-y",
                "-o" + outputDir.toAbsolutePath().toString()
        );
        pb.inheritIO();
        Process p = pb.start();
        int code = p.waitFor();
        if (code != 0) throw new IOException("❌ 7z.exe extraction failed with code " + code);
    }

    private static void extractTarXZ(Path tarFile, Path outputDir) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                "tar", "-xf",
                tarFile.toAbsolutePath().toString(),
                "-C",
                outputDir.toAbsolutePath().toString()
        );
        pb.inheritIO();
        Process p = pb.start();
        int code = p.waitFor();
        if (code != 0) throw new IOException("❌ tar extraction failed with code " + code);
    }

    private static void deleteRecursively(Path path) throws IOException {
        if (!Files.exists(path)) return;
        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> entries = Files.newDirectoryStream(path)) {
                for (Path entry : entries) {
                    deleteRecursively(entry);
                }
            }
        }
        Files.delete(path);
    }
}
