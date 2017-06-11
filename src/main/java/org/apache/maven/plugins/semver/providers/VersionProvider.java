package org.apache.maven.plugins.semver.providers;

import org.apache.maven.plugins.semver.exceptions.SemverException;
import org.apache.maven.plugins.semver.goals.SemverGoal;
import org.apache.maven.plugins.semver.runmodes.RunMode;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.util.Map;

/**
 * <h1>VersionProvider</h1>
 * <p>The versionprovider is used to determine the different symantic-versioning versions to create git tags.</p>
 *
 * @author sido
 */
public interface VersionProvider {

  /**
   * <p>Determine raw version list from POM-version.</p>
   *
   * @param semverGoal executed gaol
   * @param runMode executed runmode
   * @param configBranchVersion branchVersion determine by {@link org.apache.maven.plugins.semver.configuration.SemverConfiguration}
   * @param configMetaData metaData determine by {@link org.apache.maven.plugins.semver.configuration.SemverConfiguration}
   * @param version example: 0.x.x-SNAPSHOT
   * @return list of development, git and release-versions
   * @throws SemverException native exception
   * @throws IOException write to disk exception
   * @throws GitAPIException repository exception
   */
  Map<VersionProvider.RAW_VERSION, String> determineRawVersions(SemverGoal.SEMVER_GOAL semverGoal, RunMode.RUNMODE runMode, String configBranchVersion, String configMetaData, String version) throws SemverException, IOException, GitAPIException;

  /**
   * <p>Determine release versions from {@link RAW_VERSION}.</p>
   *
   * @param rawVersions raw version map with development version patch, minor and major the {@link org.apache.maven.plugins.semver.providers.VersionProvider.RAW_VERSION} enumeration is used to define the map
   * @return finalVersions
   */
  Map<VersionProviderImpl.FINAL_VERSION, String> determineReleaseVersions(Map<RAW_VERSION, String> rawVersions);

  /**
   * <p>Determine release-versions from {@link VersionProvider.RAW_VERSION}.</p>
   * <p>This version contains also the buildmeta-data and branch information.</p>
   *
   * @param rawVersions raw version map with development version patch, minor and major the {@link org.apache.maven.plugins.semver.providers.VersionProvider.RAW_VERSION} enumeration is used to define the map
   * @return finalVersions
   */
  Map<VersionProviderImpl.FINAL_VERSION, String> determineReleaseBranchVersions(Map<RAW_VERSION, String> rawVersions, RunMode.RUNMODE runMode, String metaData, String branchVersion);

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
  String determineReleaseBranchTag(RunMode.RUNMODE runMode, String branchVersion, int patch, int minor, int major);

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
  String determineReleaseTag(RunMode.RUNMODE runMode, int patch, int minor, int major);

  /**
   * <p>Determine wether or not buildMetaData had to be added to the scmversion for GIT</p>
   *
   * @param patch patch is the number to define a bugfix in symantic-versioning
   * @param minor minor is the number to define a feature in symantic-versioning
   * @param major major is the number to define a breaking change in symantic-versioning
   * @return build metadata
   */
  String determineBuildMetaData(RunMode.RUNMODE runMode, String metaData, int patch, int minor, int major);

  /**
   * <p>Determine if the version in the pom.xml is corrupt.</p>
   * <p>If this is the case then exit the semver-plugin.</p>
   *
   * @param pomVersion get pom version from project
   * @return is version corrupt?
   * @throws SemverException native semver exception
   */
  boolean isVersionCorrupt(String pomVersion) throws SemverException;

  /**
   * <p>Version-type is mentoined here.</p>
   *
   */
  enum RAW_VERSION {
    DEVELOPMENT,
    RELEASE,
    SCM,
    MAJOR,
    MINOR,
    PATCH
  }

  /**
   *
   * <p>The FINAL_VERSION is determined in each RunMode to create the new build-version.</p>
   */
  enum FINAL_VERSION {
    DEVELOPMENT,
    BUILD_METADATA,
    SCM,
    RELEASE
  }

}
