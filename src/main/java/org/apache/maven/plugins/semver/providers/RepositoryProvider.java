package org.apache.maven.plugins.semver.providers;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.semver.SemverMavenPlugin;
import org.apache.maven.plugins.semver.configuration.SemverConfiguration;
import org.apache.maven.plugins.semver.exceptions.SemverException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * <h>RepositoryProvider</h>
 * <p>Provider that is used to make connection to SCM repository and handle all request to and from that repository.</p>
 *
 * @author sido
 */
public class RepositoryProvider {

  private static final String URL_GITHUB = "github.com";

  private Log LOG;

  private Git repository;
  private CredentialsProvider provider;
  private Prompter prompter;

  private String branch = "master";

  /**
   *
   * <p>Initialize the RepositoryProvider.</p>
   *
   * @param LOG get logging from the mojo
   * @param project get the {@link MavenProject} from the mojo
   * @param configuration get the {@link SemverConfiguration} from the mojo
   * @param prompter get the {@link Prompter} from the mojo
   */
  public RepositoryProvider(Log LOG, MavenProject project, SemverConfiguration configuration, Prompter prompter) {
    this.LOG = LOG;
    this.prompter = prompter;
    try {
      repository = initializeRepository(project);
      provider = initializeCredentialsProvider(project, configuration);
    } catch (SemverException err) {
      LOG.error(err.getMessage());
      Runtime.getRuntime().exit(1);
    }
  }

  /**
   * <p>Initialize SCM-repo for determining branch and tag information.</p>
   *
   * @param project {@link MavenProject}
   * @return {@link Git} SCM-repository
   * @throws SemverException exception for not initializing local and remote repository
   */
  private Git initializeRepository(MavenProject project) throws SemverException {
    Git repository = null;
    LOG.info(SemverMavenPlugin.FUNCTION_LINE_BREAK);
    LOG.info("Initializing SCM-repository");
    FileRepositoryBuilder repoBuilder = new FileRepositoryBuilder();
    repoBuilder.addCeilingDirectory(project.getBasedir());
    repoBuilder.findGitDir(project.getBasedir());
    Repository repo = null;
    try {
      repo = repoBuilder.build();
      repository = new Git(repo);
      LOG.info(" * SCM-repository is initialized");
    } catch (Exception err) {
      LOG.error(" * This is not a valid SCM-repository.");
      LOG.error(" * Please run this goal in a valid SCM-repository");
      LOG.error(" * Could not initialize repostory", err);
      throw new SemverException("This is not a valid SCM-repository", "Please run this goal in a valid SCM-repository");
    }
    return repository;
  }

  /**
   * <p>Initialize credentialsprovider to acces remote SCM repository.</p>
   *
   * @param configuration {@link SemverConfiguration}
   * @return {@link CredentialsProvider} initialized credentialsProvider
   */
  private CredentialsProvider initializeCredentialsProvider(MavenProject project, SemverConfiguration configuration) {
    LOG.info("Initializing SCM-credentialsprovider");
    CredentialsProvider provider = null;
    String scmUserName = configuration.getScmUsername();
    String scmPassword = configuration.getScmPassword();
    String scmDefaultUsername = "";
    if(scmUserName.isEmpty() || scmPassword.isEmpty()) {
      String messageUsername = "[info]  * Please enter your (SCM) username";
      String messagePassword = "[info]  * Please enter your (SCM) password";
      if(project.getScm().getUrl().contains(URL_GITHUB)) {
        scmDefaultUsername = "token";
        messageUsername = "[info]  * Please enter your (SCM) token";
        messagePassword = "[info]  * Please enter your (SCM) secret";
      }
      try {
        scmUserName = prompter.prompt(messageUsername, scmDefaultUsername);
        scmPassword = prompter.promptForPassword(messagePassword);
      } catch (PrompterException err) {
        LOG.error(err);
      }
    }
    if (!(scmUserName.isEmpty() || scmPassword.isEmpty())) {
      provider = new UsernamePasswordCredentialsProvider(scmPassword, scmPassword);
      LOG.info(" * SCM-credentialsprovider is initialized");
      LOG.info(SemverMavenPlugin.FUNCTION_LINE_BREAK);
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
      LOG.error("Please check your SCM-credentials to fix this issue");
      LOG.error("Please run semver:rollback to return to initial state");
      Runtime.getRuntime().exit(1);
    }
    return isSuccess;
  }

