package org.apache.maven.plugins.semver.providers;

import org.apache.maven.plugins.semver.exceptions.SemverException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author sido
 */
public interface RepositoryProvider {

    void initialize(File baseDir, String scmUrl, String configScmUsername, String configScmPassword);

    boolean isInitialized();

    boolean pull();

    String getCurrentBranch();

    List<Ref> getLocalTags();

    Map<String, Ref> getRemoteTags();

    boolean createTag(String tag);

    boolean deleteTag(String tag);

    boolean commit(String message);

    boolean push();

    boolean pushTag();

    void closeRepository();

    boolean isChanged();

    void isLocalVersionCorrupt(String scmVersion) throws SemverException, IOException, GitAPIException;

    boolean isRemoteVersionCorrupt(String scmVersion);
}
