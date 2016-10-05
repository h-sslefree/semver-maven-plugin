package org.apache.maven.plugins.semver.goals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.semver.SemverMavenPlugin;
import org.eclipse.jgit.api.errors.GitAPIException;

@Mojo(name = "major")
public class SemverMavenPluginGoalMajor extends SemverMavenPlugin {

  public void execute() throws MojoExecutionException, MojoFailureException {

    String version = project.getVersion();
    String scmConnection = project.getScm().getConnection();
    File scmRoot = project.getBasedir();

    log.info("Semver-goal                       : MAJOR");
    log.info("Run-mode                          : " + runMode);
    log.info("Version from POM                  : " + version);
    log.info("SCM-connection                    : " + scmConnection);
    log.info("SCM-root                          : " + scmRoot);
    log.info("------------------------------------------------------------------------");

    try {
      cleanupGitTags(scmConnection, scmRoot);
    } catch (IOException e) {
      log.error(e.getMessage());
    } catch (GitAPIException e) {
      log.error(e.getMessage());
    }

    List<String> versions = new ArrayList<String>();
    try {
      versions = determineVersions(version);
    } catch (Exception e) {
      log.error(e.getMessage());
    }

    if (runMode.equals(RUN_MODE.RELEASE.getKey())) {
      createReleaseProperties(versions.get(DEVELOPMENT), versions.get(RELEASE));
    } else if (runMode.equals(RUN_MODE.NATIVE.getKey())) {
      createReleaseNative(versions.get(DEVELOPMENT), versions.get(RELEASE));
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
      patchVersion = Integer.valueOf(rawVersion[2].substring(0, 1));
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
    log.info("New DEVELOPMENT-version           : " + developmentVersion);
    log.info("New GIT-version                   : " + releaseVersion);
    log.info("New RELEASE-version               : " + releaseVersion);
    log.info("------------------------------------------------------------------------");

    versions.add(DEVELOPMENT, developmentVersion);
    versions.add(RELEASE, releaseVersion);

    return versions;
  }

  private void createReleaseNative(String developmentVersion, String releaseVersion) {
    // TODO Auto-generated method stub

  }

}
