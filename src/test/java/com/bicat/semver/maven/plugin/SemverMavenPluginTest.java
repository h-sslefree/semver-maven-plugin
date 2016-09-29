package com.bicat.semver.maven.plugin;

import java.io.File;

import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.plugin.testing.resources.TestResources;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import com.bicat.semver.maven.plugin.goal.SemverMavenPluginGoalPatch;

public class SemverMavenPluginTest {

	@Rule
	public MojoRule rule = new MojoRule();

	@Rule
	public TestResources resources = new TestResources();

	@Test
	public void testSemverMavenPluginProject() throws Exception {

		File projectCopy = this.resources.getBasedir("semver-maven-plugin");
		File pom = new File(projectCopy, "pom.xml");
		Assert.assertNotNull(pom);
		Assert.assertTrue(pom.exists());

		SemverMavenPluginGoalPatch mojo = (SemverMavenPluginGoalPatch) this.rule.lookupMojo("patch", pom);
		Assert.assertNotNull(mojo);
		mojo.execute();
	}

}
