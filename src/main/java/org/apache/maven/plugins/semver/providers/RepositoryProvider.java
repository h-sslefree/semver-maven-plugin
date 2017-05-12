package org.apache.maven.plugins.semver.providers;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.semver.SemverMavenPlugin;
import org.apache.maven.plugins.semver.configuration.SemverConfiguration;
import org.apache.maven.plugins.semver.exceptions.SemverException;
import org.apache.maven.project.MavenProject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * <h>RepositoryProvider</h>
 * <p>
 *
 *
 * </p>
 *
 * @author sido
 */
public class RepositoryProvider {

  private Log LOG;

  private Git repository;
  private CredentialsProvider provider;

  public RepositoryProvider(Log LOG, MavenProject project, SemverConfiguration configuration) {
    this.LOG = LOG;
    try {
      repository = initializeRepository(project);
      provider = initializeCredentialsProvider(configuration);
    } catch (SemverException err) {
      LOG.error(err.getMessage());
      Runtime.getRuntime().exit(1);
    }
  }

  /**
   * <p>Initialize GIT-repo for determining branch and tag information.</p>
   *
   * @param project {@link MavenProject}
   * @return {@link Git} GIT-repository
   * @throws SemverException exception for not initializing local and remote repository
   */
  private Git initializeRepository(MavenProject project) throws SemverException {
    Git repository = null;
    LOG.info(SemverMavenPlugin.FUNCTION_LINE_BREAK);
    LOG.info("Initializing GIT-repository");
    FileRepositoryBuilder repoBuilder = new FileRepositoryBuilder();
    repoBuilder.addCeilingDirectory(project.getBasedir());
    repoBuilder.findGitDir(project.getBasedir());
    Repository repo = null;
    try {
      repo = repoBuilder.build();
      repository = new Git(repo);
      LOG.info(" * GIT-repository is initialized");
    } catch (Exception err) {
      LOG.error(" * This is not a valid GIT-repository.");
      LOG.error(" * Please run this goal in a valid GIT-repository");
      LOG.error(" * Could not initialize repostory", err);
      throw new SemverException("This is not a valid GIT-repository", "Please run this goal in a valid GIT-repository");
    }
    return repository;
  }

  /**
   * <p>Initialize credentialsprovider to acces remote GIT repository.</p>
   *
   * @param configuration {@link SemverConfiguration}
   * @return {@link CredentialsProvider} initialized credentialsProvider
   */
  private CredentialsProvider initializeCredentialsProvider(SemverConfiguration configuration) {
    CredentialsProvider provider = null;
    if (!(configuration.getScmPassword().isEmpty() || configuration.getScmUsername().isEmpty())) {
      provider = new UsernamePasswordCredentialsProvider(configuration.getScmUsername(), configuration.getScmPassword());
      LOG.info(" * GIT-credential provider is initialized");
    }
    return provider;
  }

  /**
   *
   * <p>Perform a pull from the remote GIT-repository.</p>
   *
   * @return is pull completed?
   */
  public boolean pull() {
    boolean isSuccess = true;
    try {
      repository.pull().setCredentialsProvider(provider).call();
    } catch (GitAPIException err) {
      isSuccess = false;
      LOG.error(err.getMessage());
      LOG.error("");
      LOG.error("Please check your GIT-credentials to fix this issue");
      LOG.error("Please run semver:rollback to return to initial state");
      Runtime.getRuntime().exit(1);
    }
    return isSuccess;
  }

  /**
   *
   * <p>Perform a push to the remote GIT-repository</p>
   *
   * @return is the push completed?
   */
  public boolean push() {
    boolean isSuccess = true;
    try {
      repository.push().setRemote("origin").setCredentialsProvider(provider).call();
    } catch (GitAPIException err) {
      isSuccess = false;
      LOG.error(err.getMessage());
      LOG.error("");
      LOG.error("Please check your GIT-credentials to fix this issue");
      LOG.error("Please run semver:rollback to return to initial state");
      Runtime.getRuntime().exit(1);
    }
    return isSuccess;
  }

  /**
   *
   * <p>Push a GIT-tag to the remote GIT-repository.</p>
   *
   * @param tag GIT-tag
   * @return is the tag succesfully pushed
   */
  public boolean pushTag(String tag) {
    boolean isSuccess = true;
    RefSpec refSpec = new RefSpec().setSource(null).setDestination(tag);
    try {
      repository.push().setRemote("origin").setRefSpecs(refSpec).setCredentialsProvider(provider).call();
    } catch (GitAPIException err) {
      isSuccess = false;
      LOG.error(err.getMessage());
      LOG.error("");
      LOG.error("");
      LOG.error("Please check your GIT-credentials to fix this issue");
      LOG.error("Please run semver:rollback to return to initial state");
      Runtime.getRuntime().exit(1);
    }
    return isSuccess;
  }

  /**
   *
   *
   * @return
   */
  public String getCurrentBranch() {
    String currentBranch = "";
    try {
      currentBranch = repository.getRepository().getBranch();
    } catch (IOException err) {
      LOG.error(err.getMessage());
      LOG.error("");
      LOG.error("Please check your GIT-credentials to fix this issue");
      LOG.error("Please run semver:rollback to return to initial state");
      Runtime.getRuntime().exit(1);
    }
    return currentBranch;
  }

