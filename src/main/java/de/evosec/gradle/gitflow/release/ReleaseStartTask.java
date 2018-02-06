package de.evosec.gradle.gitflow.release;

import com.atlassian.jgitflow.core.JGitFlow;
import com.atlassian.jgitflow.core.exception.DirtyWorkingTreeException;
import com.atlassian.jgitflow.core.exception.JGitFlowException;
import com.github.zafarkhaja.semver.Version;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.tasks.TaskAction;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class ReleaseStartTask extends AbstractTask {

    @TaskAction
    public void releaseStart() throws JGitFlowException, GitAPIException {
        checkPropertiesFile();
        checkSnapshotDependencies();

        JGitFlow flow = ensureGitFlow();

        Version version = Version.valueOf(getProject().getVersion().toString());

        try {
            flow.releaseStart(version.getNormalVersion())
                    .setAllowUntracked(getPlugin().getExtension().isAllowUntracked())
                    .call();
        } catch (DirtyWorkingTreeException e) {
            throw new GradleException(e.getMessage(), e);
        }

        updateVersionProperty(version.getNormalVersion());
        flow.git().add().setUpdate(true).addFilepattern(getPropertiesFileName()).call();
        flow.git().commit().setMessage("update version to " + getProject().getVersion() + " release").call();
    }

    private void checkSnapshotDependencies() {
        StringBuilder messageBuilder = new StringBuilder();

        Action<Project> dependencyChecker = getDependencyChecker(messageBuilder);
        dependencyChecker.execute(getProject());
        getProject().subprojects(dependencyChecker);

        String message = messageBuilder.toString();
        if (!message.isEmpty()) {
            message = "Snapshot dependencies detected: " + message;
            warnOrThrow(getPlugin().getExtension().isFailOnSnapshotDependencies(), message);
        }
    }

    private Action<Project> getDependencyChecker(StringBuilder messageBuilder) {
        return project -> {
            List<String> snapshotDependencies = project.getConfigurations()
                    .stream()
                    .flatMap(c -> c.getDependencies().stream())
                    .filter(d-> !(d instanceof ProjectDependency))
                    .filter(d -> d.getVersion() != null && d.getVersion().contains("SNAPSHOT"))
                    .map(d -> {
                        String group = d.getGroup() == null ? "" : d.getGroup();
                        return group + ":" + d.getName() + ":" + d.getVersion();
                    })
                    .collect(toList());

            if (!snapshotDependencies.isEmpty()) {
                messageBuilder.append("\n\t").append(project.getName())
                        .append(": ").append(snapshotDependencies);
            }
        };
    }

    private void warnOrThrow(boolean doThrow, String message) {
        if (doThrow) {
            throw new GradleException(message);
        } else {
            getProject().getLogger().warn(message);
        }
    }
}
