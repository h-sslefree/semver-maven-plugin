package org.apache.maven.plugins.semver.providers;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.apache.maven.plugins.semver.SemverMavenPlugin;
import org.apache.maven.plugins.semver.exceptions.SemverException;
import org.apache.maven.plugins.semver.goals.SemverGoal;
import org.apache.maven.plugins.semver.runmodes.RunMode;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named
@Singleton
public class VersionProviderImpl implements VersionProvider {

  private Logger LOG = LoggerFactory.getLogger(VersionProviderImpl.class);

  private RepositoryProvider repositoryProvider;

  @Inject
  public VersionProviderImpl(RepositoryProvider repositoryProvider) {
    this.repositoryProvider = requireNonNull(repositoryProvider);
  }

  public Map<VersionProvider.RAW_VERSION, String> determineRawVersions(
      SemverGoal.SEMVER_GOAL semverGoal,
      RunMode.RUN_MODE runMode,
      String configBranchVersion,
      String configMetaData,
      String pomVersion)
      throws SemverException, IOException, GitAPIException {

    Map<VersionProvider.RAW_VERSION, String> versions = new HashMap<>();

    int majorVersion;
    int minorVersion;
    int patchVersion;

    String[] rawVersion = pomVersion.split("\\.");
    if (rawVersion.length == 3) {
      LOG.debug("Set version-variables from POM.xml");
      LOG.debug(SemverMavenPlugin.MOJO_LINE_BREAK);
      majorVersion = Integer.parseInt(rawVersion[0]);
      minorVersion = Integer.parseInt(rawVersion[1]);
      patchVersion = Integer.parseInt(rawVersion[2].substring(0, rawVersion[2].lastIndexOf('-')));
    } else {
      LOG.error("Unrecognized version-pattern");
      LOG.error("Semver plugin is terminating");
      throw new SemverException(
          "Unrecognized version-pattern",
          "Could not parse version from POM.xml because of not parsable version-pattern");
    }

    LOG.debug("MAJOR-version                     : [ {} ]", majorVersion);
    LOG.debug("MINOR-version                     : [ {} ]", minorVersion);
    LOG.debug("PATCH-version                     : [ {} ]", patchVersion);
    LOG.debug(SemverMavenPlugin.MOJO_LINE_BREAK);

    if (semverGoal == SemverGoal.SEMVER_GOAL.MAJOR) {
      majorVersion = majorVersion + 1;
      minorVersion = 0;
      patchVersion = 0;
    } else if (semverGoal == SemverGoal.SEMVER_GOAL.MINOR) {
      minorVersion = minorVersion + 1;
      patchVersion = 0;
    } else if (semverGoal == SemverGoal.SEMVER_GOAL.PATCH) {
      patchVersion = patchVersion + 1;
    }

    String developmentVersion =
        majorVersion + "." + minorVersion + "." + patchVersion + "-SNAPSHOT";

    // TODO:SH move this part to a RunModeNative and RunModeNativeRpm implementation
    String releaseVersion;
    String scmVersion;
    if (runMode.equals(RunMode.RUN_MODE.NATIVE_BRANCH)
        || runMode.equals(RunMode.RUN_MODE.NATIVE_BRANCH_RPM)) {
      scmVersion =
          determineReleaseBranchTag(
              runMode, configBranchVersion, patchVersion, minorVersion, majorVersion);
      releaseVersion = scmVersion;
    } else {
      scmVersion = determineReleaseTag(runMode, patchVersion, minorVersion, majorVersion);
      releaseVersion = majorVersion + "." + minorVersion + "." + patchVersion;
    }

    String metaData =
        determineBuildMetaData(runMode, configMetaData, patchVersion, minorVersion, majorVersion);

    LOG.info("New DEVELOPMENT-version            : [ {} ]", developmentVersion);
    LOG.info("New GIT-version                    : [ {}{} ]", scmVersion, metaData);
    LOG.info("New RELEASE-version                : [ {} ]", releaseVersion);
    LOG.info(SemverMavenPlugin.FUNCTION_LINE_BREAK);

    versions.put(VersionProvider.RAW_VERSION.DEVELOPMENT, developmentVersion);
    versions.put(VersionProvider.RAW_VERSION.RELEASE, releaseVersion);
    versions.put(VersionProvider.RAW_VERSION.SCM, scmVersion + metaData);
    versions.put(VersionProvider.RAW_VERSION.MAJOR, String.valueOf(majorVersion));
    versions.put(VersionProvider.RAW_VERSION.MINOR, String.valueOf(minorVersion));
    versions.put(VersionProvider.RAW_VERSION.PATCH, String.valueOf(patchVersion));

    repositoryProvider.isLocalVersionCorrupt(scmVersion);
    return versions;
  }

