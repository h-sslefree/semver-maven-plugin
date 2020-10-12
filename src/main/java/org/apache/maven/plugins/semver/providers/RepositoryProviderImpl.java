package org.apache.maven.plugins.semver.providers;

import static java.lang.String.format;
import static org.apache.maven.plugins.semver.SemverMavenPlugin.FUNCTION_LINE_BREAK;
import static org.apache.maven.plugins.semver.SemverMavenPlugin.MOJO_LINE_BREAK;
import static org.apache.maven.plugins.semver.utils.SemverConsole.readLine;
import static org.apache.maven.plugins.semver.utils.SemverConsole.readPassword;

import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.inject.Named;
import javax.inject.Singleton;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
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
    repository = initializeRepository(baseDir);
    provider = initializeCredentialsProvider(scmUrl, configScmUsername, configScmPassword);
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
   */
  private Git initializeRepository(File baseDir) {
    Git initRepository = null;
    logger.info(FUNCTION_LINE_BREAK);
    logger.info("Initializing SCM-repository");
    FileRepositoryBuilder repoBuilder = new FileRepositoryBuilder();
    repoBuilder.addCeilingDirectory(baseDir);
    repoBuilder.findGitDir(baseDir);
    Repository repo;
    try {
      repo = repoBuilder.build();
      initRepository = new Git(repo);
      logger.info(" * SCM-repository is initialized");
    } catch (IOException e) {
      handleException(e, true);
    }
    return initRepository;
  }

  /**
   * Initialize credentials provider to access remote SCM repository.
   *
   * @return {@link CredentialsProvider} initialized credentialsProvider
   */
  private CredentialsProvider initializeCredentialsProvider(
      String scmUrl, String configScmUserName, String configScmPassword) {
    logger.info("Initializing SCM-credentials provider");
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
    logger.info(" * SCM-credentials provider is initialized");
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
      handleException(err, false);
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
    EnumMap<CREDENTIALS, String> credentials = new EnumMap<>(CREDENTIALS.class);
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
          credentials.put(CREDENTIALS.USERNAME, readLine(messageUsername));
        } else {
          credentials.put(CREDENTIALS.USERNAME, readLine(messageUsername, scmDefaultUsername));
        }
        credentials.put(CREDENTIALS.PASSWORD, readPassword(messagePassword));
      } catch (Exception err) {
        handleException(err, true);
      }
    } else {
      credentials.put(CREDENTIALS.USERNAME, configScmUserName);
      credentials.put(CREDENTIALS.PASSWORD, configScmPassword);
    }
    return credentials;
  }

  @Override
  public void pull() {
    try {
      repository.pull().setCredentialsProvider(provider).call();
    } catch (GitAPIException err) {
      handleException(err, true);
    }
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
      handleException(err, true);
    }
    return isRemoteDifferent;
  }

  @Override
  public String getCurrentBranch() {
    String currentBranch = "";
    try {
      currentBranch = repository.getRepository().getBranch();
    } catch (IOException err) {
      handleException(err, true);
    }
    return currentBranch;
  }

  @Override
  public List<Ref> getLocalTags() {
    List<Ref> tags = new ArrayList<>();
    try {
      tags = repository.tagList().call();
    } catch (GitAPIException err) {
      handleException(err, true);
    }
    return tags;
  }

  @Override
  public Map<String, Ref> getRemoteTags() {
    Map<String, Ref> tags = new HashMap<>();
    try {
      tags = repository.pull().setCredentialsProvider(provider).getRepository().getTags();
    } catch (Exception err) {
      handleException(err, true);
    }
    return tags;
  }

  @Override
  public void createTag(String tag) {
    try {
      deleteTag(tag);
      repository.tag().setName(tag).call();
    } catch (GitAPIException err) {
      handleException(err, true);
    }
  }

  @Override
  public void deleteTag(String tag) {
    try {
      repository.tagDelete().setTags(tag).call();
    } catch (GitAPIException err) {
      handleException(err, true);
    }
  }

  @Override
  public void commit(String message) {
    try {
      repository.commit().setAll(true).setMessage(message).call();
    } catch (GitAPIException err) {
      handleException(err, true);
    }
  }

  @Override
  public void push() {
    try {
      repository.push().setPushAll().setRemote(BASE_BRANCH).setCredentialsProvider(provider).call();
    } catch (GitAPIException err) {
      handleException(err, true);
    }
  }

  /**
   * Log the exception.
   *
   * @param err {@link GitAPIException}
   * @param stop
   */
  private void handleException(Exception err, boolean stop) {
    logger.error(err.getMessage());
    logger.error("");
    logger.error("Please check your SCM-credentials to fix this issue");
    logger.error("Please run semver:rollback to return to initial state");
    if (stop) {
      Runtime.getRuntime().exit(1);
    }
  }

  @Override
  public void pushTag() {
    try {
      repository
          .push()
          .setPushTags()
          .setRemote(BASE_BRANCH)
          .setCredentialsProvider(provider)
          .call();
    } catch (GitAPIException err) {
      handleException(err, true);
    }
  }

  @Override
  public void closeRepository() {
    repository.close();
  }

  @Override
  public boolean isChanged() {
    boolean isChanged = false;
    logger.info("Check for local or remote changes");
    logger.info(MOJO_LINE_BREAK);
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
        } else {
          logger.info("Local changes                      : workingtree is clean");
          logger.info(FUNCTION_LINE_BREAK);
        }
      } catch (GitAPIException err) {
        logger.error(err.getMessage());
        isChanged = true;
      }
    }
    if (isChanged) {
      logger.info(FUNCTION_LINE_BREAK);
      logger.error("");
      logger.error("Semver-goal has failed");
      logger.error("There are uncomitted changes or the remote is ahead of local repository");
      logger.error("Please pull remote changes and/or commit and push the open changes");
    }
    return isChanged;
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
    logger.info(MOJO_LINE_BREAK);
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
    logger.info(FUNCTION_LINE_BREAK);
    return isRemoteVersionCorrupt;
  }
}
