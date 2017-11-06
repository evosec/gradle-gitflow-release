package de.evosec.gradle.gitflow.release;

import com.atlassian.jgitflow.core.JGitFlow;
import com.atlassian.jgitflow.core.exception.JGitFlowException;
import com.github.zafarkhaja.semver.Version;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

public class ReleaseFinishTask extends AbstractTask {

    @TaskAction
    public void releaseFinish() throws JGitFlowException, GitAPIException {
        JGitFlow flow = ensureGitFlow();

        Version version = Version.valueOf(getProject().getVersion().toString());

        flow.releaseFinish(version.getNormalVersion())
                .setAllowUntracked(getPlugin().getExtension().isAllowUntracked())
                .call();

        delay();

        if (getPlugin().getExtension().isIncrementMinorVersion()) {
            version = version.incrementMinorVersion("SNAPSHOT");
        } else {
            version = version.incrementPatchVersion("SNAPSHOT");
        }

        updateVersionProperty(version.toString());
        flow.git().add().addFilepattern(getPropertiesFileName()).call();
        flow.git().commit().setMessage("update version to " + version.toString() + " development").call();

        if (getPlugin().getExtension().isPushAfterReleaseFinish()) {
            flow.git().push().setPushTags().setPushAll().call();
        }
    }

    private void delay() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new GradleException("release finish interrupted", e);
        }
    }
}
