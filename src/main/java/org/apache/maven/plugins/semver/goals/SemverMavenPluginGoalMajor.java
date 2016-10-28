package org.apache.maven.plugins.semver.goals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.semver.SemverMavenPlugin;

@Mojo(name = "major")
public class SemverMavenPluginGoalMajor extends SemverMavenPlugin {

  public void execute() throws MojoExecutionException, MojoFailureException {

    String version = project.getVersion();
    String scmConnection = project.getScm().getConnection();
    File scmRoot = project.getBasedir();

    log.info("Semver-goal                       : MAJOR");
    log.info("Run-mode                          : " + getConfiguration().getRunMode());
    log.info("Version from POM                  : " + version);
    log.info("SCM-connection                    : " + scmConnection);
    log.info("SCM-root                          : " + scmRoot);
    log.info("------------------------------------------------------------------------");

    List<String> versions = new ArrayList<String>();
    try {
      versions = determineVersions(version);
    } catch (Exception e) {
      log.error(e);
    }

    if (getConfiguration().getRunMode() == RUNMODE.RELEASE) {
      createReleaseProperties(versions.get(VERSION.DEVELOPMENT.getIndex()), versions.get(VERSION.RELEASE.getIndex()));
    } else if (getConfiguration().getRunMode() == RUNMODE.NATIVE) {
      createReleaseNative(versions.get(VERSION.DEVELOPMENT.getIndex()), versions.get(VERSION.RELEASE.getIndex()));
    } else if(getConfiguration().getRunMode() == RUNMODE.RELEASE_RPM) {
      createReleaseRpm(versions.get(VERSION.DEVELOPMENT.getIndex()), Integer.valueOf(versions.get(VERSION.MAJOR.getIndex())), Integer.valueOf(versions.get(VERSION.MINOR.getIndex())), Integer.valueOf(versions.get(VERSION.PATCH.getIndex())));
    }

  }

  private List<String> determineVersions(String version) throws Exception {

    List<String> versions = new ArrayList<String>();

    int majorVersion = 1;
    int minorVersion = 0;
    int patchVersion = 0;

    String[] rawVersion = version.split("\\.");
    if (rawVersion.length == 3) {
      log.debug("Set version-variables from POM.xml");
      log.debug("------------------------------------------------------------------------");
      majorVersion = Integer.valueOf(rawVersion[0]);
      minorVersion = Integer.valueOf(rawVersion[1]);
      patchVersion = Integer.valueOf(rawVersion[2].substring(0, rawVersion[2].lastIndexOf("-")));
    }

    log.debug("MAJOR-version                    : " + majorVersion);
    log.debug("MINOR-version                    : " + minorVersion);
    log.debug("PATCH-version                    : " + patchVersion);
    log.debug("------------------------------------------------------------------------");

    majorVersion = majorVersion + 1;
    minorVersion = 0;
    patchVersion = 0;

    String developmentVersion = majorVersion + "." + minorVersion + "." + patchVersion + "-SNAPSHOT";
    String releaseVersion = majorVersion + "." + minorVersion + "." + patchVersion;
    log.info("Determine new versions for branch : " + getConfiguration().getBranchVersion());
    log.info("New DEVELOPMENT-version           : " + developmentVersion);
    log.info("New GIT-version                   : " + releaseVersion);
    log.info("New RELEASE-version               : " + releaseVersion);
    log.info("------------------------------------------------------------------------");

    versions.add(VERSION.DEVELOPMENT.getIndex(), developmentVersion);
    versions.add(VERSION.RELEASE.getIndex(), releaseVersion);
    versions.add(VERSION.MAJOR.getIndex(), String.valueOf(majorVersion));
    versions.add(VERSION.MINOR.getIndex(), String.valueOf(minorVersion));
    versions.add(VERSION.PATCH.getIndex(), String.valueOf(patchVersion));
    
    cleanupGitLocalAndRemoteTags(releaseVersion);
    
    return versions;
  }
  


}
