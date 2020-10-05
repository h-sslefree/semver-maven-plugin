package org.apache.maven.plugins.semver.providers;

import org.apache.maven.plugins.semver.configuration.SemverConfiguration;
import org.apache.maven.plugins.semver.exceptions.SemverException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * <h1>RepositoryProvider</h1>
 *
 * <p>The repository provider does all communication between the local and remote repository
 *
 * <p>At this moment only GIT is supported.
 *
 * @author sido
 */
public interface RepositoryProvider
{

	/**
	 * Iniitalize repository for {@link RepositoryProviderImpl}
	 *
	 * @param baseDir           baseDirectory of GIT-repository
	 * @param scmUrl            repository url
	 * @param configScmUsername scmUsername from {@link SemverConfiguration}
	 * @param configScmPassword scmPassword from {@link SemverConfiguration}
	 */
	void initialize(File baseDir, String scmUrl, String configScmUsername, String configScmPassword);

	/**
	 * Is the repotistory succesfully initialized?
	 *
	 * @return isInitialized
	 */
	boolean isInitialized();

	/**
	 * Perform a pull from the remote GIT-repository.
	 *
	 * @return is pull completed?
	 */
	void pull();

	/**
	 * Get currentbranch which you are working in
	 *
	 * @return curent branch
	 */
	String getCurrentBranch();

	/**
	 * Return a list of local SCM-tags.
	 *
	 * @return local SCM-tags
	 */
	List<Ref> getLocalTags();

	/**
	 * Return a list of remote SCM-tags.
	 *
	 * @return remote SCM-tags
	 */
	Map<String, Ref> getRemoteTags();

	/**
	 * Create a local SCM-tag.
	 *
	 * @param tag SCM-tag to create
	 * @return is the SCM-tag succesfully created
	 */
	void createTag(String tag);

	/**
	 * Delete a local SCM-tag
	 *
	 * @param tag SCM-tag to delete
	 * @return is the tag succesfully deleted?
	 */
	void deleteTag(String tag);

	/**
	 * Perform a commit on the local repository
	 *
	 * @param message SCM-commit message
	 * @return is the commit completed?
	 */
	void commit(String message);

	/**
	 * Push all changes to remote.
	 *
	 * @return is push successfull
	 */
	void push();

	/**
	 * Push a SCM-tag to the remote SCM-repository.
	 *
	 * @return is the tag succesfully pushed
	 */
	void pushTag();

	/**
	 * Close the repository when finished.
	 */
	void closeRepository();

	/**
	 * Determine if there are any open changes in the SCM-repository.
	 *
	 * @return are there any open changes?
	 */
	boolean isChanged();

	/**
	 * When a <i>release:rollback</i> is performed local SCM-tags have to be cleaned to perform the
	 * next release.
	 *
	 * @param scmVersion scmVersion
	 * @throws SemverException native plugin exception
	 * @throws IOException     disk write exception
	 * @throws GitAPIException repository exception
	 */
	void isLocalVersionCorrupt(String scmVersion) throws SemverException, IOException, GitAPIException;

	/**
	 * Determine if remote version is corrupt.
	 *
	 * @param scmVersion the pomVersion which has to be evaluated
	 * @return is corrupt or not
	 */
	boolean isRemoteVersionCorrupt(String scmVersion);
}
