package de.evosec.gradle.gitflow.release;

import org.eclipse.jgit.transport.CredentialsProvider;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class ReleasePlugin implements Plugin<Project> {

    private ReleaseExtension extension;

    @Override
    public void apply(Project project) {
        extension = project.getExtensions().create("release", ReleaseExtension.class, project);
        project.afterEvaluate(extension::afterEvaluate);

        CredentialsProvider.setDefault(new GitCredentialsProvider(extension));

        project.getTasks().create("releaseStart", StartReleaseTask.class, task -> task.setPlugin(ReleasePlugin.this));
        project.getTasks().create("releaseFinish", ReleaseFinishTask.class, task -> task.setPlugin(ReleasePlugin.this));
    }

    ReleaseExtension getExtension() {
        return extension;
    }
}
