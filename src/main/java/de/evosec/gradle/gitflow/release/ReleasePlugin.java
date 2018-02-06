package de.evosec.gradle.gitflow.release;

import com.github.zafarkhaja.semver.Version;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtraPropertiesExtension;

public class ReleasePlugin implements Plugin<Project> {

    private ReleaseExtension extension;

    @Override
    public void apply(Project project) {
        extension = project.getExtensions().create("release", ReleaseExtension.class, project);
        project.afterEvaluate(extension::afterEvaluate);

        CredentialsProvider.setDefault(new GitCredentialsProvider(extension));

        project.allprojects(this::setAndroidVersion);

        project.getTasks().create("releaseStart", ReleaseStartTask.class, task -> task.setPlugin(ReleasePlugin.this));
        project.getTasks().create("releaseFinish", ReleaseFinishTask.class, task -> task.setPlugin(ReleasePlugin.this));
    }

    ReleaseExtension getExtension() {
        return extension;
    }

    private void setAndroidVersion(Project project) {
        Object extObj = project.getExtensions().findByName("ext");
        if (!(extObj instanceof ExtraPropertiesExtension)) {
            return;
        }

        Version version = Version.valueOf(project.getVersion().toString());

        int versionCode = extension.getAndroidVersionCodeOffset();
        versionCode += version.getPatchVersion();
        versionCode += version.getMinorVersion() * 1000;
        versionCode += version.getMajorVersion() * 1000000;

        ExtraPropertiesExtension ext = (ExtraPropertiesExtension) extObj;
        ext.set("releaseAndroidVersionCode", versionCode);
        ext.set("releaseAndroidVersionName", project.getVersion().toString());
    }
}
