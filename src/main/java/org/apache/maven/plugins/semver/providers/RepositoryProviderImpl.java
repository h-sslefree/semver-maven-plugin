package org.apache.maven.plugins.semver.providers;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Named;
import javax.inject.Singleton;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.plugins.semver.SemverMavenPlugin;
import org.apache.maven.plugins.semver.exceptions.SemverException;
import org.apache.maven.plugins.semver.exceptions.SemverExceptionMessages;
import org.apache.maven.plugins.semver.utils.SemverConsole;
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
import org.slf4j.LoggerFactory;

/**
 * <h>RepositoryProvider</h>
 *
 * <p>Provider that is used to make connection to SCM repository and handle all request to and from
 * that repository.
 *
 * @author sido
 */
@Named
@Singleton
public class RepositoryProviderImpl implements RepositoryProvider {

  private static final String URL_GITHUB = "github.com";
  private static final String BASE_BRANCH = "origin";

  private enum CREDENTIALS {
    USERNAME,
    PASSWORD
  }

  private final Logger logger = LoggerFactory.getLogger(RepositoryProviderImpl.class);

  private boolean isInitialized = false;

  private Git repository;
  private CredentialsProvider provider;

  @Override
  public void initialize(
      File baseDir, String scmUrl, String configScmUsername, String configScmPassword) {
    try {
      repository = initializeRepository(baseDir);
      provider = initializeCredentialsProvider(scmUrl, configScmUsername, configScmPassword);
    } catch (SemverException err) {
      logger.error(err.getMessage());
      Runtime.getRuntime().exit(1);
    }
  }

  @Override
  public boolean isInitialized() {
    return this.isInitialized;
  }

  /**
   * Initialize SCM-repo for determining branch and tag information.
   *
   * @param baseDir SCm root-directory
   * @return {@link Git} SCM-repository
   * @throws SemverException exception for not initializing local and remote repository
   */
  private Git initializeRepository(File baseDir) throws SemverException {
    Git initRepository;
    logger.info(SemverMavenPlugin.FUNCTION_LINE_BREAK);
    logger.info("Initializing SCM-repository");
    FileRepositoryBuilder repoBuilder = new FileRepositoryBuilder();
    repoBuilder.addCeilingDirectory(baseDir);
    repoBuilder.findGitDir(baseDir);
    Repository repo;
    try {
      repo = repoBuilder.build();
      initRepository = new Git(repo);
      logger.info(" * SCM-repository is initialized");
    } catch (Exception err) {
      logger.error(" * This is not a valid SCM-repository.");
      logger.error(" * Please run this goal in a valid SCM-repository");
      logger.error(" * Could not initialize repostory", err);
      throw new SemverException(
          "This is not a valid SCM-repository", "Please run this goal in a valid SCM-repository");
    }
    return initRepository;
  }

  /**
   * Initialize credentialsprovider to acces remote SCM repository.
   *
   * @return {@link CredentialsProvider} initialized credentialsProvider
   */
  private CredentialsProvider initializeCredentialsProvider(
      String scmUrl, String configScmUserName, String configScmPassword) {
    logger.info("Initializing SCM-credentialsprovider");
    CredentialsProvider initProvider;

    Map<CREDENTIALS, String> credentials =
        promptForCredentials(configScmUserName, configScmPassword, scmUrl);

    initProvider =
        new UsernamePasswordCredentialsProvider(
            credentials.get(CREDENTIALS.USERNAME), credentials.get(CREDENTIALS.PASSWORD));
    logger.info(" * Validate credentials");
    boolean isAuthorized = checkCredentials(initProvider);
    while (!isAuthorized) {
      Map<CREDENTIALS, String> newCredentials = promptForCredentials("", "", scmUrl);
      initProvider =
          new UsernamePasswordCredentialsProvider(
              newCredentials.get(CREDENTIALS.USERNAME), newCredentials.get(CREDENTIALS.PASSWORD));
      isAuthorized = checkCredentials(initProvider);
    }
    logger.info(" * SCM-credentialsprovider is initialized");
    isInitialized = true;

    return initProvider;
  }

