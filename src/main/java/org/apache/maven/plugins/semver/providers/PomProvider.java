package org.apache.maven.plugins.semver.providers;

import java.util.Map;

/**
 *
 * <h>PomProvider</h>
 * <p>The PomProvider handles all request converning pom-changes.</p>
 *
 * @author sido
 */
public interface PomProvider {

    void createReleasePom(Map<VersionProvider.FINAL_VERSION, String> finalVersions);

    void createNextDevelopmentPom(String developmentVersion);
}
