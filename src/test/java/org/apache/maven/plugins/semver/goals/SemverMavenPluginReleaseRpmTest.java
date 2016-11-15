package org.apache.maven.plugins.semver.goals;

import static org.junit.Assert.assertEquals;

import org.apache.maven.plugins.semver.test.AbstractSemverMavenPluginTest;
import org.apache.maven.plugins.semver.SemverMavenPlugin.RUNMODE;
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
public class SemverMavenPluginReleaseRpmTest extends AbstractSemverMavenPluginTest {

	@Test
	public void testSemverMavenPluginPatchRpm() throws Exception {
		
		SemverMavenPluginGoalPatch mojo = (SemverMavenPluginGoalPatch) mojoRule.lookupConfiguredMojo(loadPom("rpm/"), "patch");
		Assert.assertNotNull(mojo);
		mojo.execute();
		assertEquals("6.3.4", mojo.getConfiguration().getBranchVersion());
		assertEquals(RUNMODE.RELEASE_RPM, mojo.getConfiguration().getRunMode());
	}

	@Test
	public void testSemverMavenPluginMinorRpm() throws Exception {
		SemverMavenPluginGoalMinor mojo = (SemverMavenPluginGoalMinor) mojoRule.lookupConfiguredMojo(loadPom("rpm/"), "minor");
		Assert.assertNotNull(mojo);
		mojo.execute();
		assertEquals("6.3.4", mojo.getConfiguration().getBranchVersion());
		assertEquals(RUNMODE.RELEASE_RPM, mojo.getConfiguration().getRunMode());
	}

	@Test
	public void testSemverMavenPluginMajorRpm() throws Exception {
		SemverMavenPluginGoalMajor mojo = (SemverMavenPluginGoalMajor) mojoRule.lookupConfiguredMojo(loadPom("rpm/"), "major");
		Assert.assertNotNull(mojo);
		mojo.execute();
		assertEquals("6.3.4", mojo.getConfiguration().getBranchVersion());
		assertEquals(RUNMODE.RELEASE_RPM, mojo.getConfiguration().getRunMode());
	}

}
