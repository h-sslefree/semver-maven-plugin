package org.apache.maven.plugins.semver.goals;

import java.io.File;
import javax.inject.Inject;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.semver.SemverMavenPlugin;
import org.apache.maven.plugins.semver.providers.BranchProvider;
import org.apache.maven.plugins.semver.providers.PomProvider;
import org.apache.maven.plugins.semver.providers.RepositoryProvider;
import org.apache.maven.plugins.semver.providers.VersionProvider;

/**
 *
 *
 * <h1>Determine MAJOR version for MAVEN-project.</h1>
 *
 * <p>This advances the tag of the project and the pom.xml version.
 *
 * <p>Example:
 *
 * <pre>{@code
 * <version>1.x.x</version>
 * to
 * <version>2.x.x</version>
 * }</pre>
 *
 * <p>Run the test-phase when this goal is executed.
 *
 * @author sido
 */
@Mojo(name = "major")
@Execute(phase = LifecyclePhase.TEST)
public class SemverMavenPluginGoalMajor extends SemverMavenPlugin {

  @Inject
  public SemverMavenPluginGoalMajor(
      VersionProvider versionProvider,
      PomProvider pomProvider,
      RepositoryProvider repositoryProvider,
      BranchProvider branchProvider) {
    super(versionProvider, pomProvider, repositoryProvider, branchProvider);
  }

  @Override
  public void execute() {

    String pomVersion = mavenProject.getVersion();
    String scmConnection = mavenProject.getScm().getConnection();
    File scmRoot = mavenProject.getBasedir();
    getRepositoryProvider()
        .initialize(
            scmRoot,
            scmConnection,
            getConfiguration().getScmUsername(),
            getConfiguration().getScmPassword());

    logger.info(FUNCTION_LINE_BREAK);
    logger.info(
        "Semver-goal                        : {}", SemverGoal.SEMVER_GOAL.MAJOR.getDescription());
    logger.info("Run-mode                           : {}", getConfiguration().getRunMode());
    logger.info("Version from POM                   : [ {} ]", pomVersion);
    logger.info("SCM-connection                     : {}", scmConnection);
    logger.info("SCM-root                           : {}", scmRoot);
    logger.info(FUNCTION_LINE_BREAK);

    try {
      runModeImpl.execute(SemverGoal.SEMVER_GOAL.MAJOR, getConfiguration(), pomVersion);
    } catch (Exception e) {
      logger.error(e.getMessage());
    }
  }
}
