package org.apache.maven.plugins.semver.providers;

import static java.util.Objects.requireNonNull;
import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugins.semver.SemverMavenPlugin;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named
@Singleton
public class PomProviderImpl implements PomProvider {

  private final Logger logger = LoggerFactory.getLogger(PomProviderImpl.class);

  private final BuildPluginManager buildPluginManager;
  private final MavenProject mavenProject;
  private final MavenSession mavenSession;
  private final RepositoryProvider repositoryProvider;

  @Inject
  public PomProviderImpl(
      MavenProject mavenProject,
      MavenSession mavenSession,
      BuildPluginManager buildPluginManager,
      RepositoryProvider repositoryProvider) {
    this.buildPluginManager = requireNonNull(buildPluginManager);
    this.mavenProject = requireNonNull(mavenProject);
    this.mavenSession = requireNonNull(mavenSession);
    this.repositoryProvider = requireNonNull(repositoryProvider);
  }

  @Override
  public void createReleasePom(Map<VersionProvider.FINAL_VERSION, String> finalVersions) {
    logger.info("Create release-pom");
    logger.info(SemverMavenPlugin.MOJO_LINE_BREAK);
    MavenProject releasePom = mavenProject;
    String scmTag = finalVersions.get(VersionProvider.FINAL_VERSION.SCM);
    releasePom.getScm().setTag(scmTag);
    updateVersion(releasePom, finalVersions.get(VersionProvider.FINAL_VERSION.RELEASE));
    releasePom.setVersion(scmTag);
    String commitMessage =
        "[semver-maven-plugin] create new release-pom for tag : [ " + scmTag + " ]";
    logger.info(SemverMavenPlugin.MOJO_LINE_BREAK);
    logger.info("Commit new release-pom             : {}", commitMessage);
    repositoryProvider.commit(commitMessage);
    logger.info("Push new release-pom to remote     : {}", commitMessage);
    repositoryProvider.push();
    logger.info("Create local scm-tag               : [ {} ]", scmTag);
    repositoryProvider.createTag(scmTag);
    logger.info("Create remote scm-tag              : [ {} ]", scmTag);
    repositoryProvider.pushTag();
    logger.info(SemverMavenPlugin.FUNCTION_LINE_BREAK);
  }

  @Override
  public void createNextDevelopmentPom(String developmentVersion) {
    logger.info("Create next development-pom");
    logger.info(SemverMavenPlugin.MOJO_LINE_BREAK);
    MavenProject nextDevelopementPom = mavenProject;
    nextDevelopementPom.getScm().setTag("");
    updateVersion(nextDevelopementPom, developmentVersion);
    String commitMessage =
        "[semver-maven-plugin] create next dev-pom version : [ " + developmentVersion + " ]";
    logger.info(SemverMavenPlugin.MOJO_LINE_BREAK);
    logger.info("Commit next dev-pom                : {}", commitMessage);
    repositoryProvider.commit(commitMessage);
    logger.info("Push next dev-pom to remote        : {}", commitMessage);
    repositoryProvider.push();
    logger.info(SemverMavenPlugin.FUNCTION_LINE_BREAK);
  }

  private void checkSnapshotVersions(String version) {
    try {
      executeMojo(
          plugin(groupId("org.codehaus.mojo"), artifactId("versions-maven-plugin"), version("2.3")),
          goal("set"),
          configuration(
              element(name("generateBackupPoms"), "false"), element(name("newVersion"), version)),
          executionEnvironment(mavenProject, mavenSession, buildPluginManager));
    } catch (Exception err) {
      logger.error(err.getMessage());
    }
  }

  /**
   * <h>Update pom-versions</h>
   *
   * <p>Makes use the versions-plugin to advance the pom.xml's.
   *
   * @param project {@link MavenProject} from parent Mojo
   * @param version the updated version
   */
  private void updateVersion(MavenProject project, String version) {
    try {
      executeMojo(
          plugin(groupId("org.codehaus.mojo"), artifactId("versions-maven-plugin"), version("2.3")),
          goal("set"),
          configuration(
              element(name("generateBackupPoms"), "false"), element(name("newVersion"), version)),
          executionEnvironment(project, mavenSession, buildPluginManager));
    } catch (Exception err) {
      logger.error(err.getMessage());
    }
  }
}
