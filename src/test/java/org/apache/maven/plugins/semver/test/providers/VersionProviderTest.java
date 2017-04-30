package org.apache.maven.plugins.semver.test.providers;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.semver.SemverMavenPlugin;
import org.apache.maven.plugins.semver.configuration.SemverConfiguration;
import org.apache.maven.plugins.semver.providers.VersionProvider;
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
public class VersionProviderTest extends AbstractSemverMavenPluginTest {

    private final Log LOG = null;

    private SemverConfiguration getConfigurationRelease() {
        SemverConfiguration configuration = new SemverConfiguration(null);
        configuration.setRunMode(SemverMavenPlugin.RUNMODE.RELEASE);
        return configuration;
    }

    private SemverConfiguration getConfigurationReleaseBranch() {
        SemverConfiguration configuration = new SemverConfiguration(null);
        configuration.setRunMode(SemverMavenPlugin.RUNMODE.RELEASE_BRANCH);
        configuration.setBranchVersion("6.4.0");
        return configuration;
    }

    private SemverConfiguration getConfigurationReleaseBranchHosee() {
        SemverConfiguration configuration = new SemverConfiguration(null);
        configuration.setRunMode(SemverMavenPlugin.RUNMODE.RELEASE_BRANCH_HOSEE);
        configuration.setBranchVersion("6.4.0");
        return configuration;
    }

    private SemverConfiguration getConfigurationNative() {
        SemverConfiguration configuration = new SemverConfiguration(null);
        configuration.setRunMode(SemverMavenPlugin.RUNMODE.NATIVE);
        return configuration;
    }

    private SemverConfiguration getConfigurationNativeBranch() {
        SemverConfiguration configuration = new SemverConfiguration(null);
        configuration.setRunMode(SemverMavenPlugin.RUNMODE.NATIVE_BRANCH);
        configuration.setBranchVersion("6.4.0");
        return configuration;
    }

    @Test
    public void createReleaseTest() {

        VersionProvider versionProvider = new VersionProvider(LOG, getConfigurationRelease());

        Map<SemverMavenPlugin.RAW_VERSION, String> rawVersions = new HashMap<>();
        rawVersions.put(SemverMavenPlugin.RAW_VERSION.DEVELOPMENT, "1.0.1-SNAPSHOT");
        rawVersions.put(SemverMavenPlugin.RAW_VERSION.RELEASE, "1.0.0");
        rawVersions.put(SemverMavenPlugin.RAW_VERSION.PATCH, "0");
        rawVersions.put(SemverMavenPlugin.RAW_VERSION.MINOR, "0");
        rawVersions.put(SemverMavenPlugin.RAW_VERSION.MAJOR, "1");

        Map<VersionProvider.FINAL_VERSION, String> finalVersions = versionProvider.determineReleaseVersions(rawVersions);
        assertEquals(finalVersions.get(VersionProvider.FINAL_VERSION.RELEASE), "1.0.0");
        assertEquals(finalVersions.get(VersionProvider.FINAL_VERSION.DEVELOPMENT), "1.0.1-SNAPSHOT");
        assertEquals(finalVersions.get(VersionProvider.FINAL_VERSION.SCM), "1.0.0");
    }

    @Test
    public void createReleaseBranchTest() {

        VersionProvider versionProvider = new VersionProvider(LOG, getConfigurationReleaseBranch());

        Map<SemverMavenPlugin.RAW_VERSION, String> rawVersions = new HashMap<>();
        rawVersions.put(SemverMavenPlugin.RAW_VERSION.DEVELOPMENT, "1.0.1-SNAPSHOT");
        rawVersions.put(SemverMavenPlugin.RAW_VERSION.RELEASE, "1.0.0");
        rawVersions.put(SemverMavenPlugin.RAW_VERSION.PATCH, "0");
        rawVersions.put(SemverMavenPlugin.RAW_VERSION.MINOR, "0");
        rawVersions.put(SemverMavenPlugin.RAW_VERSION.MAJOR, "1");

        Map<VersionProvider.FINAL_VERSION, String> finalVersions = versionProvider.determineReleaseBranchVersions(rawVersions);
        assertEquals(finalVersions.get(VersionProvider.FINAL_VERSION.RELEASE), "6.4.0-1.0.0");
        assertEquals(finalVersions.get(VersionProvider.FINAL_VERSION.DEVELOPMENT), "1.0.1-SNAPSHOT");
        assertEquals(finalVersions.get(VersionProvider.FINAL_VERSION.SCM), "6.4.0-1.0.0");
    }

