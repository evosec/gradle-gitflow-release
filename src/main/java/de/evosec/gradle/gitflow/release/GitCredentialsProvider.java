package de.evosec.gradle.gitflow.release;

import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;

public class GitCredentialsProvider extends CredentialsProvider {

    private final ReleaseExtension extension;

    public GitCredentialsProvider(ReleaseExtension extension) {
        this.extension = extension;
    }

    @Override
    public boolean isInteractive() {
        return false;
    }

    @Override
    public boolean supports(CredentialItem... items) {
        for (CredentialItem i : items) {
            if (!itemIsUsernameOrPassword(i)) {
                return false;
            }
        }
        return true;
    }

    private boolean itemIsUsernameOrPassword(CredentialItem i) {
        return i instanceof CredentialItem.Username || i instanceof CredentialItem.Password;
    }

    @Override
    public boolean get(URIish uri, CredentialItem... items) {
        for (CredentialItem i : items) {
            if (i instanceof CredentialItem.Username) {
                ((CredentialItem.Username) i).setValue(extension.getUsername());
                continue;
            }
            if (i instanceof CredentialItem.Password) {
                ((CredentialItem.Password) i).setValue(extension.getPassword().toCharArray());
                continue;
            }
            if (i instanceof CredentialItem.StringType && "Password: ".equals(i.getPromptText())) {
                ((CredentialItem.StringType) i).setValue(
                        extension.getPassword());
                continue;
            }
            throw new UnsupportedCredentialItem(uri, i.getClass().getName()
                    + ":" + i.getPromptText());
        }
        return true;
    }
}