  @Override
  public Map<FINAL_VERSION, String> determineReleaseVersions(Map<RAW_VERSION, String> rawVersions) {
    Map<FINAL_VERSION, String> finalVersions = new HashMap<>();
    finalVersions.put(FINAL_VERSION.DEVELOPMENT, rawVersions.get(RAW_VERSION.DEVELOPMENT));
    finalVersions.put(FINAL_VERSION.RELEASE, rawVersions.get(RAW_VERSION.RELEASE));
    finalVersions.put(FINAL_VERSION.SCM, rawVersions.get(RAW_VERSION.RELEASE));
    return finalVersions;
  }

  @Override
  public Map<FINAL_VERSION, String> determineReleaseBranchVersions(
      Map<VersionProvider.RAW_VERSION, String> rawVersions,
      RunMode.RUN_MODE runMode,
      String metaData,
      String branchVersion) {

    int patch = Integer.parseInt(rawVersions.get(VersionProvider.RAW_VERSION.PATCH));
    int minor = Integer.parseInt(rawVersions.get(VersionProvider.RAW_VERSION.MINOR));
    int major = Integer.parseInt(rawVersions.get(VersionProvider.RAW_VERSION.MAJOR));

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
    scmVersion.append(buildMetaData);

    LOG.info(
        "New DEVELOPMENT-version            : [ {} ]",
        rawVersions.get(VersionProvider.RAW_VERSION.DEVELOPMENT));
    LOG.info(
        "New BRANCH GIT build-metadata      : [ {} ]", determineLogBuildMetaData(buildMetaData));
    LOG.info("New BRANCH GIT-version             : [ {} ]", scmVersion);
    LOG.info("New BRANCH RELEASE-version         : [ {} ]", releaseVersion);
    LOG.info(SemverMavenPlugin.MOJO_LINE_BREAK);

    Map<FINAL_VERSION, String> finalVersions = new HashMap<>();
    finalVersions.put(
        FINAL_VERSION.DEVELOPMENT, rawVersions.get(VersionProvider.RAW_VERSION.DEVELOPMENT));
    finalVersions.put(FINAL_VERSION.BUILD_METADATA, buildMetaData);
    finalVersions.put(FINAL_VERSION.SCM, scmVersion.toString());
    finalVersions.put(FINAL_VERSION.RELEASE, releaseVersion.toString());

    return finalVersions;
  }

  /**
   * Sometimes there is no metaData. To make sure no blank fileeds are displayed this method
   * determines if there is metaData.
   *
   * @param buildMetaData source metaData
   * @return metaData for logging purposes
   */
  private String determineLogBuildMetaData(String buildMetaData) {
    String logBuildMetaData = "no build metadata determined";
    if (!buildMetaData.isEmpty()) {
      logBuildMetaData = buildMetaData;
    }
    return logBuildMetaData;
  }

  @Override
  public String determineReleaseBranchTag(
      RunMode.RUN_MODE runMode, String branchVersion, int patch, int minor, int major) {
    StringBuilder releaseTag = new StringBuilder();
    releaseTag.append(major);
    releaseTag.append(".");
    releaseTag.append(minor);
    releaseTag.append(".");
    releaseTag.append(patch);
    if (runMode == RunMode.RUN_MODE.RELEASE_BRANCH_RPM
        || runMode == RunMode.RUN_MODE.NATIVE_BRANCH_RPM) {
      releaseTag = new StringBuilder();
      releaseTag.append(branchVersion);
      releaseTag.append(String.format("%03d%03d%03d", major, minor, patch));
    }
    return releaseTag.toString();
  }

  @Override
  public String determineReleaseTag(RunMode.RUN_MODE runMode, int patch, int minor, int major) {
    StringBuilder releaseTag = new StringBuilder();
    releaseTag.append(major);
    releaseTag.append(".");
    releaseTag.append(minor);
    releaseTag.append(".");
    releaseTag.append(patch);
    if (runMode == RunMode.RUN_MODE.RELEASE_BRANCH_RPM
        || runMode == RunMode.RUN_MODE.NATIVE_BRANCH_RPM) {
      releaseTag = new StringBuilder();
      releaseTag.append(String.format("%03d%03d%03d", major, minor, patch));
    }
    return releaseTag.toString();
  }

  @Override
  public String determineBuildMetaData(
      RunMode.RUN_MODE runmode, String metaData, int patch, int minor, int major) {
    StringBuilder buildMetaData = new StringBuilder();
    if (runmode == RunMode.RUN_MODE.RELEASE_BRANCH_RPM
        || runmode == RunMode.RUN_MODE.NATIVE_BRANCH_RPM) {
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
    } else if (pomVersion.contains("-SNAPSHOT")) {
      isVersionCorrupt = true;
      LOG.error("");
      LOG.error(
          "The version in the pom.xml [ {} ] does not contain -SNAPSHOT. Please repair the version-string",
          pomVersion);
      LOG.error("");
    } else {
      LOG.info("Pom-version is correct             : [ {} ]", pomVersion);
    }
    LOG.info(SemverMavenPlugin.FUNCTION_LINE_BREAK);
    return isVersionCorrupt;
  }
}
