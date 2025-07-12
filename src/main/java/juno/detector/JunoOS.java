package juno.detector;

public enum JunoOS {
    WINDOWS,
    LINUX,
    MACOS,
    UNKNOWN;

    public static JunoOS detect() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) return WINDOWS;
        if (osName.contains("nux") || osName.contains("nix")) return LINUX;
        if (osName.contains("mac")) return MACOS;
        return UNKNOWN;
    }

    // ✅ Add this method here:
    public boolean isWindows() {
        return this == WINDOWS;
    }

    public boolean isUnixLike() {
        return this == LINUX || this == MACOS;
    }
}
