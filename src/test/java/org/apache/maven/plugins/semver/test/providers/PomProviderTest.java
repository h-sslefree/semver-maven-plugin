package org.apache.maven.plugins.semver.test.providers;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import org.apache.maven.plugins.semver.providers.PomProvider;
import org.apache.maven.plugins.semver.providers.PomProviderImpl;
import org.apache.maven.plugins.semver.providers.VersionProvider;
import org.apache.maven.plugins.semver.providers.VersionProvider.FINAL_VERSION;
import org.apache.maven.plugins.semver.providers.VersionProvider.RAW_VERSION;
import org.apache.maven.plugins.semver.providers.VersionProviderImpl;
import org.apache.maven.plugins.semver.test.AbstractSemverMavenPluginTest;
import org.junit.Test;

/**
 *
 *
 * <h1>VersionFactoryTest</h1>
 *
 * <p>Testing the version factory without the pom context.
 *
 * @author sido
 */
public class PomProviderTest extends AbstractSemverMavenPluginTest {

  @Test
  public void createReleaseTest() {
    PomProvider pomProvider = new PomProviderImpl();
    VersionProvider versionProvider = new VersionProviderImpl();

    Map<RAW_VERSION, String> rawVersions = new HashMap<>();
    rawVersions.put(RAW_VERSION.DEVELOPMENT, "1.0.1-SNAPSHOT");
    rawVersions.put(RAW_VERSION.RELEASE, "1.0.0");
    rawVersions.put(RAW_VERSION.PATCH, "0");
    rawVersions.put(RAW_VERSION.MINOR, "0");
    rawVersions.put(RAW_VERSION.MAJOR, "1");

    pomProvider.createReleasePom(versionProvider.determineReleaseVersions(rawVersions));

    Map<FINAL_VERSION, String> finalVersions =
        versionProvider.determineReleaseVersions(rawVersions);
    assertEquals(finalVersions.get(FINAL_VERSION.RELEASE), "1.0.0");
    assertEquals(finalVersions.get(FINAL_VERSION.DEVELOPMENT), "1.0.1-SNAPSHOT");
    assertEquals(finalVersions.get(FINAL_VERSION.SCM), "1.0.0");
  }
}
