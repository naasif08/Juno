package juno.detector;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Properties;

public final class JunoPaths {

    private static boolean initialized = false;

    // Public static paths
    public static String idfPath;
    public static String pythonPath;
    public static String pythonExecutablePath;
    public static String toolchainPath;
    public static String serialPort;
    public static String gitPath;
    public static String xtensaGdbPath;
    public static String xtensaToolchainPath;
    public static String espClangPath;
    public static String cMakePath;
    public static String openOcdBin;
    public static String ninjaPath;
    public static String idfPyPath;
    public static String cCacheBinPath;
    public static String dfuUtilBinPath;
    public static String openOcdScriptsPath;

    private static File dotJunoDir;

    public static void init() {
        if (initialized) return;

        dotJunoDir = new File(System.getProperty("user.dir"), ".juno");
        if (!dotJunoDir.exists()) dotJunoDir.mkdirs();

        ensureJunoPropertiesTemplate();

        idfPath = JunoDetector.detectIdfPath();
        pythonPath = JunoDetector.detectPythonPath();
        pythonExecutablePath = JunoDetector.detectPythonExecutable();
        toolchainPath = JunoDetector.detectToolchainBin();
        serialPort = JunoDetector.detectEsp32Port();
        gitPath = JunoDetector.detectEspressifGitPath();
        xtensaGdbPath = JunoDetector.detectXtensaGdbPath();
        xtensaToolchainPath = JunoDetector.detectXtensaToolchainPath();
        cMakePath = JunoDetector.detectCmakePath();
        openOcdBin = JunoDetector.detectOpenOcdBin();
        ninjaPath = JunoDetector.detectNinjaPath();
        idfPyPath = JunoDetector.detectIdfPyPath();
        cCacheBinPath = JunoDetector.detectCcacheBin();
        dfuUtilBinPath = JunoDetector.detectDfuUtilBin();
        openOcdScriptsPath = JunoDetector.detectOpenOcdScriptsPath();

        loadPropertiesOverrides();
        validatePaths();

        initialized = true;
    }

    public static boolean isInitialized() {
        return initialized;
    }

    public static File getDotJunoDir() {
        if (dotJunoDir == null) {
            dotJunoDir = new File(System.getProperty("user.dir"), ".juno");
            if (!dotJunoDir.exists()) {
                boolean created = dotJunoDir.mkdirs();
                if (!created) {
                    throw new RuntimeException("❌ Failed to create/access .juno directory: " + dotJunoDir.getAbsolutePath());
                }
            }
        }
        return dotJunoDir;
    }

    public static File getProjectDir(String projectName) {
        Path projectDir = getDotJunoDir().toPath().resolve(projectName);
        try {
            Files.createDirectories(projectDir);
        } catch (IOException e) {
            throw new RuntimeException("❌ Failed to create/access project directory: " + projectDir, e);
        }
        return projectDir.toFile();
    }

    private static void ensureJunoPropertiesTemplate() {
        Path propPath = getDotJunoDir().toPath().resolve("juno.properties");
        if (Files.exists(propPath)) return;

        String template = """
            # Juno Properties Template
            # Fill manually if auto-detection fails

            juno.idfPath=
            juno.idfPyPath=
            juno.pythonPath=
            juno.pythonExecutablePath=
            juno.toolchainPath=
            juno.cMakePath=
            juno.ninjaPath=
            juno.serialPort=

            juno.gitPath=
            juno.xtensaGdbPath=
            juno.xtensaToolchainPath=
            juno.espClangPath=
            juno.openOcdBin=
            juno.cCacheBinPath=
            juno.dfuUtilBinPath=
            juno.openOcdScriptsPath=
            """;

        try {
            Files.writeString(propPath, template);
            System.out.println("📝 Created .juno/juno.properties template.");
        } catch (IOException e) {
            System.err.println("❌ Failed to create juno.properties: " + e.getMessage());
        }
    }

    private static void loadPropertiesOverrides() {
        Path propPath = getDotJunoDir().toPath().resolve("juno.properties");
        if (!Files.exists(propPath)) return;

        try (InputStream in = Files.newInputStream(propPath)) {
            Properties props = new Properties();
            props.load(in);
            System.out.println("✅ Loaded manual overrides from juno.properties");

            idfPath = resolve(props, "juno.idfPath", idfPath);
            pythonPath = resolve(props, "juno.pythonPath", pythonPath);
            pythonExecutablePath = resolve(props, "juno.pythonExecutablePath", pythonExecutablePath);
            toolchainPath = resolve(props, "juno.toolchainPath", toolchainPath);
            serialPort = resolve(props, "juno.serialPort", serialPort);
            gitPath = resolve(props, "juno.gitPath", gitPath);
            xtensaGdbPath = resolve(props, "juno.xtensaGdbPath", xtensaGdbPath);
            xtensaToolchainPath = resolve(props, "juno.xtensaToolchainPath", xtensaToolchainPath);
            espClangPath = resolve(props, "juno.espClangPath", espClangPath);
            cMakePath = resolve(props, "juno.cMakePath", cMakePath);
            openOcdBin = resolve(props, "juno.openOcdBin", openOcdBin);
            ninjaPath = resolve(props, "juno.ninjaPath", ninjaPath);
            idfPyPath = resolve(props, "juno.idfPyPath", idfPyPath);
            cCacheBinPath = resolve(props, "juno.cCacheBinPath", cCacheBinPath);
            dfuUtilBinPath = resolve(props, "juno.dfuUtilBinPath", dfuUtilBinPath);
            openOcdScriptsPath = resolve(props, "juno.openOcdScriptsPath", openOcdScriptsPath);

        } catch (IOException e) {
            System.err.println("⚠️ Failed to read juno.properties: " + e.getMessage());
        }
    }

    private static String resolve(Properties props, String key, String fallback) {
        String value = props.getProperty(key, fallback);
        return (value == null || value.equalsIgnoreCase("null")) ? fallback : value;
    }

    private static void validatePaths() {
        check("idfPath", idfPath);
        check("idfPyPath", idfPyPath);
        check("pythonPath", pythonPath);
        check("pythonExecutablePath", pythonExecutablePath);
        check("toolchainPath", toolchainPath);
        check("cMakePath", cMakePath);
        check("ninjaPath", ninjaPath);
        check("serialPort", serialPort);
    }

    private static void check(String name, String value) {
        if (value == null || value.trim().isEmpty() || value.equals("null")) {
            System.err.println("❌ Missing required path: juno." + name);
            System.err.println("→ Fix it in .juno/juno.properties or set it manually.");
            System.exit(1);
        }
    }

    private JunoPaths() {} // prevent instantiation
}
