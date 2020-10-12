package org.apache.maven.plugins.semver.providers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Scm;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugins.semver.providers.VersionProvider.FINAL_VERSION;
import org.apache.maven.plugins.semver.providers.VersionProvider.RAW_VERSION;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PomProviderImplTest {
  @Mock private BuildPluginManager buildPluginManager;
  @Mock private MavenSession mavenSession;
  @Mock private MavenProject mavenProject;
  @Mock private RepositoryProvider repositoryProvider;

  private PomProvider pomProvider;
  private VersionProvider versionProvider;

  @Before
  public void setUp() {
    Scm scm = new Scm();
    pomProvider =
        new PomProviderImpl(mavenProject, mavenSession, buildPluginManager, repositoryProvider);
    versionProvider = new VersionProviderImpl(repositoryProvider);
    when(mavenProject.getScm()).thenReturn(scm);
  }

  @Test
  public void createReleaseTest() {

    Map<RAW_VERSION, String> rawVersions = new HashMap<>();
    rawVersions.put(RAW_VERSION.DEVELOPMENT, "1.0.1-SNAPSHOT");
    rawVersions.put(RAW_VERSION.RELEASE, "1.0.0");
    rawVersions.put(RAW_VERSION.PATCH, "0");
    rawVersions.put(RAW_VERSION.MINOR, "0");
    rawVersions.put(RAW_VERSION.MAJOR, "1");

    pomProvider.createReleasePom(versionProvider.determineReleaseVersions(rawVersions));

    Map<FINAL_VERSION, String> finalVersions =
        versionProvider.determineReleaseVersions(rawVersions);
    assertEquals("1.0.0", finalVersions.get(FINAL_VERSION.RELEASE));
    assertEquals("1.0.1-SNAPSHOT", finalVersions.get(FINAL_VERSION.DEVELOPMENT));
    assertEquals("1.0.0", finalVersions.get(FINAL_VERSION.SCM));
  }
}