  /**
   *
   *
   * <h1>Check if credentials are valid</h1>
   *
   * <p>Checks with a lsremote command if the remote repository is reachable
   *
   * @param provider give the new {@link UsernamePasswordCredentialsProvider}
   * @return isAuthorized
   */
  private boolean checkCredentials(CredentialsProvider provider) {
    boolean isAuthorized = false;
    try {
      repository.lsRemote().setCredentialsProvider(provider).call();
      isAuthorized = true;
      logger.info(" * Current credentials are valid");
    } catch (GitAPIException err) {
      logger.error(err.getMessage());
    }

    return isAuthorized;
  }

  /**
   * Create a prompt o fill in credentials
   *
   * @param configScmUserName username
   * @param configScmPassword password
   * @param scmUrl source control url
   * @return a map with the credentials
   */
  private Map<CREDENTIALS, String> promptForCredentials(
      String configScmUserName, String configScmPassword, String scmUrl) {
    Map<CREDENTIALS, String> credentials = new HashMap<>();
    String scmDefaultUsername = "";
    if (configScmUserName.isEmpty() || configScmPassword.isEmpty()) {
      String messageUsername = "[info]  * Please enter your (SCM) username : ";
      String messagePassword = "[info]  * Please enter your (SCM) password : ";
      if (scmUrl.contains(URL_GITHUB)) {
        scmDefaultUsername = "token";
        messageUsername = "[info]  * Please enter your (SCM) token : ";
        messagePassword = "[info]  * Please enter your (SCM) secret : ";
      }
      try {
        if (scmDefaultUsername.isEmpty()) {
          credentials.put(CREDENTIALS.USERNAME, SemverConsole.readLine(messageUsername));
        } else {
          credentials.put(
              CREDENTIALS.USERNAME, SemverConsole.readLine(messageUsername, scmDefaultUsername));
        }
        credentials.put(CREDENTIALS.PASSWORD, SemverConsole.readPassword(messagePassword));
      } catch (Exception err) {
        logger.error(err.getMessage());
      }
    } else {
      credentials.put(CREDENTIALS.USERNAME, configScmUserName);
      credentials.put(CREDENTIALS.PASSWORD, configScmPassword);
    }
    return credentials;
  }

  @Override
  public boolean pull() {
    boolean isSuccess = true;
    try {
      repository.pull().setCredentialsProvider(provider).call();
    } catch (GitAPIException err) {
      isSuccess = false;
      logger.error(err.getMessage());
      logger.error("");
      logger.error(SemverExceptionMessages.MESSAGE_ERROR_SCM_CREDENTIALS);
      Runtime.getRuntime().exit(1);
    }
    return isSuccess;
  }

  /**
   * Perform a pull from the remote GIT-repository.
   *
   * @return is pull completed?
   */
  private boolean checkRemoteUpdates() {
    boolean isRemoteDifferent = false;
    try {
      FetchResult fetch =
          repository
              .fetch()
              .setRemote(BASE_BRANCH)
              .setCredentialsProvider(provider)
              .setDryRun(true)
              .call();
      if (!fetch.getTrackingRefUpdates().isEmpty()) {
        isRemoteDifferent = true;
      }
    } catch (GitAPIException err) {
      logger.error(err.getMessage());
      logger.error("");
      logger.error(SemverExceptionMessages.MESSAGE_ERROR_SCM_CREDENTIALS);
      Runtime.getRuntime().exit(1);
    }
    return isRemoteDifferent;
  }

  @Override
  public String getCurrentBranch() {
    String currentBranch = "";
    try {
      currentBranch = repository.getRepository().getBranch();
    } catch (IOException err) {
      logger.error(err.getMessage());
      logger.error("");
      logger.error(SemverExceptionMessages.MESSAGE_ERROR_SCM_CREDENTIALS);
      Runtime.getRuntime().exit(1);
    }
    return currentBranch;
  }

