package org.apache.maven.plugins.semver.goals;

import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.inject.Inject;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.semver.SemverMavenPlugin;
import org.apache.maven.plugins.semver.providers.BranchProvider;
import org.apache.maven.plugins.semver.providers.PomProvider;
import org.apache.maven.plugins.semver.providers.RepositoryProvider;
import org.apache.maven.plugins.semver.providers.VersionProvider;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;

/**
 * used to be a goal that was used before a build on a BUILD-server (HUDSON).
 *
 * <p>Can be phased out when the BUILD-server jobs are obsolete.
 *
 * @author sido
 * @deprecated
 */
@Deprecated
@Mojo(name = "cleanup-git-tags")
public class SemverMavenPluginGoalCleanupGitTags extends SemverMavenPlugin {

  @Inject
  public SemverMavenPluginGoalCleanupGitTags(
      VersionProvider versionProvider,
      PomProvider pomProvider,
      RepositoryProvider repositoryProvider,
      BranchProvider branchProvider) {
    super(versionProvider, pomProvider, repositoryProvider, branchProvider);
  }

  /**
   * @throws MojoExecutionException
   * @throws MojoFailureException
   */
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {

    String version = mavenProject.getVersion();
    String scmConnection = mavenProject.getScm().getConnection();
    File scmRoot = mavenProject.getBasedir();

    logger.info("Semver-goal                       : CLEANUP-GIT-TAGS");
    logger.info("Run-mode                          : " + getConfiguration().getRunMode());
    logger.info("Version from POM                  : " + version);
    logger.info("SCM-connection                    : " + scmConnection);
    logger.info("SCM-root                          : " + scmRoot);
    logger.info(FUNCTION_LINE_BREAK);

    try {
      cleanupGitRemoteTags(scmConnection, scmRoot);
    } catch (IOException e) {
      logger.error("Error when determining config", e);
    } catch (GitAPIException e) {
      logger.error("Error when determining GIT-repo", e);
    }
  }

  /**
   * Cleanup lost GIT-tags before making a release on BUILD-server (for example HUDSON)
   *
   * @param scmConnection
   * @param scmRoot
   * @throws IOException
   * @throws GitAPIException
   */
  private void cleanupGitRemoteTags(String scmConnection, File scmRoot)
      throws IOException, GitAPIException {
    logger.info("Determine local and remote SCM-tags for SCM-repo");
    logger.info(MOJO_LINE_BREAK);
    getRepositoryProvider().pull();
    List<Ref> refs = getRepositoryProvider().getLocalTags();
    if (refs.isEmpty()) {
      boolean found = false;
      for (Ref ref : refs) {
        if (ref.getName().contains(preparedReleaseTag)) {
          found = true;
          logger.info("Delete local SCM-tag                 : " + ref.getName().substring(10));
          getRepositoryProvider().deleteTag(ref.getName());
          logger.info("Delete remote SCM-tag                : " + ref.getName().substring(10));
          getRepositoryProvider().pushTag();
        }
      }
      if (!found) {
        logger.info("No local or remote prepared SCM-tags found");
      }
    } else {
      logger.info("No local or remote prepared SCM-tags found");
    }

    getRepositoryProvider().closeRepository();
    logger.info(MOJO_LINE_BREAK);
  }
}