  /**
   *
   * <p>Get the currentbranch to determine the current branch version.</p>
   *
   * @return current branch
   */
  public String getCurrentBranch() {
    String currentBranch = "";
    try {
      currentBranch = repository.getRepository().getBranch();
    } catch (IOException err) {
      LOG.error(err.getMessage());
      LOG.error("");
      LOG.error("Please check your SCM-credentials to fix this issue");
      LOG.error("Please run semver:rollback to return to initial state");
      Runtime.getRuntime().exit(1);
    }
    return currentBranch;
  }

  /**
   *
   * <p>Return a list of local SCM-tags.</p>
   *
   * @return local SCM-tags
   */
  public List<Ref> getLocalTags() {
    List<Ref> tags = new ArrayList<Ref>();
    try {
      tags = repository.tagList().call();
    } catch (GitAPIException err) {
      LOG.error(err.getMessage());
      LOG.error("");
      LOG.error("Please check your SCM-credentials to fix this issue");
      LOG.error("Please run semver:rollback to return to initial state");
      Runtime.getRuntime().exit(1);
    }
    return tags;
  }

  /**
   *
   * <p>Return a list of remote SCM-tags.</p>
   *
   * @return remote SCM-tags
   */
  public Map<String, Ref> getRemoteTags() {
    Map<String, Ref> tags = new HashMap<>();
    try {
      tags = repository.pull().setCredentialsProvider(provider).getRepository().getTags();
    } catch (Exception err) {
      LOG.error(err.getMessage());
      LOG.error("");
      LOG.error("Please check your SCM-credentials to fix this issue");
      LOG.error("Please run semver:rollback to return to initial state");
      Runtime.getRuntime().exit(1);
    }
    return tags;
  }

  /**
   *
   * <p>Create a local SCM-tag.</p>
   *
   * @param tag SCM-tag to create
   * @return is the SCM-tag succesfully created
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
      LOG.error("Please check your SCM-credentials to fix this issue");
      LOG.error("Please run semver:rollback to return to initial state");
      Runtime.getRuntime().exit(1);
    }
    return isTagCreated;
  }

  /**
   *
   * <p>Delete a local SCM-tag</p>
   *
   * @param tag SCM-tag to delete
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
      LOG.error("Please check your SCM-credentials to fix this issue");
      LOG.error("Please run semver:rollback to return to initial state");
      Runtime.getRuntime().exit(1);
    }
    return isSuccess;
  }

  /**
   *
   * <p>Perform a commit on the local repository</p>
   *
   * @param message SCM-commit message
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
      LOG.error("Please check your SCM-credentials to fix this issue");
      LOG.error("Please run semver:rollback to return to initial state");
      Runtime.getRuntime().exit(1);
    }

    return isCommitSuccess;
  }

  /**
   *
   * <p>Push all changes to remote.</p>
   *
   * @return is push successfull
   */
  public boolean push(){
    boolean isPushSuccess = true;
    try {
      repository.push().setPushAll().setRemote("origin").setCredentialsProvider(provider).call();
    } catch (GitAPIException err) {
      isPushSuccess = false;
      LOG.error(err.getMessage());
      LOG.error("");
      LOG.error("Please check your SCM-credentials to fix this issue");
      LOG.error("Please run semver:rollback to return to initial state");
      Runtime.getRuntime().exit(1);
    }
    return isPushSuccess;
  }

  /**
   *
   * <p>Push a SCM-tag to the remote SCM-repository.</p>
   *
   * @return is the tag succesfully pushed
   */
  public boolean pushTag() {
    boolean isSuccess = true;
    try {
      repository.push().setPushTags().setRemote("origin").setCredentialsProvider(provider).call();
    } catch (GitAPIException err) {
      isSuccess = false;
      LOG.error(err.getMessage());
      LOG.error("");
      LOG.error("");
      LOG.error("Please check your SCM-credentials to fix this issue");
      LOG.error("Please run semver:rollback to return to initial state");
      Runtime.getRuntime().exit(1);
    }
    return isSuccess;
  }

  /**
   * <p>Close the repository when finished.</p>
   */
  public void closeRepository() {
    repository.close();
  }

  /**
   *
   * <p>Determine if there are any open changes in the SCM-repository.</p>
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
   * <p>When a <i>release:rollback</i> is performed local SCM-tags have to be cleaned to perform the next release.</p>
   *
   * @param scmVersion scmVersion
   * @throws SemverException native plugin exception
   * @throws IOException disk write exception
   * @throws GitAPIException repository exception
   */
  public void cleanupGitLocalAndRemoteTags(Log LOG, String scmVersion) throws SemverException, IOException, GitAPIException {
    LOG.info("Check for matching tags            :  [ " + scmVersion + " ]");
    LOG.info(SemverMavenPlugin.MOJO_LINE_BREAK);
    pull();
    List<Ref> refs = getLocalTags();
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
          pushTag();
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