    @Test
    public void createReleaseBranchHoseeTest() {

        VersionProvider versionProvider = new VersionProvider(LOG, getConfigurationReleaseBranchHosee());

        Map<SemverMavenPlugin.RAW_VERSION, String> rawVersions = new HashMap<>();
        rawVersions.put(SemverMavenPlugin.RAW_VERSION.DEVELOPMENT, "1.0.1-SNAPSHOT");
        rawVersions.put(SemverMavenPlugin.RAW_VERSION.RELEASE, "1.0.0");
        rawVersions.put(SemverMavenPlugin.RAW_VERSION.PATCH, "0");
        rawVersions.put(SemverMavenPlugin.RAW_VERSION.MINOR, "0");
        rawVersions.put(SemverMavenPlugin.RAW_VERSION.MAJOR, "1");

        Map<VersionProvider.FINAL_VERSION, String> finalVersions = versionProvider.determineReleaseBranchVersions(rawVersions);
        assertEquals(finalVersions.get(VersionProvider.FINAL_VERSION.RELEASE), "6.4.0-001000000");
        assertEquals(finalVersions.get(VersionProvider.FINAL_VERSION.DEVELOPMENT), "1.0.1-SNAPSHOT");
        assertEquals(finalVersions.get(VersionProvider.FINAL_VERSION.SCM), "6.4.0-001000000+1.0.0");
    }



    @Test
    public void createNativeTest() {

        VersionProvider versionProvider = new VersionProvider(LOG,  getConfigurationNative());

        Map<SemverMavenPlugin.RAW_VERSION, String> rawVersions = new HashMap<>();
        rawVersions.put(SemverMavenPlugin.RAW_VERSION.DEVELOPMENT, "1.0.1-SNAPSHOT");
        rawVersions.put(SemverMavenPlugin.RAW_VERSION.RELEASE, "1.0.0");
        rawVersions.put(SemverMavenPlugin.RAW_VERSION.PATCH, "0");
        rawVersions.put(SemverMavenPlugin.RAW_VERSION.MINOR, "0");
        rawVersions.put(SemverMavenPlugin.RAW_VERSION.MAJOR, "1");

        Map<VersionProvider.FINAL_VERSION, String> finalVersions = versionProvider.determineReleaseVersions(rawVersions);
        assertEquals(finalVersions.get(VersionProvider.FINAL_VERSION.RELEASE), "1.0.0");
        assertEquals(finalVersions.get(VersionProvider.FINAL_VERSION.DEVELOPMENT), "1.0.1-SNAPSHOT");
        assertEquals(finalVersions.get(VersionProvider.FINAL_VERSION.SCM), "1.0.0");
    }

    @Test
    public void createNativeBranchTest() {

        VersionProvider versionProvider = new VersionProvider(LOG, getConfigurationNativeBranch());

        Map<SemverMavenPlugin.RAW_VERSION, String> rawVersions = new HashMap<s>();
        rawVersions.put(SemverMavenPlugin.RAW_VERSION.DEVELOPMENT, "1.0.1-SNAPSHOT");
        rawVersions.put(SemverMavenPlugin.RAW_VERSION.RELEASE, "1.0.0");
        rawVersions.put(SemverMavenPlugin.RAW_VERSION.PATCH, "0");
        rawVersions.put(SemverMavenPlugin.RAW_VERSION.MINOR, "0");
        rawVersions.put(SemverMavenPlugin.RAW_VERSION.MAJOR, "1");

        Map<VersionProvider.FINAL_VERSION, String> finalVersions = versionProvider.determineReleaseBranchVersions(rawVersions);
        assertEquals(finalVersions.get(VersionProvider.FINAL_VERSION.RELEASE), "6.4.0-1.0.0");
        assertEquals(finalVersions.get(VersionProvider.FINAL_VERSION.DEVELOPMENT), "1.0.1-SNAPSHOT");
        assertEquals(finalVersions.get(VersionProvider.FINAL_VERSION.SCM), "6.4.0-1.0.0");
    }

}




