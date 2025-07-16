package juno.builder.impl;

import juno.builder.JunoBuilder;
import juno.detector.JunoDetector;
import juno.detector.JunoPaths;
import juno.logger.JunoLogger;
import juno.probuilder.JunoBatchBuilder;
import juno.probuilder.JunoProjectCreator;
import juno.remote.GitHubArtifactDownloader;
import juno.remote.GitHubUploader;
import juno.remote.GitHubWorkflowRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class RemoteBuilder implements JunoBuilder {


    private final String githubRepoUrl;
    private final String githubAccessToken;
    private File projectDir;

    public RemoteBuilder(String githubRepoUrl, String githubAccessToken) {
        this.githubRepoUrl = githubRepoUrl;
        this.githubAccessToken = githubAccessToken;
    }

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
        try {
            Path firmwareDir = JunoPaths.getDotJunoDir().toPath().resolve("firmware");
            Files.createDirectories(firmwareDir);

            JunoLogger.info("🚀 Uploading project to GitHub...");

            // 1. Push projectDir to a temp branch
            GitHubUploader uploader = new GitHubUploader(githubRepoUrl, githubAccessToken);
            String commitSHA = uploader.pushProject(projectDir);

            // 2. Trigger the GitHub Actions build
            GitHubWorkflowRunner runner = new GitHubWorkflowRunner(githubRepoUrl, githubAccessToken);
            String artifactUrl = String.valueOf(runner.waitForFirmwareArtifact(Path.of(commitSHA)));

            // 3. Download firmware.bin to .juno/firmware/
            Path outputFile = firmwareDir.resolve("firmware.bin");
            GitHubArtifactDownloader.downloadArtifact(artifactUrl, outputFile, githubAccessToken);

            JunoLogger.success("Firmware downloaded to: " + outputFile.toAbsolutePath());

        } catch (Exception e) {
            throw new RuntimeException("❌ Remote flashing failed.", e);
        }
    }


    @Override
    public void clean() {

    }

    @Override
    public void setOption(String key, String value) {

    }
}
