package org.apache.maven.plugins.semver.goals;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.semver.SemverMavenPlugin;
import org.apache.maven.plugins.semver.exceptions.SemverException;
import org.apache.maven.plugins.semver.runmodes.RunMode;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * <h>Determine MAJOR version for MAVEN-project.</h>
 * <p>This advances the tag of the project and the pom.xml version.</p>
 * <p>Example:</p>
 * <pre>
 *     <code>
 *          <version>1.x.x</version>
 *          to
 *          <version>2.x.x</version>
 *     </code>
 * </pre>
 * <p>Run the test-phase when this goal is executed.</p>
 *
 * @author sido
 */
@Mojo(name = "major")
@Execute(phase = LifecyclePhase.TEST)
public class SemverMavenPluginGoalMajor extends SemverMavenPlugin {

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {

    String version = project.getVersion();
    String scmConnection = project.getScm().getConnection();
    File scmRoot = project.getBasedir();
    getRepositoryProvider().initialize(scmRoot, scmConnection, getConfiguration().getScmUsername(), getConfiguration().getScmPassword());

    LOG.info(FUNCTION_LINE_BREAK);
    LOG.info("Semver-goal                        : MAJOR");
    LOG.info("Run-mode                           : " + getConfiguration().getRunMode());
    LOG.info("Version from POM                   : " + version);
    LOG.info("SCM-connection                     : " + scmConnection);
    LOG.info("SCM-root                           : " + scmRoot);
    LOG.info(FUNCTION_LINE_BREAK);

    Map<RAW_VERSION, String> rawVersions = new HashMap<>();
    try {
      if (!getVersionProvider().isVersionCorrupt(version) && !getRepositoryProvider().isChanged()) {
        rawVersions = determineRawVersions(version);
      } else {
        Runtime.getRuntime().exit(1);
      }
    } catch (Exception e) {
      LOG.error(e.getMessage());
    }

    executeRunMode(rawVersions);

  }

  /**
   * <p>Determine raw version list from POM-version.</p>
   *
   * @param version example: 0.x.x-SNAPSHOT
   * @return list of development, git and release-version
   * @throws SemverException native exception
   * @throws IOException write to disk exception
   * @throws GitAPIException repository exception
   */
  private Map<RAW_VERSION, String> determineRawVersions(String version) throws SemverException, IOException, GitAPIException {

    Map<RAW_VERSION, String> versions = new HashMap<>();

    int majorVersion;
    int minorVersion;
    int patchVersion;

    String[] rawVersion = version.split("\\.");
    if (rawVersion.length > 0 && rawVersion.length == 3) {
      LOG.debug("Set version-variables from POM.xml");
      LOG.debug(MOJO_LINE_BREAK);
      majorVersion = Integer.valueOf(rawVersion[0]);
      minorVersion = Integer.valueOf(rawVersion[1]);
      patchVersion = Integer.valueOf(rawVersion[2].substring(0, rawVersion[2].lastIndexOf('-')));
    } else {
      LOG.error("Unrecognized version-pattern");
      LOG.error("Semver plugin is terminating");
      throw new SemverException("Unrecognized version-pattern", "Could not parse version from POM.xml because of not parseble version-pattern");
    }

    LOG.debug("MAJOR-version                     : " + majorVersion);
    LOG.debug("MINOR-version                     : " + minorVersion);
    LOG.debug("PATCH-version                     : " + patchVersion);
    LOG.debug(MOJO_LINE_BREAK);

    majorVersion = majorVersion + 1;
    minorVersion = 0;
    patchVersion = 0;

    String developmentVersion = majorVersion + "." + minorVersion + "." + patchVersion + "-SNAPSHOT";


    //TODO:SH move this part to a RunModeNative and RunModeNativeRpm implementation
    String releaseVersion;
    String scmVersion;
    if(getConfiguration().getRunMode().equals(RunMode.RUNMODE.NATIVE_BRANCH) || getConfiguration().getRunMode().equals(RunMode.RUNMODE.NATIVE_BRANCH_RPM)) {
      scmVersion = getVersionProvider().determineReleaseBranchTag(getConfiguration().getRunMode(), getConfiguration().getBranchVersion(), patchVersion, minorVersion, majorVersion);
      releaseVersion = scmVersion;
    } else {
      scmVersion = getVersionProvider().determineReleaseTag(getConfiguration().getRunMode(), patchVersion, minorVersion, majorVersion);
      releaseVersion = majorVersion + "." + minorVersion + "." + patchVersion;
    }

    String metaData = getVersionProvider().determineBuildMetaData(getConfiguration().getRunMode(), getConfiguration().getMetaData(), patchVersion, minorVersion, majorVersion);

    LOG.info("New DEVELOPMENT-version            : " + developmentVersion);
    LOG.info("New GIT-version                    : " + scmVersion+metaData);
    LOG.info("New RELEASE-version                : " + releaseVersion);
    LOG.info(FUNCTION_LINE_BREAK);

    versions.put(RAW_VERSION.DEVELOPMENT, developmentVersion);
    versions.put(RAW_VERSION.RELEASE, releaseVersion);
    versions.put(RAW_VERSION.MAJOR, String.valueOf(majorVersion));
    versions.put(RAW_VERSION.MINOR, String.valueOf(minorVersion));
    versions.put(RAW_VERSION.PATCH, String.valueOf(patchVersion));

    getRepositoryProvider().isLocalVersionCorrupt(scmVersion);
    return versions;
  }


}
