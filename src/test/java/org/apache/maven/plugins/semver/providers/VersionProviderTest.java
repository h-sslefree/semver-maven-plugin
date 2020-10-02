package org.apache.maven.plugins.semver.providers;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import org.apache.maven.plugins.semver.configuration.SemverConfiguration;
import org.apache.maven.plugins.semver.providers.VersionProvider.FINAL_VERSION;
import org.apache.maven.plugins.semver.providers.VersionProvider.RAW_VERSION;
import org.apache.maven.plugins.semver.runmodes.RunMode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class VersionProviderTest {

  @Mock private RepositoryProvider repositoryProvider;

  private VersionProvider versionProvider;

  @Before
  public void setUp() {
    versionProvider = new VersionProviderImpl(repositoryProvider);
  }

  private SemverConfiguration getConfigurationRelease() {
    SemverConfiguration configuration = new SemverConfiguration(null);
    configuration.setRunMode(RunMode.RUN_MODE.RELEASE);
    return configuration;
  }

  private SemverConfiguration getConfigurationReleaseBranch() {
    SemverConfiguration configuration = new SemverConfiguration(null);
    configuration.setRunMode(RunMode.RUN_MODE.RELEASE_BRANCH);
    configuration.setBranchVersion("6.4.0");
    return configuration;
  }

  private SemverConfiguration getConfigurationReleaseBranchRpm() {
    SemverConfiguration configuration = new SemverConfiguration(null);
    configuration.setRunMode(RunMode.RUN_MODE.RELEASE_BRANCH_RPM);
    configuration.setBranchVersion("6.4.0");
    return configuration;
  }

  private SemverConfiguration getConfigurationNative() {
    SemverConfiguration configuration = new SemverConfiguration(null);
    configuration.setRunMode(RunMode.RUN_MODE.NATIVE);
    return configuration;
  }

  private SemverConfiguration getConfigurationNativeBranch() {
    SemverConfiguration configuration = new SemverConfiguration(null);
    configuration.setRunMode(RunMode.RUN_MODE.NATIVE_BRANCH);
    configuration.setBranchVersion("6.4.0");
    return configuration;
  }

  @Test
  public void createReleaseTest() {
    Map<RAW_VERSION, String> rawVersions = new HashMap<>();
    rawVersions.put(RAW_VERSION.DEVELOPMENT, "1.0.1-SNAPSHOT");
    rawVersions.put(RAW_VERSION.RELEASE, "1.0.0");
    rawVersions.put(RAW_VERSION.PATCH, "0");
    rawVersions.put(RAW_VERSION.MINOR, "0");
    rawVersions.put(RAW_VERSION.MAJOR, "1");

    Map<FINAL_VERSION, String> finalVersions =
        versionProvider.determineReleaseVersions(rawVersions);
    assertEquals(finalVersions.get(FINAL_VERSION.RELEASE), "1.0.0");
    assertEquals(finalVersions.get(FINAL_VERSION.DEVELOPMENT), "1.0.1-SNAPSHOT");
    assertEquals(finalVersions.get(FINAL_VERSION.SCM), "1.0.0");
  }

  @Test
  public void createReleaseBranchTest() {

    Map<RAW_VERSION, String> rawVersions = new HashMap<>();
    rawVersions.put(RAW_VERSION.DEVELOPMENT, "1.0.1-SNAPSHOT");
    rawVersions.put(RAW_VERSION.RELEASE, "1.0.0");
    rawVersions.put(RAW_VERSION.PATCH, "0");
    rawVersions.put(RAW_VERSION.MINOR, "0");
    rawVersions.put(RAW_VERSION.MAJOR, "1");

    Map<FINAL_VERSION, String> finalVersions =
        versionProvider.determineReleaseBranchVersions(
            rawVersions,
            getConfigurationReleaseBranch().getRunMode(),
            getConfigurationReleaseBranch().getMetaData(),
            getConfigurationReleaseBranch().getBranchVersion());
    assertEquals(finalVersions.get(FINAL_VERSION.RELEASE), "6.4.0-1.0.0");
    assertEquals(finalVersions.get(FINAL_VERSION.DEVELOPMENT), "1.0.1-SNAPSHOT");
    assertEquals(finalVersions.get(FINAL_VERSION.SCM), "6.4.0-1.0.0");
  }

  @Test
  public void createReleaseBranchRpmTest() {

    Map<RAW_VERSION, String> rawVersions = new HashMap<>();
    rawVersions.put(RAW_VERSION.DEVELOPMENT, "1.0.1-SNAPSHOT");
    rawVersions.put(RAW_VERSION.RELEASE, "1.0.0");
    rawVersions.put(RAW_VERSION.PATCH, "0");
    rawVersions.put(RAW_VERSION.MINOR, "0");
    rawVersions.put(RAW_VERSION.MAJOR, "1");

    Map<FINAL_VERSION, String> finalVersions =
        versionProvider.determineReleaseBranchVersions(
            rawVersions,
            getConfigurationReleaseBranchRpm().getRunMode(),
            getConfigurationReleaseBranchRpm().getMetaData(),
            getConfigurationReleaseBranchRpm().getBranchVersion());
    assertEquals(finalVersions.get(FINAL_VERSION.RELEASE), "6.4.0-001000000");
    assertEquals(finalVersions.get(FINAL_VERSION.DEVELOPMENT), "1.0.1-SNAPSHOT");
    assertEquals(finalVersions.get(FINAL_VERSION.SCM), "6.4.0-001000000+1.0.0");
  }

  @Test
  public void createNativeTest() {

    Map<RAW_VERSION, String> rawVersions = new HashMap<>();
    rawVersions.put(RAW_VERSION.DEVELOPMENT, "1.0.1-SNAPSHOT");
    rawVersions.put(RAW_VERSION.RELEASE, "1.0.0");
    rawVersions.put(RAW_VERSION.PATCH, "0");
    rawVersions.put(RAW_VERSION.MINOR, "0");
    rawVersions.put(RAW_VERSION.MAJOR, "1");

    Map<FINAL_VERSION, String> finalVersions =
        versionProvider.determineReleaseVersions(rawVersions);
    assertEquals(finalVersions.get(FINAL_VERSION.RELEASE), "1.0.0");
    assertEquals(finalVersions.get(FINAL_VERSION.DEVELOPMENT), "1.0.1-SNAPSHOT");
    assertEquals(finalVersions.get(FINAL_VERSION.SCM), "1.0.0");
  }

  @Test
  public void createNativeBranchTest() {

    Map<RAW_VERSION, String> rawVersions = new HashMap<>();
    rawVersions.put(RAW_VERSION.DEVELOPMENT, "1.0.1-SNAPSHOT");
    rawVersions.put(RAW_VERSION.RELEASE, "1.0.0");
    rawVersions.put(RAW_VERSION.PATCH, "0");
    rawVersions.put(RAW_VERSION.MINOR, "0");
    rawVersions.put(RAW_VERSION.MAJOR, "1");

    Map<FINAL_VERSION, String> finalVersions =
        versionProvider.determineReleaseBranchVersions(
            rawVersions,
            getConfigurationNativeBranch().getRunMode(),
            getConfigurationNativeBranch().getMetaData(),
            getConfigurationNativeBranch().getBranchVersion());
    assertEquals(finalVersions.get(FINAL_VERSION.RELEASE), "6.4.0-1.0.0");
    assertEquals(finalVersions.get(FINAL_VERSION.DEVELOPMENT), "1.0.1-SNAPSHOT");
    assertEquals(finalVersions.get(FINAL_VERSION.SCM), "6.4.0-1.0.0");
  }
}
