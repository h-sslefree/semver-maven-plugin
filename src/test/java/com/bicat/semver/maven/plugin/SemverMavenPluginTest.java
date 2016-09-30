package com.bicat.semver.maven.plugin;

import java.io.File;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.junit.Assert;
import org.junit.Test;

import com.bicat.semver.maven.plugin.goal.SemverMavenPluginGoalPatch;

/**
 * 
 * <p>Bij het runnen van de tests moet eerst het project met maven zijn gebuild</p>
 * 
 * 
 * 
 * @author sido
 *
 */
public class SemverMavenPluginTest extends AbstractMojoTestCase {

  
  protected void setUp() throws Exception {
    super.setUp();
  }

  @Test
  public void testSemverMavenPluginPatch() throws Exception {

    File testPom = new File(getBasedir(), "src/test/resources/com/bicat/semver/maven/plugin/pom.xml");
    Assert.assertTrue(testPom.exists());

    SemverMavenPluginGoalPatch mojo = (SemverMavenPluginGoalPatch)lookupMojo("patch", testPom);
    Assert.assertNotNull(mojo);
    mojo.execute();
  }

}
