package org.apache.maven.plugins.semver.test.runmodes;

import org.apache.maven.plugins.semver.goals.SemverMavenPluginGoalMajor;
import org.apache.maven.plugins.semver.goals.SemverMavenPluginGoalMinor;
import org.apache.maven.plugins.semver.goals.SemverMavenPluginGoalPatch;
import org.apache.maven.plugins.semver.test.AbstractSemverMavenPluginTest;
import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * <p>
 * Bij het runnen van de tests moet eerst het project met maven zijn gebuild
 * </p>
 * LETOP de configuratie van de plugin in test wordt niet geladen!!!!!
 * 
 * 
 * @author sido
 *
 */
public class ReleaseTest extends AbstractSemverMavenPluginTest {



	@Test
	public void testSemverMavenPluginPatchRelease() throws Exception {
		SemverMavenPluginGoalPatch mojo = (SemverMavenPluginGoalPatch) mojoRule.lookupConfiguredMojo(loadPom("release/"), "patch");
		Assert.assertNotNull(mojo);
		mojo.execute();
	}

	@Test
	public void testSemverMavenPluginMinorRelease() throws Exception {
		SemverMavenPluginGoalMinor mojo = (SemverMavenPluginGoalMinor) mojoRule.lookupConfiguredMojo(loadPom("release/"), "minor");
		Assert.assertNotNull(mojo);
		mojo.execute();
	}

	@Test
	public void testSemverMavenPluginMajorRelease() throws Exception {
		SemverMavenPluginGoalMajor mojo = (SemverMavenPluginGoalMajor) mojoRule.lookupConfiguredMojo(loadPom("release/"), "major");
		Assert.assertNotNull(mojo);
		mojo.execute();
	}

}
