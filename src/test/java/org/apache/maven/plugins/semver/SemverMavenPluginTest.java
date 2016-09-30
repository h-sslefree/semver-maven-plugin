package org.apache.maven.plugins.semver;

import java.io.File;

import org.apache.maven.plugins.semver.goal.SemverMavenPluginGoalMajor;
import org.apache.maven.plugins.semver.goal.SemverMavenPluginGoalMinor;
import org.apache.maven.plugins.semver.goal.SemverMavenPluginGoalPatch;
import org.apache.maven.plugins.test.mojo.AbstractSemverMojoTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * <p>Bij het runnen van de tests moet eerst het project met maven zijn gebuild</p>
 * 
 * 
 * 
 * @author sido
 *
 */
public class SemverMavenPluginTest extends AbstractSemverMojoTestCase {

  
  protected void setUp() throws Exception {
    super.setUp();
  }

  @Test
  public void testSemverMavenPluginPatch() throws Exception {

    File testPom = new File(getBasedir(), "src/test/resources/org/apache/maven/plugins/semver/pom.xml");
    Assert.assertTrue(testPom.exists());

    SemverMavenPluginGoalPatch mojo = (SemverMavenPluginGoalPatch)lookupConfiguredMojo(testPom, "patch");
    Assert.assertNotNull(mojo);
    mojo.execute();
  }

  @Test
  public void testSemverMavenPluginMinor() throws Exception {

    File testPom = new File(getBasedir(), "src/test/resources/org/apache/maven/plugins/semver/pom.xml");
    Assert.assertTrue(testPom.exists());

    SemverMavenPluginGoalMinor mojo = (SemverMavenPluginGoalMinor)lookupConfiguredMojo(testPom, "minor");
    Assert.assertNotNull(mojo);
    mojo.execute();
  }
  
  @Test
  public void testSemverMavenPluginMajor() throws Exception {

    File testPom = new File(getBasedir(), "src/test/resources/org/apache/maven/plugins/semver/pom.xml");
    Assert.assertTrue(testPom.exists());

    SemverMavenPluginGoalMajor mojo = (SemverMavenPluginGoalMajor)lookupConfiguredMojo(testPom, "major");
    Assert.assertNotNull(mojo);
    mojo.execute();
  }
}
