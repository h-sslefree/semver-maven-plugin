package org.apache.maven.plugins.semver.providers;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

import java.util.Map;
import javax.inject.Inject;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugins.semver.SemverMavenPlugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;

@Component(role = PomProvider.class)
public class PomProviderImpl implements PomProvider {

  @Requirement private Logger LOG;
  @Requirement private RepositoryProvider repositoryProvider;
  @Requirement private BuildPluginManager pluginManager;

  @Requirement private MavenProject project;
  @Requirement private MavenSession session;

  /**
   * <h>POM-provider</h>
   *
   * <p>This class provides pom.xml.
   */
  @Inject
  public PomProviderImpl() {}

  @Override
  public void createReleasePom(Map<VersionProvider.FINAL_VERSION, String> finalVersions) {
    LOG.info("Create release-pom");
    LOG.info(SemverMavenPlugin.MOJO_LINE_BREAK);
    MavenProject releasePom = project;
    String scmTag = finalVersions.get(VersionProvider.FINAL_VERSION.SCM);
    releasePom.getScm().setTag(scmTag);
    updateVersion(releasePom, finalVersions.get(VersionProvider.FINAL_VERSION.RELEASE));
    releasePom.setVersion(scmTag);
    String commitMessage =
        "[semver-maven-plugin] create new release-pom for tag : [ " + scmTag + " ]";
    LOG.info(SemverMavenPlugin.MOJO_LINE_BREAK);
    LOG.info("Commit new release-pom             : {}", commitMessage);
    repositoryProvider.commit(commitMessage);
    LOG.info("Push new release-pom to remote     : {}", commitMessage);
    repositoryProvider.push();
    LOG.info("Create local scm-tag               : [ {} ]", scmTag);
    repositoryProvider.createTag(scmTag);
    LOG.info("Create remote scm-tag              : [ {} ]", scmTag);
    repositoryProvider.pushTag();
    LOG.info(SemverMavenPlugin.FUNCTION_LINE_BREAK);
  }

  @Override
  public void createNextDevelopmentPom(String developmentVersion) {
    LOG.info("Create next development-pom");
    LOG.info(SemverMavenPlugin.MOJO_LINE_BREAK);
    MavenProject nextDevelopementPom = project;
    nextDevelopementPom.getScm().setTag("");
    updateVersion(nextDevelopementPom, developmentVersion);
    String commitMessage =
        "[semver-maven-plugin] create next dev-pom version : [ " + developmentVersion + " ]";
    LOG.info(SemverMavenPlugin.MOJO_LINE_BREAK);
    LOG.info("Commit next dev-pom                : {}", commitMessage);
    repositoryProvider.commit(commitMessage);
    LOG.info("Push next dev-pom to remote        : {}", commitMessage);
    repositoryProvider.push();
    LOG.info(SemverMavenPlugin.FUNCTION_LINE_BREAK);
  }

  private void checkSnapshotVersions(String version) {
    try {
      executeMojo(
          plugin(groupId("org.codehaus.mojo"), artifactId("versions-maven-plugin"), version("2.3")),
          goal("set"),
          configuration(
              element(name("generateBackupPoms"), "false"), element(name("newVersion"), version)),
          executionEnvironment(project, session, pluginManager));
    } catch (Exception err) {
      LOG.error(err.getMessage());
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
          executionEnvironment(project, session, pluginManager));
    } catch (Exception err) {
      LOG.error(err.getMessage());
    }
  }
}
