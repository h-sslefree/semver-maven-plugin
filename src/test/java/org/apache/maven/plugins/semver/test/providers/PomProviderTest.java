package org.apache.maven.plugins.semver.test.providers;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.semver.SemverMavenPlugin;
import org.apache.maven.plugins.semver.configuration.SemverConfiguration;
import org.apache.maven.plugins.semver.providers.*;
import org.apache.maven.plugins.semver.runmodes.RunMode;
import org.apache.maven.plugins.semver.test.AbstractSemverMavenPluginTest;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 *
 * <h1>VersionFactoryTest</h1>
 * <p>Testing the version factory withoutthe pom context.</p>
 *
 * @author sido
 */
public class PomProviderTest extends AbstractSemverMavenPluginTest {

    private final Log LOG = null;

    private SemverConfiguration getConfigurationRelease() {
        SemverConfiguration configuration = new SemverConfiguration(null);
        configuration.setRunMode(RunMode.RUNMODE.RELEASE);
        return configuration;
    }

    @Test
    public void createReleaseTest() {

        RepositoryProvider repositoryProvider = new RepositoryProviderImpl();
        PomProvider pomProvider = new PomProviderImpl();
        VersionProvider versionProvider = new VersionProviderImpl();

        Map<SemverMavenPlugin.RAW_VERSION, String> rawVersions = new HashMap<>();
        rawVersions.put(SemverMavenPlugin.RAW_VERSION.DEVELOPMENT, "1.0.1-SNAPSHOT");
        rawVersions.put(SemverMavenPlugin.RAW_VERSION.RELEASE, "1.0.0");
        rawVersions.put(SemverMavenPlugin.RAW_VERSION.PATCH, "0");
        rawVersions.put(SemverMavenPlugin.RAW_VERSION.MINOR, "0");
        rawVersions.put(SemverMavenPlugin.RAW_VERSION.MAJOR, "1");

        pomProvider.createReleasePom(versionProvider.determineReleaseVersions(rawVersions));

        Map<VersionProvider.FINAL_VERSION, String> finalVersions = versionProvider.determineReleaseVersions(rawVersions);
        assertEquals(finalVersions.get(VersionProvider.FINAL_VERSION.RELEASE), "1.0.0");
        assertEquals(finalVersions.get(VersionProvider.FINAL_VERSION.DEVELOPMENT), "1.0.1-SNAPSHOT");
        assertEquals(finalVersions.get(VersionProvider.FINAL_VERSION.SCM), "1.0.0");
    }



}




