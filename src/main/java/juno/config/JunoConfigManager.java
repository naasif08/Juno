package juno.config;

import juno.detector.JunoDetector;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class JunoConfigManager {
    private static final String CONFIG_FILE_NAME = "juno.properties";

    private final Path configFilePath;
    private Properties properties;

    public JunoConfigManager(Path junoRoot) throws IOException {
        this.configFilePath = junoRoot.resolve(CONFIG_FILE_NAME);
        load();
    }

    private void load() throws IOException {
        properties = new Properties();
        if (Files.exists(configFilePath)) {
            try (InputStream in = Files.newInputStream(configFilePath)) {
                properties.load(in);
            }
        }
    }

    public void save() throws IOException {
        try (OutputStream out = Files.newOutputStream(configFilePath)) {
            properties.store(out, "JUNO Configurations");
        }
    }

    public boolean isLocalBuild() {
        return Boolean.parseBoolean(properties.getProperty("localBuild", "true"));
    }

    public void setLocalBuild(boolean localBuild) {
        properties.setProperty("localBuild", Boolean.toString(localBuild));
    }

    public String getProjectName() {
        return properties.getProperty("projectName", "ESP32Project");
    }

    public void setProjectName(String projectName) {
        properties.setProperty("projectName", projectName);
    }

    public String getComPort() {
        if (JunoDetector.detectEsp32Port() != null) {
            return JunoDetector.detectEsp32Port();
        }
        return properties.getProperty("comPort");  // default Windows COM port
    }

    public void setComPort(String comPort) {
        properties.setProperty("comPort", comPort);
    }

    // Add more config getters/setters as needed
}