  /**
   *
   * <p>Return a list of remote GIT-tags.</p>
   *
   * @return GIT-tags
   */
  public List<Ref> getTags() {
    List<Ref> tags = new ArrayList<Ref>();
    try {
      tags = repository.tagList().call();
    } catch (GitAPIException err) {
      LOG.error(err.getMessage());
      LOG.error("");
      LOG.error("Please check your GIT-credentials to fix this issue");
      LOG.error("Please run semver:rollback to return to initial state");
      Runtime.getRuntime().exit(1);
    }
    return tags;
  }

  /**
   *
   * <p>Create a local GIT-tag.</p>
   *
   * @param tag GIT-tag to create
   * @return is the GIT-tag succesfully created
   */
  public boolean createTag(String tag) {
    boolean isTagCreated = true;
    try {
      deleteTag(tag);
      repository.tag().setName(tag).call();
    } catch (GitAPIException err) {
      isTagCreated = false;
      LOG.error(err.getMessage());
      LOG.error("");
      LOG.error("Please check your GIT-credentials to fix this issue");
      LOG.error("Please run semver:rollback to return to initial state");
      Runtime.getRuntime().exit(1);
    }
    return isTagCreated;
  }

  /**
   *
   * <p>Delete a local GIT-tag</p>
   *
   * @param tag GIT-tag to delete
   * @return is the tag succesfully deleted?
   */
  public boolean deleteTag(String tag) {
    boolean isSuccess = true;
    try {
      repository.tagDelete().setTags(tag).call();
    } catch (GitAPIException err) {
      isSuccess = false;
      LOG.error(err.getMessage());
      LOG.error("");
      LOG.error("Please check your GIT-credentials to fix this issue");
      LOG.error("Please run semver:rollback to return to initial state");
      Runtime.getRuntime().exit(1);
    }
    return isSuccess;
  }

  /**
   *
   * <p>Perform a commit on the local repository</p>
   *
   * @param message GIT-commit message
   * @return is the commit completed?
   */
  public boolean commit(String message) {
    boolean isCommitSuccess = true;
    try {
      repository.commit().setAll(true).setMessage(message).call();
    } catch (GitAPIException err) {
      isCommitSuccess = false;
      LOG.error(err.getMessage());
      LOG.error("");
      LOG.error("Please check your GIT-credentials to fix this issue");
      LOG.error("Please run semver:rollback to return to initial state");
      Runtime.getRuntime().exit(1);
    }

    return isCommitSuccess;
  }

  /**
   * <p>Close the repository when finished.</p>
   */
  public void closeRepository() {
    repository.close();
  }

  /**
   *
   * <p>Determine if there are any open changes in the GIT-repository.</p>
   *
   * @return are there any open changes?
   */
  public boolean isChanged() {
    boolean isChanged = false;
    LOG.info("Check for local or remote changes");
    LOG.info(SemverMavenPlugin.MOJO_LINE_BREAK);
    pull();
    try {
      Status status = repository.status().call();
      if (!status.isClean()) {
        LOG.error("Semver-action has failed");
        LOG.error("There are uncomitted changes");
        LOG.error("Please commit and push the open changes");
        isChanged = true;
      } else {
        LOG.info("Local changes                      : workingtree is clean");
      }
    } catch (GitAPIException err) {
      LOG.error(err.getMessage());
      isChanged = true;
    }
    LOG.info(SemverMavenPlugin.FUNCTION_LINE_BREAK);
    return isChanged;
  }

  /**
   * <p>When a <i>release:rollback</i> is performed local git-tags have to be cleaned to perform the next release.</p>
   *
   * @param scmVersion scmVersion
   * @throws SemverException native plugin exception
   * @throws IOException disk write exception
   * @throws GitAPIException repository exception
   */
  public void cleanupGitLocalAndRemoteTags(Log LOG, String scmVersion) throws SemverException, IOException, GitAPIException {
    LOG.info("Check for lost-tags");
    LOG.info(SemverMavenPlugin.MOJO_LINE_BREAK);
    pull();
    List<Ref> refs = getTags();
    LOG.debug("Remote tags                        ");
    for(Ref ref : refs) {
      LOG.debug(" * " + ref.getName());
    }
    if (refs.isEmpty()) {
      boolean found = false;
      for (Ref ref : refs) {
        if (ref.getName().contains(scmVersion)) {
          found = true;
          LOG.info("Delete lost local-tag                   : " + ref.getName().substring(10));
          deleteTag(ref.getName());
          LOG.info("Delete lost remote-tag                  : " + ref.getName().substring(10));
          pushTag(ref.getName());
        }
      }
      if (!found) {
        LOG.info("No lost-tags where found           : local or remote");
      }
    } else {
      LOG.info("No lost-tags where found           : local or remote");
    }
    closeRepository();
    LOG.info(SemverMavenPlugin.FUNCTION_LINE_BREAK);
  }

}
