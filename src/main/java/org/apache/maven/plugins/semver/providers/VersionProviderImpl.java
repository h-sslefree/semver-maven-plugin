package org.apache.maven.plugins.semver.providers;

import org.apache.maven.plugins.semver.SemverMavenPlugin;
import org.apache.maven.plugins.semver.exceptions.SemverException;
import org.apache.maven.plugins.semver.runmodes.RunMode;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;


@Component(role = VersionProvider.class)
public class VersionProviderImpl implements VersionProvider {

  @Requirement
  private Logger LOG;
  @Requirement
  private RepositoryProvider repositoryProvider;

  /**
   * <p>In the constructor the logging and the configuration is given.</p>
   * <p>These are elements which continue to return in the different methods.</p>
   */
  @Inject
  public VersionProviderImpl() {
  }

  /**
   * <p>Determine release versions from {@link SemverMavenPlugin.RAW_VERSION}.</p>
   *
   * @param rawVersions raw version map with development version patch, minor and major the {@link org.apache.maven.plugins.semver.SemverMavenPlugin.RAW_VERSION} enumeration is used to define the map
   * @return finalVersions
   */
  @Override
  public Map<FINAL_VERSION, String> determineReleaseVersions(Map<SemverMavenPlugin.RAW_VERSION, String> rawVersions) {
    Map<FINAL_VERSION, String> finalVersions = new HashMap<>();
    finalVersions.put(FINAL_VERSION.DEVELOPMENT, rawVersions.get(SemverMavenPlugin.RAW_VERSION.DEVELOPMENT));
    finalVersions.put(FINAL_VERSION.RELEASE, rawVersions.get(SemverMavenPlugin.RAW_VERSION.RELEASE));
    finalVersions.put(FINAL_VERSION.SCM, rawVersions.get(SemverMavenPlugin.RAW_VERSION.RELEASE));
    return finalVersions;
  }

  /**
   * <p>Determine release-versions from {@link SemverMavenPlugin.RAW_VERSION}.</p>
   * <p>This version contains also the buildmeta-data and branch information.</p>
   *
   * @param rawVersions raw version map with development version patch, minor and major the {@link org.apache.maven.plugins.semver.SemverMavenPlugin.RAW_VERSION} enumeration is used to define the map
   * @return finalVersions
   */
  @Override
  public Map<FINAL_VERSION, String> determineReleaseBranchVersions(Map<SemverMavenPlugin.RAW_VERSION, String> rawVersions, RunMode.RUNMODE runMode, String metaData, String branchVersion) {

    int patch = Integer.parseInt(rawVersions.get(SemverMavenPlugin.RAW_VERSION.PATCH));
    int minor = Integer.parseInt(rawVersions.get(SemverMavenPlugin.RAW_VERSION.MINOR));
    int major = Integer.parseInt(rawVersions.get(SemverMavenPlugin.RAW_VERSION.MAJOR));

    String releaseTag = determineReleaseTag(runMode, patch, minor, major);
    String buildMetaData = determineBuildMetaData(runMode, metaData, patch, minor, major);

    StringBuilder releaseVersion = new StringBuilder();
    if (branchVersion != null && !branchVersion.isEmpty()) {
      releaseVersion.append(branchVersion);
      releaseVersion.append("-");
    }
    releaseVersion.append(releaseTag);

    StringBuilder scmVersion = new StringBuilder();
    scmVersion.append(releaseVersion);

    if (LOG != null) {
      LOG.info("New DEVELOPMENT-version            : " + rawVersions.get(SemverMavenPlugin.RAW_VERSION.DEVELOPMENT));
      LOG.info("New BRANCH GIT build metadata      : " + buildMetaData);
      LOG.info("New BRANCH GIT-version             : " + scmVersion);
      LOG.info("New BRANCH RELEASE-version         : " + releaseVersion);
      LOG.info(SemverMavenPlugin.MOJO_LINE_BREAK);
    }
    Map<FINAL_VERSION, String> finalVersions = new HashMap<>();
    finalVersions.put(FINAL_VERSION.DEVELOPMENT, rawVersions.get(SemverMavenPlugin.RAW_VERSION.DEVELOPMENT));
    finalVersions.put(FINAL_VERSION.BUILD_METADATA, buildMetaData);
    finalVersions.put(FINAL_VERSION.SCM, scmVersion.toString());
    finalVersions.put(FINAL_VERSION.RELEASE, releaseVersion.toString());

    return finalVersions;
  }

