package org.apache.maven.plugins.semver.providers;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.plugins.semver.SemverMavenPlugin;
import org.apache.maven.plugins.semver.exceptions.SemverException;
import org.apache.maven.plugins.semver.exceptions.SemverExceptionMessages;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;

import java.io.Console;
import java.io.File;
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
@Component(role = RepositoryProvider.class)
public class RepositoryProviderImpl implements RepositoryProvider {

  private static final String URL_GITHUB = "github.com";

  private enum CREDENTIALS {
    USERNAME,
    PASSWORD
  }

  @Requirement
  private Logger LOG;

  private Console console = System.console();

  private boolean isInitialized = false;

  private Git repository;
  private CredentialsProvider provider;


  /**
   *
   * <p>Initialize the RepositoryProvider.</p>
   *
   */
  public RepositoryProviderImpl() {}

  public void initialize(File baseDir, String scmUrl, String configScmUsername, String configScmPassword) {
    try {
      repository = initializeRepository(baseDir);
      provider = initializeCredentialsProvider(scmUrl, configScmUsername, configScmPassword);
    } catch (SemverException err) {
      LOG.error(err.getMessage());
      Runtime.getRuntime().exit(1);
    }
  }

  public boolean isInitialized() {
    return this.isInitialized;
  }

  /**
   * <p>Initialize SCM-repo for determining branch and tag information.</p>
   *
   * @param baseDir SCm root-directory
   * @return {@link Git} SCM-repository
   * @throws SemverException exception for not initializing local and remote repository
   */
  private Git initializeRepository(File baseDir) throws SemverException {
    Git repository = null;
    LOG.info(SemverMavenPlugin.FUNCTION_LINE_BREAK);
    LOG.info("Initializing SCM-repository");
    FileRepositoryBuilder repoBuilder = new FileRepositoryBuilder();
    repoBuilder.addCeilingDirectory(baseDir);
    repoBuilder.findGitDir(baseDir);
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
   * @return {@link CredentialsProvider} initialized credentialsProvider
   */
  private CredentialsProvider initializeCredentialsProvider(String scmUrl, String configScmUserName, String configScmPassword) {
    LOG.info("Initializing SCM-credentialsprovider");
    CredentialsProvider provider;

    Map<CREDENTIALS, String> credentials = promptForCredentials(configScmUserName, configScmPassword, scmUrl);

    provider = new UsernamePasswordCredentialsProvider(credentials.get(CREDENTIALS.USERNAME), credentials.get(CREDENTIALS.PASSWORD));
    LOG.info(" * Validate credentials");
    boolean isAuthorized = checkCredentials(provider);
    while(!isAuthorized) {
      Map<CREDENTIALS, String> newCredentials = promptForCredentials("", "", scmUrl);
      provider = new UsernamePasswordCredentialsProvider(newCredentials.get(CREDENTIALS.USERNAME), newCredentials.get(CREDENTIALS.PASSWORD));
      isAuthorized = checkCredentials(provider);
    }
    LOG.info(" * SCM-credentialsprovider is initialized");
    isInitialized = true;

    return provider;
  }

  /**
   *
   * <h1>Check if credentials are valid</h1>
   * <p>Checks with a lsremote command if the remote repository is reachable</p>
   *
   * @param provider give the new {@link UsernamePasswordCredentialsProvider}
   * @return isAuthorized
   */
  private boolean checkCredentials(CredentialsProvider provider) {
    boolean isAuthorized = false;
    try {
      repository.lsRemote().setCredentialsProvider(provider).call();
      isAuthorized = true;
      LOG.info(" * Current credentials are valid");
    } catch(GitAPIException err) {
      LOG.error(err.getMessage());
    }

    return isAuthorized;
  }

  private Map<CREDENTIALS, String> promptForCredentials(String configScmUserName, String configScmPassword, String scmUrl) {
    Map<CREDENTIALS, String> credentials = new HashMap<>();
    String scmDefaultUsername = "";
    if(configScmUserName.isEmpty() || configScmPassword.isEmpty()) {
      String messageUsername = "[info]  * Please enter your (SCM) username : ";
      String messagePassword = "[info]  * Please enter your (SCM) password : ";
      if(scmUrl.contains(URL_GITHUB)) {
        scmDefaultUsername = "token";
        messageUsername = "[info]  * Please enter your (SCM) token : ";
        messagePassword = "[info]  * Please enter your (SCM) secret : ";
      }
      try {
        if(scmDefaultUsername.isEmpty()){
          credentials.put(CREDENTIALS.USERNAME, console.readLine(messageUsername));
        } else {
          credentials.put(CREDENTIALS.USERNAME, console.readLine(messageUsername, scmDefaultUsername));
        }
        credentials.put(CREDENTIALS.PASSWORD, new String(console.readPassword(messagePassword)));
      } catch (Exception err) {
        LOG.error(err.getMessage());
      }
    } else {
      credentials.put(CREDENTIALS.USERNAME, configScmUserName);
      credentials.put(CREDENTIALS.PASSWORD, configScmPassword);
    }
    return credentials;
  }

  /**
   *
   * <p>Perform a pull from the remote GIT-repository.</p>
   *
   * @return is pull completed?
   */
  @Override
  public boolean pull() {
    boolean isSuccess = true;
    try {
      repository.pull().setCredentialsProvider(provider).call();
    } catch (GitAPIException err) {
      isSuccess = false;
      LOG.error(err.getMessage());
      LOG.error("");
      LOG.error(SemverExceptionMessages.MESSAGE_ERROR_SCM_CREDENTIALS);
      Runtime.getRuntime().exit(1);
    }
    return isSuccess;
  }

  /**
   *
   * <p>Perform a pull from the remote GIT-repository.</p>
   *
   * @return is pull completed?
   */
  private boolean checkRemoteUpdates() {
    boolean isRemoteDifferent = false;
    try {
      FetchResult fetch = repository.fetch().setRemote("origin").setCredentialsProvider(provider).setDryRun(true).call();
      if(!fetch.getTrackingRefUpdates().isEmpty()) {
        isRemoteDifferent = true;
      }
    } catch (GitAPIException err) {
      LOG.error(err.getMessage());
      LOG.error("");
      LOG.error(SemverExceptionMessages.MESSAGE_ERROR_SCM_CREDENTIALS);
      Runtime.getRuntime().exit(1);
    }
    return isRemoteDifferent;
  }

  /**
   *
   * <p>Get the currentbranch to determine the current branch version.</p>
   *
   * @return current branch
   */
  @Override
  public String getCurrentBranch() {
    String currentBranch = "";
    try {
      currentBranch = repository.getRepository().getBranch();
    } catch (IOException err) {
      LOG.error(err.getMessage());
      LOG.error("");
      LOG.error(SemverExceptionMessages.MESSAGE_ERROR_SCM_CREDENTIALS);
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
  @Override
  public List<Ref> getLocalTags() {
    List<Ref> tags = new ArrayList<>();
    try {
      tags = repository.tagList().call();
    } catch (GitAPIException err) {
      LOG.error(err.getMessage());
      LOG.error("");
      LOG.error(SemverExceptionMessages.MESSAGE_ERROR_SCM_CREDENTIALS);
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
  @Override
  public Map<String, Ref> getRemoteTags() {
    Map<String, Ref> tags = new HashMap<>();
    try {
      tags = repository.pull().setCredentialsProvider(provider).getRepository().getTags();
    } catch (Exception err) {
      LOG.error(err.getMessage());
      LOG.error("");
      LOG.error(SemverExceptionMessages.MESSAGE_ERROR_SCM_CREDENTIALS);
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
  @Override
  public boolean createTag(String tag) {
    boolean isTagCreated = true;
    try {
      deleteTag(tag);
      repository.tag().setName(tag).call();
    } catch (GitAPIException err) {
      isTagCreated = false;
      LOG.error(err.getMessage());
      LOG.error("");
      LOG.error(SemverExceptionMessages.MESSAGE_ERROR_SCM_CREDENTIALS);
      LOG.error(SemverExceptionMessages.MESSAGE_ERROR_PERFORM_ROLLBACK);
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
  @Override
  public boolean deleteTag(String tag) {
    boolean isSuccess = true;
    try {
      repository.tagDelete().setTags(tag).call();
    } catch (GitAPIException err) {
      isSuccess = false;
      LOG.error(err.getMessage());
      LOG.error("");
      LOG.error(SemverExceptionMessages.MESSAGE_ERROR_SCM_CREDENTIALS);
      LOG.error(SemverExceptionMessages.MESSAGE_ERROR_PERFORM_ROLLBACK);
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
  @Override
  public boolean commit(String message) {
    boolean isCommitSuccess = true;
    try {
      repository.commit().setAll(true).setMessage(message).call();
    } catch (GitAPIException err) {
      isCommitSuccess = false;
      LOG.error(err.getMessage());
      LOG.error("");
      LOG.error(SemverExceptionMessages.MESSAGE_ERROR_SCM_CREDENTIALS);
      LOG.error(SemverExceptionMessages.MESSAGE_ERROR_PERFORM_ROLLBACK);
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
  @Override
  public boolean push(){
    boolean isPushSuccess = true;
    try {
      repository.push().setPushAll().setRemote("origin").setCredentialsProvider(provider).call();
    } catch (GitAPIException err) {
      isPushSuccess = false;
      LOG.error(err.getMessage());
      LOG.error("");
      LOG.error(SemverExceptionMessages.MESSAGE_ERROR_SCM_CREDENTIALS);
      LOG.error(SemverExceptionMessages.MESSAGE_ERROR_PERFORM_ROLLBACK);
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
  @Override
  public boolean pushTag() {
    boolean isSuccess = true;
    try {
      repository.push().setPushTags().setRemote("origin").setCredentialsProvider(provider).call();
    } catch (GitAPIException err) {
      isSuccess = false;
      LOG.error(err.getMessage());
      LOG.error("");
      LOG.error("");
      LOG.error(SemverExceptionMessages.MESSAGE_ERROR_SCM_CREDENTIALS);
      LOG.error(SemverExceptionMessages.MESSAGE_ERROR_PERFORM_ROLLBACK);
      Runtime.getRuntime().exit(1);
    }
    return isSuccess;
  }

  /**
   * <p>Close the repository when finished.</p>
   */
  @Override
  public void closeRepository() {
    repository.close();
  }

  /**
   *
   * <p>Determine if there are any open changes in the SCM-repository.</p>
   *
   * @return are there any open changes?
   */
  @Override
  public boolean isChanged() {
    boolean isChanged = false;
    LOG.info("Check for local or remote changes");
    LOG.info(SemverMavenPlugin.MOJO_LINE_BREAK);
    if(checkRemoteUpdates()) {
      isChanged = true;
      LOG.error("Remote changes                    : remote origin is ahead of local repository");
    } else {
      LOG.info("Remote changes                     : remote origin is up to date");
    }
    if(!isChanged) {
      try {
        Status status = repository.status().call();
        if (!status.isClean()) {
          isChanged = true;
        } else {
          LOG.info("Local changes                      : workingtree is clean");
          LOG.info(SemverMavenPlugin.FUNCTION_LINE_BREAK);
        }
      } catch (GitAPIException err) {
        LOG.error(err.getMessage());
        isChanged = true;
      }
    }
    if(isChanged) {
      LOG.info(SemverMavenPlugin.FUNCTION_LINE_BREAK);
      LOG.error("");
      LOG.error("Semver-goal has failed");
      LOG.error("There are uncomitted changes or the remote is ahead of local repository");
      LOG.error("Please pull remote changes and/or commit and push the open changes");
    }
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
  @Override
  public void isLocalVersionCorrupt(String scmVersion) throws SemverException, IOException, GitAPIException {
    LOG.info("Check for corrupt local tags       : [ {} ]", scmVersion);
    pull();
    List<Ref> refs = getLocalTags();
    LOG.debug("Local tags                        ");
    for(Ref ref : refs) {
      LOG.debug(" * " + ref.getName());
    }
    if (refs.isEmpty()) {
      boolean found = false;
      for (Ref ref : refs) {
        if (ref.getName().contains(scmVersion)) {
          found = true;
          String tag = ref.getName().substring(10);
          LOG.warn(" * Delete corrupt local-tag                   : [ {} ]", tag);
          deleteTag(ref.getName());
          LOG.warn(" * Delete possible corrupt remote-tag         : [ {} ]", tag);
          pushTag();
        }
      }
      if (!found) {
        LOG.info(" * No corrupt local tags where found");
      }
    } else {
      LOG.info(" * No corrupt local tags where found");
    }
    LOG.info(SemverMavenPlugin.MOJO_LINE_BREAK);
    closeRepository();
  }

  /**
   *
   * <p>Determine if remote version is corrupt.</p>
   *
   * @param scmVersion the pomVersion which has to be evaluated
   * @return is corrupt or not
   */
  @Override
  public boolean isRemoteVersionCorrupt(String scmVersion) {
    boolean isRemoteVersionCorrupt  = false;
    LOG.info("Check for corrupt remote tags      : [ {} ]", scmVersion);
    DefaultArtifactVersion localVersion;
    if(scmVersion.contains("-SNAPSHOT")) {
      localVersion = new DefaultArtifactVersion(scmVersion.replaceFirst("-SNAPSHOT", ""));
    } else {
      localVersion = new DefaultArtifactVersion(scmVersion);
    }
    Map<String, Ref> remoteTags = getRemoteTags();
    for(Map.Entry<String, Ref> remoteTag : remoteTags.entrySet()) {
      DefaultArtifactVersion remoteVersion = new DefaultArtifactVersion(remoteTag.getKey());
      LOG.debug(" * Compare remote-tag [ {} ] with local-tag [ {} ]", remoteVersion, localVersion);
      if(remoteVersion.compareTo(localVersion) > 0) {
        LOG.error(" * Local version is corrupt       : [ local: {} ] [ remote: {} ]", remoteVersion, localVersion);
        isRemoteVersionCorrupt = true;
      }
    }
    if(!isRemoteVersionCorrupt) {
      LOG.info(" * Remote is not ahead of local    : [ {} ]", scmVersion);
    }
    LOG.info(SemverMavenPlugin.FUNCTION_LINE_BREAK);
    return isRemoteVersionCorrupt;
  }

}
