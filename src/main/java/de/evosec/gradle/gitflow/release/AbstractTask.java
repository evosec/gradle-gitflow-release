package de.evosec.gradle.gitflow.release;

import com.atlassian.jgitflow.core.JGitFlow;
import com.atlassian.jgitflow.core.exception.JGitFlowException;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
            flow = JGitFlow.getOrInit(new File("."));
        }
        return flow;
    }

    protected void checkPropertiesFile() {
        File propertiesFile = findPropertiesFile();

        if (!propertiesFile.canRead() || !propertiesFile.canWrite()) {
            throw new GradleException("Unable to update version property. Please check file permissions.");
        }

        Properties properties = new Properties();
        Path path = Paths.get(propertiesFile.getAbsolutePath());
        try (InputStream is = Files.newInputStream(path)) {
            properties.load(is);
        } catch (IOException e) {
            throw new GradleException("unable to read " + propertiesFile.getName(), e);
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

    private File findPropertiesFile() {
        String versionPropertyFileName = plugin.getExtension().getVersionPropertyFile();
        File propertiesFile = getProject().file(versionPropertyFileName);
        if (!propertiesFile.isFile()) {
            throw new GradleException("version property file [" + versionPropertyFileName + "] not found, please create it manually and and specify the version property.");
        }
        return propertiesFile;
    }


    protected void updateVersionProperty(String newVersion) {
        String oldVersion = "${project.version}";
        if (!Objects.equals(oldVersion, newVersion)) {
            getProject().setVersion(newVersion);
            getProject().subprojects(subProject -> subProject.setVersion(newVersion));
            File propFile = findPropertiesFile();
            writeVersion(propFile, "version", newVersion);
        }
    }

    @Internal
    protected String getPropertiesFileName() {
        File file = findPropertiesFile();
        File directory = new File(".");

        int pathLen = directory.getAbsolutePath().length();

        return file.getAbsolutePath().substring(pathLen - 1);
    }

    private void writeVersion(File file, String key, String version) {
        try {
            // we use replace here as other ant tasks escape and modify the whole file

            Path path = Paths.get(file.getAbsolutePath());
            String propertesFile = new String(Files.readAllBytes(path), UTF_8);

            Pattern pattern = Pattern.compile("^(" + key + "\\s*=\\s*).+$", Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(propertesFile);
            if (matcher.find()) {
                propertesFile = matcher.replaceAll("$1" + version);
            } else {
                throw new GradleException("could not write version");
            }

            Files.write(path, propertesFile.getBytes(UTF_8));

        } catch (IOException e) {
            throw new GradleException("unable to update " + file.getName(), e);
        }
    }

}