  /**
   * <p>Determine general release-tag.</p>
   * <p>Examples:</p>
   * <ul><b>NORMAL-release</b>
   * <li>1.1.1</li>
   * </ul>
   * <ul><b>BRANCH-release</b>
   * <li>1.1.1-001001001</li>
   * </ul>
   *
   * @param patch patch is the number to define a bugfix in symantic-versioning
   * @param minor minor is the number to define a feature in symantic-versioning
   * @param major major is the number to define a breaking change in symantic-versioning
   * @return release tag
   */
  @Override
  public String determineReleaseBranchTag(RunMode.RUNMODE runMode, String branchVersion, int patch, int minor, int major) {
    StringBuilder releaseTag = new StringBuilder();
    releaseTag.append(major);
    releaseTag.append(".");
    releaseTag.append(minor);
    releaseTag.append(".");
    releaseTag.append(patch);
    if (runMode == RunMode.RUNMODE.RELEASE_BRANCH_RPM || runMode == RunMode.RUNMODE.NATIVE_BRANCH_RPM) {
      releaseTag = new StringBuilder();
      releaseTag.append(branchVersion);
      releaseTag.append(String.format("%03d%03d%03d", major, minor, patch));
    }
    return releaseTag.toString();
  }

  /**
   * <p>Determine general release-tag.</p>
   * <p>Examples:</p>
   * <ul><b>NORMAL-release</b>
   * <li>1.1.1</li>
   * </ul>
   * <ul><b>BRANCH-release</b>
   * <li>1.1.1-001001001</li>
   * </ul>
   *
   * @param patch patch is the number to define a bugfix in symantic-versioning
   * @param minor minor is the number to define a feature in symantic-versioning
   * @param major major is the number to define a breaking change in symantic-versioning
   * @return release tag
   */
  @Override
  public String determineReleaseTag(RunMode.RUNMODE runMode, int patch, int minor, int major) {
    StringBuilder releaseTag = new StringBuilder();
    releaseTag.append(major);
    releaseTag.append(".");
    releaseTag.append(minor);
    releaseTag.append(".");
    releaseTag.append(patch);
    if (runMode == RunMode.RUNMODE.RELEASE_BRANCH_RPM || runMode == RunMode.RUNMODE.NATIVE_BRANCH_RPM) {
      releaseTag = new StringBuilder();
      releaseTag.append(String.format("%03d%03d%03d", major, minor, patch));
    }
    return releaseTag.toString();
  }

  /**
   * <p>Determine wether or not buildMetaData had to be added to the scmversion for GIT</p>
   *
   * @param patch patch is the number to define a bugfix in symantic-versioning
   * @param minor minor is the number to define a feature in symantic-versioning
   * @param major major is the number to define a breaking change in symantic-versioning
   * @return build metadata
   */
  @Override
  public String determineBuildMetaData(RunMode.RUNMODE runmode, String metaData, int patch, int minor, int major) {
    StringBuilder buildMetaData = new StringBuilder();
    if (runmode == RunMode.RUNMODE.RELEASE_BRANCH_RPM ||
            runmode == RunMode.RUNMODE.NATIVE_BRANCH_RPM) {
      String buildMetaDataBranch = major + "." + minor + "." + patch;
      buildMetaData.append("+");
      buildMetaData.append(buildMetaDataBranch);
    }
    if (!metaData.isEmpty()) {
      buildMetaData.append("+");
      buildMetaData.append(metaData);
    }
    return buildMetaData.toString();
  }

  /**
   * <p>Determine if the version in the pom.xml is corrupt.</p>
   * <p>If this is the case then exit the semver-plugin.</p>
   *
   * @param pomVersion get pom version from project
   * @return is version corrupt?
   * @throws SemverException native semver exception
   */
  @Override
  public boolean isVersionCorrupt(String pomVersion) throws SemverException {
    boolean isVersionCorrupt = false;
    LOG.info("Check on pom-version");
    LOG.info(SemverMavenPlugin.MOJO_LINE_BREAK);
    if (pomVersion == null || pomVersion.isEmpty()) {
      isVersionCorrupt = true;
      LOG.error("");
      LOG.error("The version in the pom.xml is NULL of empty please correct the pom.xml");
      LOG.error("");
    } else if (!pomVersion.contains("-SNAPSHOT")) {
      isVersionCorrupt = true;
      LOG.error("");
      LOG.error("The version in the pom.xml does not contain -SNAPSHOT. Please repair the version-string");
      LOG.error("");
    } else {
      LOG.info("Pom-version is correct             : " + pomVersion);
    }
    LOG.info(SemverMavenPlugin.FUNCTION_LINE_BREAK);
    return isVersionCorrupt;
  }


}
