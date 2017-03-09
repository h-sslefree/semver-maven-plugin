package org.apache.maven.plugins.semver.test.factories;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.semver.SemverMavenPlugin;
import org.apache.maven.plugins.semver.configuration.SemverConfiguration;
import org.apache.maven.plugins.semver.factories.VersionFactory;
import org.apache.maven.plugins.semver.test.AbstractSemverMavenPluginTest;
import org.junit.Test;
import static org.junit.Assert.assertEquals;


import java.util.HashMap;
import java.util.Map;

/**
 *
 * <h1>VersionFactoryTest</h1>
 * <p>Testing the version factory withoutthe pom context.</p>
 *
 * @author sido
 */
public class VersionFactoryTest extends AbstractSemverMavenPluginTest {

    private final Log LOG = null;

    private SemverConfiguration getConFiguration() {
        SemverConfiguration configuration = new SemverConfiguration(null);
        configuration.setRunMode(SemverMavenPlugin.RUNMODE.RELEASE_BRANCH_HOSEE);
        configuration.setBranchVersion("6.4.0");
        return configuration;
    }

    @Test
    public void createReleaseTest() {

        Map<SemverMavenPlugin.RAW_VERSION, String> rawVersions = new HashMap<SemverMavenPlugin.RAW_VERSION, String>();
        rawVersions.put(SemverMavenPlugin.RAW_VERSION.DEVELOPMENT, "1.0.1-SNAPSHOT");
        rawVersions.put(SemverMavenPlugin.RAW_VERSION.RELEASE, "1.0.0");
        rawVersions.put(SemverMavenPlugin.RAW_VERSION.PATCH, "0");
        rawVersions.put(SemverMavenPlugin.RAW_VERSION.MINOR, "0");
        rawVersions.put(SemverMavenPlugin.RAW_VERSION.MAJOR, "1");

        Map<VersionFactory.FINAL_VERSION, String> finalVersions = VersionFactory.determineReleaseVersions(LOG, getConFiguration(), null, rawVersions);
        assertEquals(finalVersions.get(VersionFactory.FINAL_VERSION.RELEASE), "1.0.0");
        assertEquals(finalVersions.get(VersionFactory.FINAL_VERSION.DEVELOPMENT), "1.0.1-SNAPSHOT");
        assertEquals(finalVersions.get(VersionFactory.FINAL_VERSION.SCM), "1.0.0");
    }

    @Test
    public void createReleaseBranchTest() {
        Map<SemverMavenPlugin.RAW_VERSION, String> rawVersions = new HashMap<SemverMavenPlugin.RAW_VERSION, String>();
        rawVersions.put(SemverMavenPlugin.RAW_VERSION.DEVELOPMENT, "1.0.1-SNAPSHOT");
        rawVersions.put(SemverMavenPlugin.RAW_VERSION.RELEASE, "1.0.0");
        rawVersions.put(SemverMavenPlugin.RAW_VERSION.PATCH, "0");
        rawVersions.put(SemverMavenPlugin.RAW_VERSION.MINOR, "0");
        rawVersions.put(SemverMavenPlugin.RAW_VERSION.MAJOR, "1");

        Map<VersionFactory.FINAL_VERSION, String> finalVersions = VersionFactory.determineReleaseBranchVersions(LOG, getConFiguration(), null, rawVersions);
        assertEquals(finalVersions.get(VersionFactory.FINAL_VERSION.RELEASE), "6.4.0-001000000");
        assertEquals(finalVersions.get(VersionFactory.FINAL_VERSION.DEVELOPMENT), "1.0.1-SNAPSHOT");
        assertEquals(finalVersions.get(VersionFactory.FINAL_VERSION.SCM), "6.4.0-001000000+1.0.0");
    }


}




