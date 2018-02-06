package de.evosec.gradle.gitflow.release;

import org.gradle.api.Project;

import java.util.Map;

public class ReleaseExtension {

    private String username = null;
    private String password = null;

    private int androidVersionCodeOffset = 0;

    private boolean failOnSnapshotDependencies = true;
    private boolean pushAfterReleaseFinish = false;
    private boolean incrementMinorVersion = false;
    private boolean allowUntracked = false;

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
        if (project.hasProperty("release.allowUntracked")) {
            setAllowUntracked(Boolean.valueOf(properties.get("release.allowUntracked").toString()));
        }
        if (project.hasProperty("release.incrementMinorVersion")) {
            setIncrementMinorVersion(Boolean.valueOf(properties.get("release.incrementMinorVersion").toString()));
        }
        if (project.hasProperty("release.pushAfterReleaseFinish")) {
            setPushAfterReleaseFinish(Boolean.valueOf(properties.get("release.pushAfterReleaseFinish").toString()));
        }
        if (project.hasProperty("release.failOnSnapshotDependencies")) {
            setFailOnSnapshotDependencies(Boolean.valueOf(properties.get("release.failOnSnapshotDependencies").toString()));
        }
        if (project.hasProperty("release.androidVersionCodeOffset")) {
            setAndroidVersionCodeOffset(Integer.parseInt(properties.get("release.androidVersionCodeOffset").toString()));
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

    public int getAndroidVersionCodeOffset() {
        return androidVersionCodeOffset;
    }

    public void setAndroidVersionCodeOffset(int androidVersionCodeOffset) {
        this.androidVersionCodeOffset = androidVersionCodeOffset;
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

    public boolean isIncrementMinorVersion() {
        return incrementMinorVersion;
    }

    public void setIncrementMinorVersion(boolean incrementMinorVersion) {
        this.incrementMinorVersion = incrementMinorVersion;
    }

    public boolean isAllowUntracked() {
        return allowUntracked;
    }

    public void setAllowUntracked(boolean allowUntracked) {
        this.allowUntracked = allowUntracked;
    }

    public String getVersionPropertyFile() {
        return versionPropertyFile;
    }

    public void setVersionPropertyFile(String versionPropertyFile) {
        this.versionPropertyFile = versionPropertyFile;
    }
}
