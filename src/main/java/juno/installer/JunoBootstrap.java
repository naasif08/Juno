package juno.installer;

import juno.detector.JunoDetector;

import java.io.IOException;

public class JunoBootstrap {
    public static void runFirstTimeSetupLocal() {
        if (!EspIdfInstaller.isInstalled()) {
            System.out.println("⚙️ First-time setup: installing ESP-IDF...");
            EspIdfInstaller.downloadAndInstall(); // handles ZIP + extract
        } else {
            System.out.println("✅ ESP-IDF already installed.");
        }

        try {
            PythonInstaller.ensureMinicondaInstalled(); // handles ZIP + extract
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (!GitInstaller.isGitPresent()) {
            System.out.println("Installing Portable Git...");
            try {
                GitInstaller.ensureGitInstalled();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } else {
            System.out.println("✅ Portable Git is already installed");
        }


        System.out.println("⚙️ Installing ESP dependencies.");
        try {
            ESPInstaller.runInstallScript();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("✅ All Dependencies are installed successfully!");
        JunoDetector.printDetectedPaths();
    }

}
