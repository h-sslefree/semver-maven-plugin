package org.apache.maven.plugins.semver.goals;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.semver.SemverMavenPlugin;
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

  /**
   * @throws MojoExecutionException
   * @throws MojoFailureException
   */
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {

    String version = project.getVersion();
    String scmConnection = project.getScm().getConnection();
    File scmRoot = project.getBasedir();

    LOG.info("Semver-goal                       : CLEANUP-GIT-TAGS");
    LOG.info("Run-mode                          : " + getConfiguration().getRunMode());
    LOG.info("Version from POM                  : " + version);
    LOG.info("SCM-connection                    : " + scmConnection);
    LOG.info("SCM-root                          : " + scmRoot);
    LOG.info(FUNCTION_LINE_BREAK);

    try {
      cleanupGitRemoteTags(scmConnection, scmRoot);
    } catch (IOException e) {
      LOG.error("Error when determining config", e);
    } catch (GitAPIException e) {
      LOG.error("Error when determining GIT-repo", e);
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
    LOG.info("Determine local and remote SCM-tags for SCM-repo");
    LOG.info(MOJO_LINE_BREAK);
    getRepositoryProvider().pull();
    List<Ref> refs = getRepositoryProvider().getLocalTags();
    if (refs.isEmpty()) {
      boolean found = false;
      for (Ref ref : refs) {
        if (ref.getName().contains(preparedReleaseTag)) {
          found = true;
          LOG.info("Delete local SCM-tag                 : " + ref.getName().substring(10));
          getRepositoryProvider().deleteTag(ref.getName());
          LOG.info("Delete remote SCM-tag                : " + ref.getName().substring(10));
          getRepositoryProvider().pushTag();
        }
      }
      if (!found) {
        LOG.info("No local or remote prepared SCM-tags found");
      }
    } else {
      LOG.info("No local or remote prepared SCM-tags found");
    }

    getRepositoryProvider().closeRepository();
    LOG.info(MOJO_LINE_BREAK);
  }
}
