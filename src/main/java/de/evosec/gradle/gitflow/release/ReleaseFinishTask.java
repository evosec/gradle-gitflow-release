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

        pullMasterBranch(flow);

        flow.releaseFinish(version.getNormalVersion())
                .setAllowUntracked(getPlugin().getExtension().isAllowUntracked())
                .call();

        delay();

        if (getPlugin().getExtension().isIncrementMinorVersion()) {
            version = version.incrementMinorVersion("SNAPSHOT");
        } else {
            version = version.incrementPatchVersion("SNAPSHOT");
        }

        updateVersionAndCommit(flow, version.toString(), "development");

        if (getPlugin().getExtension().isPushAfterReleaseFinish()) {
            flow.git().push().setPushTags().setPushAll().call();
        }
    }

    private void pullMasterBranch(JGitFlow flow) throws GitAPIException {
        String originalBranch = getCurrentBranch(flow);

        flow.git().checkout().setName("master").call();
        flow.git().pull().call();
        flow.git().checkout().setName(originalBranch).call();
    }

    private String getCurrentBranch(JGitFlow flow) {
        try {
            return flow.git().getRepository().getBranch();
        } catch (Exception e) {
            throw new GradleException("unable to get current branch name", e);
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
