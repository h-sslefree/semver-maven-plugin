package org.apache.maven.plugins.semver.test.goals;

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
public class ReleaseBranchTest extends AbstractSemverMavenPluginTest{

	@Test
	public void testSemverMavenPluginPatchBranch() throws Exception {
		SemverMavenPluginGoalPatch mojo = (SemverMavenPluginGoalPatch) mojoRule.lookupConfiguredMojo(loadPom("rpm/"), "patch");
		Assert.assertNotNull(mojo);
		mojo.execute();
	}

	@Test
	public void testSemverMavenPluginMinorBranch() throws Exception {
		SemverMavenPluginGoalMinor mojo = (SemverMavenPluginGoalMinor) mojoRule.lookupConfiguredMojo(loadPom("rpm/"), "minor");
		Assert.assertNotNull(mojo);
		mojo.execute();
	}

	@Test
	public void testSemverMavenPluginMajorBranch() throws Exception {
		SemverMavenPluginGoalMajor mojo = (SemverMavenPluginGoalMajor) mojoRule.lookupConfiguredMojo(loadPom("rpm/"), "major");
		Assert.assertNotNull(mojo);
		mojo.execute();
	}

}
