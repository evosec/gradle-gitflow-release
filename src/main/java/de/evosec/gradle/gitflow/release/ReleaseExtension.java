package de.evosec.gradle.gitflow.release;

import org.gradle.api.Project;

import java.util.Map;

public class ReleaseExtension {

    private String username = null;
    private String password = null;

    private boolean failOnSnapshotDependencies = true;
    private boolean pushAfterReleaseFinish = false;

    private String versionPropertyFile = "gradle.properties";

    public ReleaseExtension(Project project) {
        // required by gradle
    }

    public void afterEvaluate(Project project) {
        Map<String, ?> properties = project.getProperties();
        if (project.hasProperty("release.username")) {
            setUsername(properties.get("release.username").toString());
        }
        if (project.hasProperty("release.password")) {
            setPassword(properties.get("release.password").toString());
        }
        if (project.hasProperty("release.versionPropertyFile")) {
            setVersionPropertyFile(properties.get("release.versionPropertyFile").toString());
        }
        if (project.hasProperty("release.pushAfterReleaseFinish")) {
            setPushAfterReleaseFinish(Boolean.valueOf(properties.get("release.pushAfterReleaseFinish").toString()));
        }
        if (project.hasProperty("release.failOnSnapshotDependencies")) {
            setFailOnSnapshotDependencies(Boolean.valueOf(properties.get("release.failOnSnapshotDependencies").toString()));
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isFailOnSnapshotDependencies() {
        return failOnSnapshotDependencies;
    }

    public void setFailOnSnapshotDependencies(boolean failOnSnapshotDependencies) {
        this.failOnSnapshotDependencies = failOnSnapshotDependencies;
    }

    public boolean isPushAfterReleaseFinish() {
        return pushAfterReleaseFinish;
    }

    public void setPushAfterReleaseFinish(boolean pushAfterReleaseFinish) {
        this.pushAfterReleaseFinish = pushAfterReleaseFinish;
    }

    public String getVersionPropertyFile() {
        return versionPropertyFile;
    }

    public void setVersionPropertyFile(String versionPropertyFile) {
        this.versionPropertyFile = versionPropertyFile;
    }
}
