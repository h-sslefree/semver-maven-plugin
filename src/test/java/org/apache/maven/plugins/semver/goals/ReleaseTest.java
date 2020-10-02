package org.apache.maven.plugins.semver.goals;

import java.io.File;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.junit.Assert;
import org.junit.Test;

public class ReleaseTest extends AbstractMojoTestCase {

  protected void setUp() throws Exception {
    super.setUp();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  @Test
  public void testSemverMavenPluginPatchRelease() throws Exception {
    File pom =
        getTestFile(
            "src/test/resources/org/apache/maven/plugins/semver/goals/release/branche/pom.xml");
    assertNotNull(pom);
    assertTrue(pom.exists());

    SemverMavenPluginGoalPatch mojo = (SemverMavenPluginGoalPatch) lookupMojo("patch", pom);
    Assert.assertNotNull(mojo);
    mojo.execute();
  }

  @Test
  public void testSemverMavenPluginMinorRelease() throws Exception {
    File pom =
        getTestFile(
            "src/test/resources/org/apache/maven/plugins/semver/goals/release/branch/pom.xml");
    assertNotNull(pom);
    assertTrue(pom.exists());
    SemverMavenPluginGoalMinor mojo = (SemverMavenPluginGoalMinor) lookupMojo("minor", pom);
    Assert.assertNotNull(mojo);
    mojo.execute();
  }

  @Test
  public void testSemverMavenPluginMajorRelease() throws Exception {

    File pom =
        getTestFile("src/test/resources/org/apache/maven/plugins/semver/goals/release/pom.xml");
    assertNotNull(pom);
    assertTrue(pom.exists());

    SemverMavenPluginGoalMajor mojo = (SemverMavenPluginGoalMajor) lookupMojo("major", pom);
    Assert.assertNotNull(mojo);
    mojo.execute();
  }
}
