package org.apache.maven.plugins.semver.goals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.semver.SemverMavenPlugin;

@Mojo(name = "patch", requiresProject = true)
public class SemverMavenPluginGoalPatch extends SemverMavenPlugin {

  public void execute() throws MojoExecutionException, MojoFailureException {
    
    String version = project.getVersion();
    String scmConnection = project.getScm().getConnection();
    File scmRoot = project.getBasedir();
    
    log.info("Semver-goal                       : PATCH");
    log.info("Run-mode                          : " + runMode);
    log.info("Version from POM                  : " + version);
    log.info("SCM-connection                    : " + scmConnection);
    log.info("SCM-root                          : " + scmRoot);
    log.info("--------------------------------------------------");

    List<String> versions = new ArrayList<String>();
    try {
      versions = determineVersions(version);
    } catch (Exception e) {
      log.error(e);
    }
    
    if(runMode.equals(RUN_MODE.RELEASE.getKey())) {
      createReleaseProperties(versions.get(DEVELOPMENT), versions.get(RELEASE));
    } else if (runMode.equals(RUN_MODE.NATIVE.getKey())) {
      createReleaseNative(versions.get(DEVELOPMENT), versions.get(RELEASE));
    }
    
  }
  
  private List<String> determineVersions(String version) throws Exception {

    List<String> versions = new ArrayList<String>();

    int majorVersion = 0;
    int minorVersion = 0;
    int patchVersion = 1;

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

    patchVersion = patchVersion + 1;

    String developmentVersion = majorVersion + "." + minorVersion + "." + patchVersion + "-SNAPSHOT";
    String releaseVersion = majorVersion + "." + minorVersion + "." + patchVersion;
    log.info("New DEVELOPMENT-version           : " + developmentVersion);
    log.info("New GIT-version                   : " + releaseVersion);
    log.info("New RELEASE-version               : " + releaseVersion);
    log.info("------------------------------------------------------------------------");

    versions.add(DEVELOPMENT, developmentVersion);
    versions.add(RELEASE, releaseVersion);

    cleanupGitLocalAndRemoteTags(releaseVersion);
    
    return versions;
  }
  
  private void createReleaseNative(String developmentVersion, String releaseVersion) {
    // TODO Auto-generated method stub
    
  }
  
}
