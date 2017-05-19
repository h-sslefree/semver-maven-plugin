package org.apache.maven.plugins.semver.goals;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.semver.SemverMavenPlugin;
import org.apache.maven.plugins.semver.exceptions.SemverException;
import org.apache.maven.plugins.semver.factories.FileWriterFactory;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * <p>Rollback failed PATCH, MINOR or MAJOR.</p>
 * <p>Delete local tags and revert old pom.xml.</p>
 *
 * @author sido
 */
@Mojo(name = "rollback")
public class SemverMavenPluginGoalRollback extends SemverMavenPlugin {

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {

    initializeProviders();

    String version = project.getVersion();
    String scmConnection = project.getScm().getConnection();
    File scmRoot = project.getBasedir();

    LOG.info("Semver-goal                       : ROLLBACK");
    LOG.info("Run-mode                          : " + getConfiguration().getRunMode());
    LOG.info("Version from POM                  : " + version);
    LOG.info("SCM-connection                    : " + scmConnection);
    LOG.info("SCM-root                          : " + scmRoot);
    LOG.info(FUNCTION_LINE_BREAK);

    if(getConfiguration().getRunMode() == RUNMODE.NATIVE || getConfiguration().getRunMode() == RUNMODE.NATIVE_BRANCH) {
      LOG.info("Rollbacked version: [" + version + "]");

      FileWriterFactory.rollbackPom(LOG);
      FileWriterFactory.removeBackupSemverPom(LOG);
    } else {
      LOG.error("Ÿou have configured a wrong RUN_MODE ( " + getConfiguration().getRunMode() + " )");
      LOG.error("Ÿou have to use release:rollback to revert the version update");
    }


  }

}
