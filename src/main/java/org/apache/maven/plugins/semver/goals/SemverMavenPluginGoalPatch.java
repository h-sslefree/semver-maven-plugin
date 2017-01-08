package org.apache.maven.plugins.semver.goals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.semver.SemverMavenPlugin;
import org.apache.maven.plugins.semver.exceptions.SemverException;
import org.eclipse.jgit.api.errors.GitAPIException;

/**
 * 
 * @author sido
 *
 */
@Mojo(name = "patch")
public class SemverMavenPluginGoalPatch extends SemverMavenPlugin {

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    
    String version = project.getVersion();
    String scmConnection = project.getScm().getConnection();
    File scmRoot = project.getBasedir();
    
    log.info("Semver-goal                       : PATCH");
    log.info("Run-mode                          : " + getConfiguration().getRunMode());
    log.info("Version from POM                  : " + version);
    log.info("SCM-connection                    : " + scmConnection);
    log.info("SCM-root                          : " + scmRoot);
    log.info(MOJO_LINE_BREAK);

    List<String> versions = new ArrayList<String>();
    try {
      versions = determineVersions(version);
    } catch (Exception e) {
      log.error(e);
    }
    
    if (getConfiguration().getRunMode() == RUNMODE.RELEASE) {
      createReleaseProperties(versions.get(VERSION.DEVELOPMENT.getIndex()), versions.get(VERSION.RELEASE.getIndex()), versions.get(VERSION.RELEASE.getIndex()));
    } else if (getConfiguration().getRunMode() == RUNMODE.NATIVE) {
      createReleaseNative(versions.get(VERSION.DEVELOPMENT.getIndex()), versions.get(VERSION.RELEASE.getIndex()));
    } else if(getConfiguration().getRunMode() == RUNMODE.RELEASE_BRANCH || getConfiguration().getRunMode() == RUNMODE.RELEASE_BRANCH_HOSEE) {
      createReleaseBranch(versions.get(VERSION.DEVELOPMENT.getIndex()), Integer.valueOf(versions.get(VERSION.MAJOR.getIndex())), Integer.valueOf(versions.get(VERSION.MINOR.getIndex())), Integer.valueOf(versions.get(VERSION.PATCH.getIndex())));
    }
    
  }
  
  private List<String> determineVersions(String version) throws SemverException, IOException, GitAPIException {

    List<String> versions = new ArrayList<String>();

    int majorVersion = 0;
    int minorVersion = 0;
    int patchVersion = 1;

    String[] rawVersion = version.split("\\.");
    if (rawVersion.length > 0 && rawVersion.length == 3) {
      log.debug("Set version-variables from POM.xml");
      log.debug(MOJO_LINE_BREAK);
      majorVersion = Integer.valueOf(rawVersion[0]);
      minorVersion = Integer.valueOf(rawVersion[1]);
      patchVersion = Integer.valueOf(rawVersion[2].substring(0, rawVersion[2].lastIndexOf('-')));
    } else {
      log.error("Unrecognized version-pattern");
      log.error("Semver plugin is terminating");
      throw new SemverException("Unrecognized version-pattern", "Could not parse version from POM.xml because of not parseble version-pattern");
    }

    log.debug("MAJOR-version                    : " + majorVersion);
    log.debug("MINOR-version                    : " + minorVersion);
    log.debug("PATCH-version                    : " + patchVersion);
    log.debug(MOJO_LINE_BREAK);

    patchVersion = patchVersion + 1;

    String developmentVersion = majorVersion + "." + minorVersion + "." + patchVersion + "-SNAPSHOT";
    String releaseVersion = majorVersion + "." + minorVersion + "." + patchVersion;
    String scmVersion = majorVersion + "." + minorVersion + "." + patchVersion;
    if(getConfiguration().getRunMode() == RUNMODE.RELEASE_BRANCH || getConfiguration().getRunMode() == RUNMODE.RELEASE_BRANCH_HOSEE) {
      log.info("Determine new versions for branch : " + getConfiguration().getBranchVersion());
    }
    log.info("New DEVELOPMENT-version           : " + developmentVersion);
    log.info("New GIT-version                   : " + scmVersion);
    log.info("New RELEASE-version               : " + releaseVersion);
    log.info(MOJO_LINE_BREAK);

    versions.add(VERSION.DEVELOPMENT.getIndex(), developmentVersion);
    versions.add(VERSION.RELEASE.getIndex(), releaseVersion);
    versions.add(VERSION.MAJOR.getIndex(), String.valueOf(majorVersion));
    versions.add(VERSION.MINOR.getIndex(), String.valueOf(minorVersion));
    versions.add(VERSION.PATCH.getIndex(), String.valueOf(patchVersion));

    cleanupGitLocalAndRemoteTags(scmVersion);
    
    return versions;
  }
  
  
}
