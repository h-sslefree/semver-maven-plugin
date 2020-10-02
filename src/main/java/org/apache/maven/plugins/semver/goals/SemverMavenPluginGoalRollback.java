package org.apache.maven.plugins.semver.goals;

import static java.lang.String.format;
import static org.apache.maven.plugins.semver.SemverMavenPlugin.MOJO_LINE_BREAK;
import static org.apache.maven.plugins.semver.goals.SemverGoal.SEMVER_GOAL.ROLLBACK;
import static org.apache.maven.plugins.semver.runmodes.RunMode.RUN_MODE.*;

import java.io.File;
import javax.inject.Inject;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.semver.SemverMavenPlugin;
import org.apache.maven.plugins.semver.factories.FileWriterFactory;
import org.apache.maven.plugins.semver.providers.BranchProvider;
import org.apache.maven.plugins.semver.providers.PomProvider;
import org.apache.maven.plugins.semver.providers.RepositoryProvider;
import org.apache.maven.plugins.semver.providers.VersionProvider;

/**
 * Rollback failed PATCH, MINOR or MAJOR.
 *
 * <p>Delete local tags and revert old pom.xml.
 *
 * @author sido
 */
@Mojo(name = "rollback")
public class SemverMavenPluginGoalRollback extends SemverMavenPlugin {

  @Inject
  public SemverMavenPluginGoalRollback(
      VersionProvider versionProvider,
      PomProvider pomProvider,
      RepositoryProvider repositoryProvider,
      BranchProvider branchProvider) {
    super(versionProvider, pomProvider, repositoryProvider, branchProvider);
  }

  @Override
  public void execute() {
    String version = mavenProject.getVersion();
    String scmConnection = mavenProject.getScm().getConnection();
    File scmRoot = mavenProject.getBasedir();
    getRepositoryProvider()
        .initialize(
            scmRoot,
            scmConnection,
            getConfiguration().getScmUsername(),
            getConfiguration().getScmPassword());

    logger.info(FUNCTION_LINE_BREAK);
    logger.info("Semver-goal                        : {}", ROLLBACK.getDescription());
    logger.info("Run-mode                           : {}", getConfiguration().getRunMode());
    logger.info("Version from POM                   : [ {} ]", version);
    logger.info("SCM-connection                     : {}", scmConnection);
    logger.info("SCM-root                           : {}", scmRoot);
    logger.info(FUNCTION_LINE_BREAK);

    if (getConfiguration().getRunMode() == NATIVE
        || getConfiguration().getRunMode() == NATIVE_BRANCH
        || getConfiguration().getRunMode() == NATIVE_BRANCH_RPM) {

      logger.info("Perform a rollback for version     : [ {} ]", version);
      logger.info(MOJO_LINE_BREAK);
      if (FileWriterFactory.canRollBack()) {
        if (getConfiguration().checkRemoteVersionTags()) {
          if (!getRepositoryProvider().isRemoteVersionCorrupt(version)) {
            executeRollback(version);
          } else {
            logger.error("");
            logger.error("Please check your repository state");
            Runtime.getRuntime().exit(1);
          }
        } else {
          executeRollback(version);
        }
      }
    } else {
      logger.error("");
      logger.error(
          format("Ÿou have configured a wrong RUN_MODE ( %s )", getConfiguration().getRunMode()));
      logger.error("Ÿou have to use release:rollback to revert the version update");
    }
  }

  private void executeRollback(String version) {
    FileWriterFactory.rollbackPom();
    logger.info(" * Commit old pom.xml");
    getRepositoryProvider().commit("[semver-maven-plugin] rollback version  : [ " + version + " ]");
    logger.info(" * Push old pom.xml");
    getRepositoryProvider().push();
    logger.info(" * Delete SCM-tag                  : [ {} ]", version);
    getRepositoryProvider().deleteTag(version);
    logger.info(" * Delete remote SCM-tag           : [ {} ]", version);
    getRepositoryProvider().pushTag();
    logger.info(MOJO_LINE_BREAK);
    FileWriterFactory.removeBackupSemverPom();
  }
}
