package com.bicat.semver.maven.plugin;

import java.io.File;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;

import com.bicat.semver.maven.plugin.goal.SemverMavenPluginGoalMinor;
import com.bicat.semver.maven.plugin.goal.SemverMavenPluginGoalPatch;

public class SemverMavenPluginTest extends AbstractMojoTestCase {

  /**
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp() throws Exception {
    super.setUp();
  }

  /**
   * @throws Exception
   */
  public void testSemverMavenPluginGoalPatch() throws Exception {
    File testPom = new File(getBasedir(), "src/test/resources/com/bicat/semver/maven/plugin/test-plugin-pom.xml");

    SemverMavenPluginGoalPatch mojo = (SemverMavenPluginGoalPatch)lookupMojo("com.bicat:semver-maven-plugin:1.0.0-SNAPSHOT:patch", testPom);

    assertNotNull(mojo);
  }
  
  /**
   * @throws Exception
   */
  public void testSemverMavenPluginGoalMinor() throws Exception {
    File testPom = new File(getBasedir(), "src/test/resources/unit/basic-test/basic-test-plugin-pom.xml");

    SemverMavenPluginGoalMinor mojo = (SemverMavenPluginGoalMinor)lookupMojo("com.bicat:semver-maven-plugin:minor", testPom);

    assertNotNull(mojo);
  }
}
