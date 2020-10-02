package org.apache.maven.plugins.semver.providers;

import java.util.Map;

/**
 * <h>PomProvider</h>
 *
 * <p>The PomProvider handles all request converning pom-changes.
 *
 * @author sido
 */
public interface PomProvider {

  /**
   * <h>Create release-pom</h>
   *
   * <p>Create a release-pom for the build.
   *
   * @param finalVersions final versions from the plugin-goals
   */
  void createReleasePom(Map<VersionProvider.FINAL_VERSION, String> finalVersions);

  /**
   * <h>Create development-pom</h>
   *
   * <p>Create next development-pom for this project
   *
   * @param developmentVersion developmentVersion
   */
  void createNextDevelopmentPom(String developmentVersion);
}
