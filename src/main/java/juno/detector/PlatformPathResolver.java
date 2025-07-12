package juno.detector;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PlatformPathResolver {

    public enum OSType {
        WINDOWS, MAC, LINUX, UNKNOWN
    }

    public static OSType getOSType() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) return OSType.WINDOWS;
        if (os.contains("mac")) return OSType.MAC;
        if (os.contains("nix") || os.contains("nux") || os.contains("aix")) return OSType.LINUX;
        return OSType.UNKNOWN;
    }

    public static Path getJunoFolder() {
        OSType os = getOSType();
        switch (os) {
            case WINDOWS:
                return Paths.get("C:", "Juno");
            case MAC:
            case LINUX:
                return Paths.get(System.getProperty("user.home"), "Juno");
            default:
                throw new UnsupportedOperationException("Unsupported OS for Juno setup.");
        }
    }

    public static Path getIDFPath() {
        return getJunoFolder().resolve("esp-idf-v5.4.2");
    }

    public static Path getToolsPath() {
        return getIDFPath().resolve("tools");
    }

    public static Path getEmbeddedPythonExe() {
        OSType os = getOSType();
        Path pythonDir = getIDFPath().resolve("python");
        if (os == OSType.WINDOWS) {
            return pythonDir.resolve("python.exe");
        } else {
            return pythonDir.resolve("bin").resolve("python3");
        }
    }
}

