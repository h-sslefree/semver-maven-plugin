package org.apache.maven.plugins.semver.goals;

import java.io.File;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.semver.SemverMavenPlugin;
import org.apache.maven.plugins.semver.factories.FileWriterFactory;
import org.apache.maven.plugins.semver.runmodes.RunMode;

import static java.lang.String.format;

/**
 * Rollback failed PATCH, MINOR or MAJOR.
 *
 * <p>Delete local tags and revert old pom.xml.
 *
 * @author sido
 */
@Mojo(name = "rollback")
public class SemverMavenPluginGoalRollback extends SemverMavenPlugin {

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {

    String version = project.getVersion();
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
        "Semver-goal                        : {}",
        SemverGoal.SEMVER_GOAL.ROLLBACK.getDescription());
    LOG.info("Run-mode                           : {}", getConfiguration().getRunMode());
    LOG.info("Version from POM                   : [ {} ]", version);
    LOG.info("SCM-connection                     : {}", scmConnection);
    LOG.info("SCM-root                           : {}", scmRoot);
    LOG.info(FUNCTION_LINE_BREAK);

    if (getConfiguration().getRunMode() == RunMode.RUN_MODE.NATIVE
        || getConfiguration().getRunMode() == RunMode.RUN_MODE.NATIVE_BRANCH
        || getConfiguration().getRunMode() == RunMode.RUN_MODE.NATIVE_BRANCH_RPM) {

      LOG.info("Perform a rollback for version     : [ {} ]", version);
      LOG.info(SemverMavenPlugin.MOJO_LINE_BREAK);
      if (FileWriterFactory.canRollBack()) {
        if (getConfiguration().checkRemoteVersionTags()) {
          if (!getRepositoryProvider().isRemoteVersionCorrupt(version)) {
            executeRollback(version);
          } else {
            LOG.error("");
            LOG.error("Please check your repository state");
            Runtime.getRuntime().exit(1);
          }
        } else {
          executeRollback(version);
        }
      }
    } else {
      LOG.error("");
      LOG.error(format("Ÿou have configured a wrong RUN_MODE ( %s )", getConfiguration().getRunMode()));
      LOG.error("Ÿou have to use release:rollback to revert the version update");
    }
  }

  private void executeRollback(String version) {
    FileWriterFactory.rollbackPom();
    LOG.info(" * Commit old pom.xml");
    getRepositoryProvider().commit("[semver-maven-plugin] rollback version  : [ " + version + " ]");
    LOG.info(" * Push old pom.xml");
    getRepositoryProvider().push();
    LOG.info(" * Delete SCM-tag                  : [ {} ]", version);
    getRepositoryProvider().deleteTag(version);
    LOG.info(" * Delete remote SCM-tag           : [ {} ]", version);
    getRepositoryProvider().pushTag();
    LOG.info(SemverMavenPlugin.MOJO_LINE_BREAK);
    FileWriterFactory.removeBackupSemverPom();
  }
}
