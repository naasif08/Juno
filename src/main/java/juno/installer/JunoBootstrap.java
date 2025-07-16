package juno.installer;

import juno.detector.JunoDetector;
import juno.logger.JunoLogger;

import java.io.IOException;

public class JunoBootstrap {
    public static void runFirstTimeSetupLocal() throws IOException {
        if (!EspIdfInstaller.isInstalled()) {
            JunoLogger.info("Setting up Environment for Juno...");
            EspIdfInstaller.downloadAndInstall(); // handles ZIP + extract
        } else {
            JunoLogger.success("Already Juno Environment installed...");
        }

        try {
            PythonInstaller.ensureMinicondaInstalled(); // handles ZIP + extract
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (!GitInstaller.isGitPresent()) {
            JunoLogger.info("Installing Portable Git...");
            try {
                GitInstaller.ensureGitInstalled();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        } else {
            JunoLogger.success("✅ Portable Git is already installed");
        }


        JunoLogger.info("Installing ESP dependencies.");
        try {
            ESPInstaller.runInstallScript();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        JunoLogger.success("All Dependencies are installed successfully!");
        JunoDetector.printDetectedPaths();
    }

}