  @Override
  public List<Ref> getLocalTags() {
    List<Ref> tags = new ArrayList<>();
    try {
      tags = repository.tagList().call();
    } catch (GitAPIException err) {
      logger.error(err.getMessage());
      logger.error("");
      logger.error(SemverExceptionMessages.MESSAGE_ERROR_SCM_CREDENTIALS);
      Runtime.getRuntime().exit(1);
    }
    return tags;
  }

  @Override
  public Map<String, Ref> getRemoteTags() {
    Map<String, Ref> tags = new HashMap<>();
    try {
      tags = repository.pull().setCredentialsProvider(provider).getRepository().getTags();
    } catch (Exception err) {
      logger.error(err.getMessage());
      logger.error("");
      logger.error(SemverExceptionMessages.MESSAGE_ERROR_SCM_CREDENTIALS);
      Runtime.getRuntime().exit(1);
    }
    return tags;
  }

  @Override
  public boolean createTag(String tag) {
    boolean isTagCreated = true;
    try {
      deleteTag(tag);
      repository.tag().setName(tag).call();
    } catch (GitAPIException err) {
      isTagCreated = false;
      logException(err);
      Runtime.getRuntime().exit(1);
    }
    return isTagCreated;
  }

  @Override
  public boolean deleteTag(String tag) {
    boolean isSuccess = true;
    try {
      repository.tagDelete().setTags(tag).call();
    } catch (GitAPIException err) {
      isSuccess = false;
      logException(err);
      Runtime.getRuntime().exit(1);
    }
    return isSuccess;
  }

  @Override
  public boolean commit(String message) {
    boolean isCommitSuccess = true;
    try {
      repository.commit().setAll(true).setMessage(message).call();
    } catch (GitAPIException err) {
      isCommitSuccess = false;
      logException(err);
      Runtime.getRuntime().exit(1);
    }
    return isCommitSuccess;
  }

  @Override
  public boolean push() {
    boolean isPushSuccess = true;
    try {
      repository.push().setPushAll().setRemote(BASE_BRANCH).setCredentialsProvider(provider).call();
    } catch (GitAPIException err) {
      isPushSuccess = false;
      logException(err);
    }
    return isPushSuccess;
  }

  /**
   * Log the exception.
   *
   * @param err {@link GitAPIException}
   */
  private void logException(GitAPIException err) {
    logger.error(err.getMessage());
    logger.error("");
    logger.error(SemverExceptionMessages.MESSAGE_ERROR_SCM_CREDENTIALS);
    logger.error(SemverExceptionMessages.MESSAGE_ERROR_PERFORM_ROLLBACK);
  }

  @Override
  public boolean pushTag() {
    boolean isSuccess = true;
    try {
      repository
          .push()
          .setPushTags()
          .setRemote(BASE_BRANCH)
          .setCredentialsProvider(provider)
          .call();
    } catch (GitAPIException err) {
      isSuccess = false;
      logException(err);
      Runtime.getRuntime().exit(1);
    }
    return isSuccess;
  }

  @Override
  public void closeRepository() {
    repository.close();
  }

  @Override
  public boolean isChanged() {
    boolean isChanged = false;
    logger.info("Check for local or remote changes");
    logger.info(SemverMavenPlugin.MOJO_LINE_BREAK);
    if (checkRemoteUpdates()) {
      isChanged = true;
      logger.error(
          "Remote changes                    : remote origin is ahead of local repository");
    } else {
      logger.info("Remote changes                     : remote origin is up to date");
    }
    if (!isChanged) {
      try {
        Status status = repository.status().call();
        if (!status.isClean()) {
          isChanged = true;
          this.logChanges(status);
          this.logConflicts(status);
          this.logUntracked(status);
          this.logModified(status);
          this.logMissing(status);
        } else {
          logger.info("Local changes                      : workingtree is clean");
          logger.info(SemverMavenPlugin.FUNCTION_LINE_BREAK);
        }
      } catch (GitAPIException err) {
        logger.error(err.getMessage());
        isChanged = true;
      }
    }
    if (isChanged) {
      logger.info(SemverMavenPlugin.FUNCTION_LINE_BREAK);
      logger.error("");
      logger.error("Semver-goal has failed");
      logger.error("There are uncomitted changes or the remote is ahead of local repository");
      logger.error("Please pull remote changes and/or commit and push the open changes");
    }
    return isChanged;
  }

