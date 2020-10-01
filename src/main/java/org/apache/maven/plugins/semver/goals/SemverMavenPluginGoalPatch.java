package org.apache.maven.plugins.semver.goals;

import java.io.File;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.semver.SemverMavenPlugin;

/**
 *
 *
 * <h1>Determine PATCH version for MAVEN-project.</h1>
 *
 * <p>This advances the tag of the project and the pom.xml version.
 *
 * <p>Example:
 *
 * <pre>{@code
 * <version>x.x.1</version>
 * to
 * <version>x.x.2</version>
 * }</pre>
 *
 * <p>Run the test-phase when this goal is executed.
 *
 * @author sido
 */
@Mojo(name = "patch")
@Execute(phase = LifecyclePhase.TEST)
public class SemverMavenPluginGoalPatch extends SemverMavenPlugin {

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {

    String pomVersion = project.getVersion();
    if (project.getScm() == null) {
      LOG.error("No source control information available");
      Runtime.getRuntime().exit(1);
    }
    String scmConnection = project.getScm().getConnection();

    File scmRoot = project.getBasedir();
    getRepositoryProvider()
        .initialize(
            scmRoot,
            scmConnection,
            getConfiguration().getScmUsername(),
            getConfiguration().getScmPassword());

    LOG.info(FUNCTION_LINE_BREAK);
    LOG.info(
        "Semver-goal                        : {}", SemverGoal.SEMVER_GOAL.PATCH.getDescription());
    LOG.info("Run-mode                           : {}", getConfiguration().getRunMode());
    LOG.info("Version from POM                   : [ {} ]", pomVersion);
    LOG.info("SCM-connection                     : {}", scmConnection);
    LOG.info("SCM-root                           : {}", scmRoot);
    LOG.info(FUNCTION_LINE_BREAK);

    try {
      runModeImpl.execute(SemverGoal.SEMVER_GOAL.PATCH, getConfiguration(), pomVersion);
    } catch (Exception e) {
      LOG.error(e.getMessage());
    }
  }
}
