package org.apache.maven.plugins.semver.providers;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.semver.SemverMavenPlugin;
import org.apache.maven.project.MavenProject;

import java.util.Map;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

/**
 *
 * <h>PomProvider</h>
 * <p></p>
 *
 * @author sido
 */
public class PomProvider {

  private Log LOG;

  private RepositoryProvider repositoryProvider;

  private MavenProject project;

  private MavenSession session;

  private BuildPluginManager pluginManager;

  /**
   *
   * <h>POM-provider</h>
   * <p>This class provides pom.xml.</p>
   *
   * @param LOG logging is only toplevel initialized so has to be passed as parameter
   * @param repositoryProvider initialized {@link RepositoryProvider}
   * @param project {@link MavenProject} from Mojo
   */
  public PomProvider(Log LOG, RepositoryProvider repositoryProvider, MavenProject project, MavenSession session, BuildPluginManager pluginManager) {
    this.LOG = LOG;
    this.repositoryProvider = repositoryProvider;
    this.project = project;
    this.session = session;
    this.pluginManager = pluginManager;
  }

  /**
   *
   * <h>Create release-pom</h>
   * <p>Create a release-pom for the build.</p>
   *
   * @param finalVersions final versions from the plugin-goals
   */
  public void createReleasePom(Map<VersionProvider.FINAL_VERSION, String> finalVersions) {
    LOG.info("Create release-pom");
    LOG.info(SemverMavenPlugin.MOJO_LINE_BREAK);
    MavenProject releasePom = project;
    String scmTag = finalVersions.get(VersionProvider.FINAL_VERSION.SCM);
    releasePom.getScm().setTag(scmTag);
    updateVersion(releasePom, finalVersions.get(VersionProvider.FINAL_VERSION.RELEASE));
    releasePom.setVersion(scmTag);
    String commitMessage = "[semver-maven-plugin] create new release-pom for tag : [ " + scmTag + " ]";
    LOG.info(SemverMavenPlugin.MOJO_LINE_BREAK);
    LOG.info("Commit new release-pom             : " + commitMessage);
    repositoryProvider.commit(commitMessage);
    LOG.info("Push new release-pom to remote     : " + commitMessage);
    repositoryProvider.push();
    LOG.info("Create local scm-tag               : " + scmTag);
    repositoryProvider.createTag(scmTag);
    LOG.info("Create remote scm-tag              : " + scmTag);
    repositoryProvider.pushTag();
    LOG.info(SemverMavenPlugin.FUNCTION_LINE_BREAK);
  }

  /**
   *
   * <h>Create development-pom</h>
   * <p>Create next development-pom for this project</p>
   *
   * @param developmentVersion developmentVersion
   */
  public void createNextDevelopmentPom(String developmentVersion) {
    LOG.info("Create next development-pom");
    LOG.info(SemverMavenPlugin.MOJO_LINE_BREAK);
    MavenProject nextDevelopementPom = project;
    nextDevelopementPom.getScm().setTag("");
    updateVersion(nextDevelopementPom, developmentVersion);
    String commitMessage = "[semver-maven-plugin] create next dev-pom version : [ " + developmentVersion + " ]";
    LOG.info(SemverMavenPlugin.MOJO_LINE_BREAK);
    LOG.info("Commit next dev-pom                : " + commitMessage);
    repositoryProvider.commit(commitMessage);
    LOG.info("Push next dev-pom to remote        : " + commitMessage);
    repositoryProvider.push();
    LOG.info(SemverMavenPlugin.FUNCTION_LINE_BREAK);
  }

  private void checkSnapshotVersions(MavenProject project, String version) {
    try {
      executeMojo(
              plugin(
                      groupId("org.codehaus.mojo"),
                      artifactId("versions-maven-plugin"),
                      version("2.3")
              ),
              goal("set"),
              configuration(
                      element(name("generateBackupPoms"), "false"),
                      element(name("newVersion"), version)
              ),
              executionEnvironment(
                      project,
                      session,
                      pluginManager
              ));
    } catch (Exception err) {
      LOG.error(err);
    }
  }

  /**
   *
   * <p>Use the versions plugin to advance the pom.xml's.</p>
   *
   * @param project
   * @param version
   */
  private void updateVersion(MavenProject project, String version) {
    try {
      executeMojo(
              plugin(
                      groupId("org.codehaus.mojo"),
                      artifactId("versions-maven-plugin"),
                      version("2.3")
              ),
              goal("set"),
              configuration(
                      element(name("generateBackupPoms"), "false"),
                      element(name("newVersion"), version)
              ),
              executionEnvironment(
                      project,
                      session,
                      pluginManager
              ));
    } catch (Exception err) {
      LOG.error(err);
    }
  }

}
