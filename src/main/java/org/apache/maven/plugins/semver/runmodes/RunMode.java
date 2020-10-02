package org.apache.maven.plugins.semver.runmodes;

import org.apache.maven.plugins.semver.configuration.SemverConfiguration;
import org.apache.maven.plugins.semver.exceptions.SemverException;
import org.apache.maven.plugins.semver.goals.SemverGoal;
import org.apache.maven.plugins.semver.goals.SemverGoal.SEMVER_GOAL;
import org.apache.maven.plugins.semver.providers.RepositoryProvider;
import org.apache.maven.plugins.semver.providers.VersionProvider;

/**
 *
 *
 * <h1>RunMode</h1>
 *
 * <p>Possible RunModes that can be configured for this plugin.
 *
 * @author sido
 */
public interface RunMode {

  /**
   * Different kind of RunModes you can configure.
   *
   * <ul>
   *   <li>RELEASE: uses basic semantic-versioning and the release-plugin to create a tag
   *   <li>RELEASE_BRANCH: uses semantic-versioning with branch-prefix and the release-plugin to
   *       create a tag
   *   <li>RELEASE_BRANCH_RPM: uses semantic-versioning with branch-prefix and a parsed variant of
   *       the semantic-versioning as a release-number for RPM. It also uses the release-plugin to
   *       create a tag
   *   <li>NATIVE (default): uses basic semantic-versioning to create a tag
   *   <li>NATIVE_BRANCH: uses semantic-versioning with branch-prefix to create a tag
   *   <li>NATIVE_BRANCH_RPM: uses semantic-versioning with branch-prefix and a parsed variant of
   *       the semantic-versioning as a release-number for RPM
   * </ul>
   */
  enum RUN_MODE {
    RELEASE,
    RELEASE_BRANCH,
    RELEASE_BRANCH_RPM,
    NATIVE,
    NATIVE_BRANCH,
    NATIVE_BRANCH_RPM,
    RUNMODE_NOT_SPECIFIED;

    public static RUN_MODE convertToEnum(String runMode) {
      RUN_MODE value = RUNMODE_NOT_SPECIFIED;
      if (runMode != null) {
        if ("RELEASE".equals(runMode)) {
          value = RELEASE;
        } else if ("RELEASE_BRANCH".equals(runMode)) {
          value = RELEASE_BRANCH;
        } else if ("RELEASE_BRANCH_RPM".equals(runMode)) {
          value = RELEASE_BRANCH_RPM;
        } else if ("NATIVE".equals(runMode)) {
          value = NATIVE;
        } else if ("NATIVE_BRANCH".equals(runMode)) {
          value = NATIVE_BRANCH;
        } else if ("NATIVE_BRANCH_RPM".equals(runMode)) {
          value = NATIVE_BRANCH_RPM;
        }
      }
      return value;
    }
  }

  /**
   * Run all checks before performing the RunMode.
   *
   * @param repositoryProvider provider for the GIT-repos
   * @param versionProvider privder for version mutations
   * @param configuration configuration for plugin
   * @param scmTag the version that has to be checked
   */
  static void checkRemoteRepository(
      RepositoryProvider repositoryProvider,
      VersionProvider versionProvider,
      SemverConfiguration configuration,
      String scmTag)
      throws SemverException {
    if (configuration.checkRemoteVersionTags()) {
      if (repositoryProvider.isRemoteVersionCorrupt(scmTag)) {
        Runtime.getRuntime().exit(1);
      }
    }
    if (versionProvider.isVersionCorrupt(scmTag) || repositoryProvider.isChanged()) {
      Runtime.getRuntime().exit(1);
    }
  }

  /**
   * Execute each RunMode implementation.
   *
   * @param semverGoal {@link SemverGoal} that is called
   * @param configuration plugin configuration
   * @param pomVersion pom version
   */
  void execute(SEMVER_GOAL semverGoal, SemverConfiguration configuration, String pomVersion);
}
