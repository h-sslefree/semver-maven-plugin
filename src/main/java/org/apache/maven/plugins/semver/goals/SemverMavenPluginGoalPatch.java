package org.apache.maven.plugins.semver.goals;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.semver.SemverMavenPlugin;

import java.io.File;


/**
 * <h1>Determine PATCH version for MAVEN-project.</h1>
 * <p>This advances the tag of the project and the pom.xml version.</p>
 * <p>Example:</p>
 * <pre>
 *     <code>
 *          < version>x.x.1< /version>
 *          to
 *          < version>x.x.2< /version>
 *     </code>
 * </pre>
 * <p>Run the test-phase when this goal is executed.</p>
 *
 * @author sido
 */
@Mojo(name = "patch")
@Execute(phase = LifecyclePhase.TEST)
public class SemverMavenPluginGoalPatch extends SemverMavenPlugin {

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {

    String pomVersion = project.getVersion();
    String scmConnection = project.getScm().getConnection();
    File scmRoot = project.getBasedir();
    getRepositoryProvider().initialize(scmRoot, scmConnection, getConfiguration().getScmUsername(), getConfiguration().getScmPassword());

    LOG.info(FUNCTION_LINE_BREAK);
    LOG.info("Semver-goal                        : {}", SemverGoals.SEMVER_GOAL.PATCH.getDescription());
    LOG.info("Run-mode                           : {}", getConfiguration().getRunMode());
    LOG.info("Version from POM                   : [ {} ]", pomVersion);
    LOG.info("SCM-connection                     : {}", scmConnection);
    LOG.info("SCM-root                           : {}", scmRoot);
    LOG.info(FUNCTION_LINE_BREAK);

    try {
      if (!getVersionProvider().isVersionCorrupt(pomVersion) && !getRepositoryProvider().isChanged()) {
        runModeImpl.execute(SemverGoals.SEMVER_GOAL.PATCH, getConfiguration(), pomVersion);
      } else {
        Runtime.getRuntime().exit(1);
      }
    } catch (Exception e) {
      LOG.error(e.getMessage());
    }


  }


}
