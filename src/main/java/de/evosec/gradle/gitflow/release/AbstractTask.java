package de.evosec.gradle.gitflow.release;

import com.atlassian.jgitflow.core.JGitFlow;
import com.atlassian.jgitflow.core.exception.JGitFlowException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

public class AbstractTask extends DefaultTask {

    @Internal
    private ReleasePlugin plugin;
    @Internal
    private JGitFlow flow;

    void setPlugin(ReleasePlugin plugin) {
        this.plugin = plugin;
    }

    public ReleasePlugin getPlugin() {
        return plugin;
    }

    protected JGitFlow ensureGitFlow() throws JGitFlowException {
        if (flow == null) {
            flow = JGitFlow.getOrInit(findWorkTree());
        }
        return flow;
    }

    protected void updateVersionAndCommit(JGitFlow flow, String targetVersion, String versionType) throws GitAPIException {
        String filepattern = findWorkTree()
                .toPath()
                .normalize()
                .relativize(findPropertiesFile())
                .toString();

        updateVersionProperty(targetVersion);
        flow.git().add().addFilepattern(filepattern).call();
        flow.git().commit().setMessage("update version to " + targetVersion + " " + versionType).call();
    }

    private File findWorkTree() {
        FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();

        repositoryBuilder.findGitDir();

        if (repositoryBuilder.getGitDir() == null) {
            throw new IllegalStateException("unable to find git working directory");
        }

        try {
            return repositoryBuilder.build().getWorkTree();
        } catch (IOException e) {
            throw new IllegalStateException("unable to find git working directory", e);
        }
    }

    protected void checkPropertiesFile() {
        Properties properties = new Properties();
        Path path = findPropertiesFile();

        try (InputStream is = Files.newInputStream(path)) {
            properties.load(is);
        } catch (IOException e) {
            throw new GradleException("unable to read " + path.getFileName().toString(), e);
        }

        // set the project version from the properties file if it was not otherwise specified
        if (!isVersionDefined()) {
            getProject().setVersion(properties.getProperty("version"));
        }
    }

    @Internal
    private boolean isVersionDefined() {
        return getProject().getVersion() != null && !"unspecified".equals(getProject().getVersion());
    }

    private Path findPropertiesFile() {
        String versionPropertyFileName = plugin.getExtension().getVersionPropertyFile();
        File propertiesFile = getProject().file(versionPropertyFileName);
        if (!propertiesFile.isFile()) {
            throw new GradleException("version property file [" + versionPropertyFileName + "] not found, please create it manually and and specify the version property.");
        }
        return propertiesFile.getAbsoluteFile().toPath();
    }

    protected void updateVersionProperty(String newVersion) {
        String oldVersion = "${project.version}";
        if (!Objects.equals(oldVersion, newVersion)) {
            getProject().setVersion(newVersion);
            getProject().subprojects(subProject -> subProject.setVersion(newVersion));
            Path propFile = findPropertiesFile();
            writeVersion(propFile, "version", newVersion);
        }
    }

    private void writeVersion(Path path, String key, String version) {
        try {
            // we use replace here as other ant tasks escape and modify the whole file

            String propertiesFile = new String(Files.readAllBytes(path), UTF_8);

            Pattern pattern = Pattern.compile("^(" + key + "\\s*=\\s*).+$", Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(propertiesFile);
            if (matcher.find()) {
                propertiesFile = matcher.replaceAll("$1" + version);
            } else {
                throw new GradleException("could not write version");
            }

            Files.write(path, propertiesFile.getBytes(UTF_8));

        } catch (IOException e) {
            throw new GradleException("unable to update " + path.getFileName().toString(), e);
        }
    }

}
