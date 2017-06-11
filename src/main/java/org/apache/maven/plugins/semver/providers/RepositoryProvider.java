package org.apache.maven.plugins.semver.providers;

import org.apache.maven.plugins.semver.exceptions.SemverException;
import org.apache.maven.plugins.semver.configuration.SemverConfiguration;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 *
 * <h1>RepositoryProvider</h1>
 *
 * <p></p>
 *
 * @author sido
 */
public interface RepositoryProvider {

    /**
     *
     * <p>Iniitalize repository for {@link RepositoryProviderImpl}</p>
     *
     * @param baseDir baseDirectory of GIT-repository
     * @param scmUrl repository url
     * @param configScmUsername scmUsername from {@link SemverConfiguration}
     * @param configScmPassword scmPassword from {@link SemverConfiguration}
     */
    void initialize(File baseDir, String scmUrl, String configScmUsername, String configScmPassword);

    /**
     *
     * <p>Is the repotistory succesfully initialized?</p>
     *
     * @return isInitialized
     */
    boolean isInitialized();

    /**
     *
     * <p>Perform a pull from the remote GIT-repository.</p>
     *
     * @return is pull completed?
     */
    boolean pull();

    /**
     *
     * <p>Get currentbranch which you are working in</p>
     *
     * @return curent branch
     */
    String getCurrentBranch();

    /**
     *
     * <p>Return a list of local SCM-tags.</p>
     *
     * @return local SCM-tags
     */
    List<Ref> getLocalTags();

    /**
     *
     * <p>Return a list of remote SCM-tags.</p>
     *
     * @return remote SCM-tags
     */
    Map<String, Ref> getRemoteTags();

    /**
     *
     * <p>Create a local SCM-tag.</p>
     *
     * @param tag SCM-tag to create
     * @return is the SCM-tag succesfully created
     */
    boolean createTag(String tag);

    /**
     *
     * <p>Delete a local SCM-tag</p>
     *
     * @param tag SCM-tag to delete
     * @return is the tag succesfully deleted?
     */
    boolean deleteTag(String tag);

    /**
     *
     * <p>Perform a commit on the local repository</p>
     *
     * @param message SCM-commit message
     * @return is the commit completed?
     */
    boolean commit(String message);

    /**
     *
     * <p>Push all changes to remote.</p>
     *
     * @return is push successfull
     */
    boolean push();

    /**
     *
     * <p>Push a SCM-tag to the remote SCM-repository.</p>
     *
     * @return is the tag succesfully pushed
     */
    boolean pushTag();

    /**
     * <p>Close the repository when finished.</p>
     */
    void closeRepository();

    /**
     *
     * <p>Determine if there are any open changes in the SCM-repository.</p>
     *
     * @return are there any open changes?
     */
    boolean isChanged();

    /**
     * <p>When a <i>release:rollback</i> is performed local SCM-tags have to be cleaned to perform the next release.</p>
     *
     * @param scmVersion scmVersion
     * @throws SemverException native plugin exception
     * @throws IOException disk write exception
     * @throws GitAPIException repository exception
     */
    void isLocalVersionCorrupt(String scmVersion) throws SemverException, IOException, GitAPIException;

    /**
     *
     * <p>Determine if remote version is corrupt.</p>
     *
     * @param scmVersion the pomVersion which has to be evaluated
     * @return is corrupt or not
     */
    boolean isRemoteVersionCorrupt(String scmVersion);
}
