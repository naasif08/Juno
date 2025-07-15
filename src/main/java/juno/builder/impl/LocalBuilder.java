package juno.builder.impl;

import juno.builder.JunoBuilder;
import juno.detector.JunoDetector;
import juno.detector.JunoPaths;
import juno.flasher.JunoFlasher;
import juno.probuilder.JunoBatchBuilder;
import juno.probuilder.JunoProjectCreator;

import java.io.File;
import java.io.IOException;

public class LocalBuilder implements JunoBuilder {

    private File projectDir;

    @Override
    public void buildJuno() {
        if (!JunoPaths.isInitialized()) {
            JunoPaths.init();
        }
        try {
            this.projectDir = JunoProjectCreator.createProject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        JunoBatchBuilder junoBatchBuilder = new JunoBatchBuilder();
        try {
            junoBatchBuilder.writeBuildScripts(projectDir, JunoDetector.detectEsp32Port());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void flashFirmware() {
        JunoFlasher junoFlasher = new JunoFlasher();
        try {
            junoFlasher.flashProject(projectDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void clean() {

    }

    @Override
    public void setOption(String key, String value) {

    }
}
