package org.apache.maven.plugins.semver;

import java.io.File;

import org.apache.maven.plugins.semver.goals.SemverMavenPluginGoalMajor;
import org.apache.maven.plugins.semver.goals.SemverMavenPluginGoalMinor;
import org.apache.maven.plugins.semver.goals.SemverMavenPluginGoalPatch;
import org.apache.maven.plugins.test.mojo.AbstractSemverMojoTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * <p>Bij het runnen van de tests moet eerst het project met maven zijn gebuild</p>
 * LETOP de configuratie van de plugin in test wordt niet geladen!!!!!
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
  public void testSemverMavenPluginPatchRelease() throws Exception {

    File testPom = new File(getBasedir(), "src/test/resources/org/apache/maven/plugins/semver/release/pom.xml");
    Assert.assertTrue(testPom.exists());

    SemverMavenPluginGoalPatch mojo = (SemverMavenPluginGoalPatch)lookupConfiguredMojo(testPom, "patch");
    Assert.assertNotNull(mojo);
    mojo.execute();
  }

  @Test
  public void testSemverMavenPluginMinorRelease() throws Exception {

    File testPom = new File(getBasedir(), "src/test/resources/org/apache/maven/plugins/semver/release/pom.xml");
    Assert.assertTrue(testPom.exists());

    SemverMavenPluginGoalMinor mojo = (SemverMavenPluginGoalMinor)lookupConfiguredMojo(testPom, "minor");
    Assert.assertNotNull(mojo);
    mojo.execute();
  }
  
  @Test
  public void testSemverMavenPluginMajorRelease() throws Exception {

    File testPom = new File(getBasedir(), "src/test/resources/org/apache/maven/plugins/semver/release/pom.xml");
    Assert.assertTrue(testPom.exists());

    SemverMavenPluginGoalMajor mojo = (SemverMavenPluginGoalMajor)lookupConfiguredMojo(testPom, "major");
    Assert.assertNotNull(mojo);
    mojo.execute();
  }
  
  @Test
  public void testSemverMavenPluginPatchRpm() throws Exception {

    File testPom = new File(getBasedir(), "src/test/resources/org/apache/maven/plugins/semver/rpm/pom.xml");
    Assert.assertTrue(testPom.exists());

    SemverMavenPluginGoalPatch mojo = (SemverMavenPluginGoalPatch)lookupConfiguredMojo(testPom, "patch");
    Assert.assertNotNull(mojo);
    mojo.execute();
  }

  @Test
  public void testSemverMavenPluginMinorRpm() throws Exception {

    File testPom = new File(getBasedir(), "src/test/resources/org/apache/maven/plugins/semver/rpm/pom.xml");
    Assert.assertTrue(testPom.exists());

    SemverMavenPluginGoalMinor mojo = (SemverMavenPluginGoalMinor)lookupConfiguredMojo(testPom, "minor");
    Assert.assertNotNull(mojo);
    mojo.execute();
  }
  
  @Test
  public void testSemverMavenPluginMajorRpm() throws Exception {

    File testPom = new File(getBasedir(), "src/test/resources/org/apache/maven/plugins/semver/rpm/pom.xml");
    Assert.assertTrue(testPom.exists());

    SemverMavenPluginGoalMajor mojo = (SemverMavenPluginGoalMajor)lookupConfiguredMojo(testPom, "major");
    Assert.assertNotNull(mojo);
    mojo.execute();
  }
  
  
}
