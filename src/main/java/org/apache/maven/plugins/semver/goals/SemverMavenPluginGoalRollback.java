package org.apache.maven.plugins.semver.goals;

import jdk.nashorn.internal.runtime.Version;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.semver.SemverMavenPlugin;
import org.apache.maven.plugins.semver.exceptions.SemverException;
import org.apache.maven.plugins.semver.factories.FileWriterFactory;
import org.apache.maven.plugins.semver.providers.PomProvider;
import org.apache.maven.plugins.semver.providers.RepositoryProvider;
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

    LOG.info("Semver-goal                        : ROLLBACK");
    LOG.info("Run-mode                           : " + getConfiguration().getRunMode());
    LOG.info("Version from POM                   : " + version);
    LOG.info("SCM-connection                     : " + scmConnection);
    LOG.info("SCM-root                           : " + scmRoot);
    LOG.info(FUNCTION_LINE_BREAK);

    if(getConfiguration().getRunMode() == RUNMODE.NATIVE || getConfiguration().getRunMode() == RUNMODE.NATIVE_BRANCH) {

      LOG.info("Perform a rollback for version     : [ " + version + " ]");
      LOG.info(SemverMavenPlugin.MOJO_LINE_BREAK);
      if(FileWriterFactory.canRollBack(LOG)) {
        if(!getVersionProvider().isRemoteVersionCorrupt(version)) {
          FileWriterFactory.rollbackPom(LOG);
          LOG.info(" * Commit old pom.xml");
          getRepositoryProvider().commit("[semver-maven-plugin] rollback version  : [ " + version + " ]");
          LOG.info(" * Push old pom.xml");
          getRepositoryProvider().push();
          LOG.info(" * Delete SCM-tag                  : [ " + version + " ]");
          getRepositoryProvider().deleteTag(version);
          LOG.info(" * Delete remote SCM-tag           : [ " + version + " ]");
          getRepositoryProvider().pushTag();
          LOG.info(SemverMavenPlugin.MOJO_LINE_BREAK);
          FileWriterFactory.removeBackupSemverPom(LOG);
        }
      }
    } else {
      LOG.error("Ÿou have configured a wrong RUN_MODE ( " + getConfiguration().getRunMode() + " )");
      LOG.error("Ÿou have to use release:rollback to revert the version update");
    }


  }

}