  private void logChanges(Status status) {
    Set<String> changes = status.getChanged();
    logger.info("git changes:");
    for (String change : changes) {
      logger.info(" * " + change);
    }
  }

  private void logConflicts(Status status) {
    Set<String> conflicts = status.getConflicting();
    logger.info("git conflicts:");
    for (String conflict : conflicts) {
      logger.info(" * " + conflict);
    }
  }

  private void logUntracked(Status status) {
    Set<String> untrackeds = status.getUntracked();
    logger.info("git untracked:");
    for (String untracked : untrackeds) {
      logger.info(" * " + untracked);
    }
  }

  private void logModified(Status status) {
    Set<String> modifieds = status.getModified();
    logger.info("git modified:");
    for (String modified : modifieds) {
      logger.info(" * " + modified);
    }
  }

  private void logMissing(Status status) {
    Set<String> missings = status.getMissing();
    logger.info("git missing:");
    for (String missing : missings) {
      logger.info(" * " + missing);
    }
  }

  @Override
  public void isLocalVersionCorrupt(String scmVersion) {
    logger.info("Check for corrupt local tags       : [ {} ]", scmVersion);
    pull();
    List<Ref> refs = getLocalTags();
    logger.debug("Local tags                        ");
    for (Ref ref : refs) {
      logger.debug(format(" * %s", ref.getName()));
    }
    if (!refs.isEmpty()) {
      boolean found = false;
      for (Ref ref : refs) {
        if (ref.getName().contains(scmVersion)) {
          found = true;
          String tag = ref.getName().substring(10);
          logger.warn(" * Delete corrupt local-tag                   : [ {} ]", tag);
          deleteTag(ref.getName());
          logger.warn(" * Delete possible corrupt remote-tag         : [ {} ]", tag);
          pushTag();
        }
      }
      if (!found) {
        logger.info(" * No corrupt local tags where found");
      }
    } else {
      logger.info(" * No corrupt local tags where found");
    }
    logger.info(SemverMavenPlugin.MOJO_LINE_BREAK);
    closeRepository();
  }

  @Override
  public boolean isRemoteVersionCorrupt(String scmVersion) {
    boolean isRemoteVersionCorrupt = false;
    logger.info("Check for corrupt remote tags      : [ {} ]", scmVersion);
    DefaultArtifactVersion localVersion;
    if (scmVersion.contains("-SNAPSHOT")) {
      localVersion = new DefaultArtifactVersion(scmVersion.replaceFirst("-SNAPSHOT", ""));
    } else {
      localVersion = new DefaultArtifactVersion(scmVersion);
    }
    Map<String, Ref> remoteTags = getRemoteTags();
    for (Map.Entry<String, Ref> remoteTag : remoteTags.entrySet()) {
      DefaultArtifactVersion remoteVersion = new DefaultArtifactVersion(remoteTag.getKey());
      logger.debug(
          " * Compare remote-tag [ {} ] with local-tag [ {} ]", remoteVersion, localVersion);
      if (remoteVersion.compareTo(localVersion) > 0) {
        logger.error(
            " * Local version is corrupt       : [ local: {} ] [ remote: {} ]",
            remoteVersion,
            localVersion);
        isRemoteVersionCorrupt = true;
      }
    }
    if (!isRemoteVersionCorrupt) {
      logger.info(" * Remote is not ahead of local    : [ {} ]", scmVersion);
    }
    logger.info(SemverMavenPlugin.FUNCTION_LINE_BREAK);
    return isRemoteVersionCorrupt;
  }
}
