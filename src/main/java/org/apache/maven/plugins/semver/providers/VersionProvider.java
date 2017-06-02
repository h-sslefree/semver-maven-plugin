package org.apache.maven.plugins.semver.providers;

import org.apache.maven.plugins.semver.SemverMavenPlugin;
import org.apache.maven.plugins.semver.exceptions.SemverException;
import org.apache.maven.plugins.semver.runmodes.RunMode;

import java.util.Map;

/**
 * <h1>VersionProvider</h1>
 * <p>The versionprovider is used to determine the different symantic-versioning versions to create git tags.</p>
 *
 * @author sido
 */
public interface VersionProvider {

  Map<VersionProviderImpl.FINAL_VERSION, String> determineReleaseVersions(Map<RAW_VERSION, String> rawVersions);

  Map<VersionProviderImpl.FINAL_VERSION, String> determineReleaseBranchVersions(Map<RAW_VERSION, String> rawVersions, RunMode.RUNMODE runMode, String metaData, String branchVersion);

  String determineReleaseBranchTag(RunMode.RUNMODE runMode, String branchVersion, int patch, int minor, int major);

  String determineReleaseTag(RunMode.RUNMODE runMode, int patch, int minor, int major);

  String determineBuildMetaData(RunMode.RUNMODE runMode, String metaData, int patch, int minor, int major);

  boolean isVersionCorrupt(String pomVersion) throws SemverException;

  /**
   * <p>Version-type is mentoined here.</p>
   *
   * @author sido
   */
  enum RAW_VERSION {
    DEVELOPMENT,
    RELEASE,
    SCM,
    MAJOR,
    MINOR,
    PATCH
  }

  enum FINAL_VERSION {
    DEVELOPMENT,
    BUILD_METADATA,
    SCM,
    RELEASE
  }

}
