package org.apache.maven.plugins.semver.goals;

import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.semver.SemverMavenPlugin;
import org.apache.maven.plugins.semver.providers.BranchProvider;
import org.apache.maven.plugins.semver.providers.PomProvider;
import org.apache.maven.plugins.semver.providers.RepositoryProvider;
import org.apache.maven.plugins.semver.providers.VersionProvider;

import javax.inject.Inject;
import java.io.File;

import static org.apache.maven.plugins.semver.goals.SemverGoal.SEMVER_GOAL.PATCH;

/**
 * <h1>Determine PATCH version for MAVEN-project.</h1>
 *
 * <p>This advances the tag of the project and the pom.xml version.
 *
 * <p>Example:
 *
 * <pre>{@code
 * <version>x.x.1</version>
 * to
 * <version>x.x.2</version>
 * }</pre>
 *
 * <p>Run the test-phase when this goal is executed.
 *
 * @author sido
 */
@Mojo(name = "patch")
@Execute(phase = LifecyclePhase.TEST)
public class SemverMavenPluginGoalPatch extends SemverMavenPlugin
{

	@Inject
	public SemverMavenPluginGoalPatch(VersionProvider versionProvider, PomProvider pomProvider,
			RepositoryProvider repositoryProvider, BranchProvider branchProvider)
	{
		super(versionProvider, pomProvider, repositoryProvider, branchProvider);
	}

	@Override
	public void execute()
	{
		String pomVersion = mavenProject.getVersion();
		String scmConnection = null;
		File scmRoot = null;
		if (getConfiguration().pushTags() && mavenProject.getScm() != null)
		{
			scmConnection = mavenProject.getScm().getConnection();
			scmRoot = mavenProject.getBasedir();
			getRepositoryProvider().initialize(scmRoot, scmConnection, getConfiguration().getScmUsername(),
					getConfiguration().getScmPassword());
		}
		else if (getConfiguration().pushTags())
		{
			logger.error(" * No SCM information supplied");
			logger.error(" * Please described the scm block in the pom.xml");
			Runtime.getRuntime().exit(1);
		}

		logger.info(FUNCTION_LINE_BREAK);
		logger.info("Semver-goal                        : {}", PATCH.getDescription());
		logger.info("Run-mode                           : {}", getConfiguration().getRunMode());
		logger.info("Version from POM                   : [ {} ]", pomVersion);
		if (getConfiguration().pushTags())
		{
			logger.info("SCM-connection                     : {}", scmConnection);
			logger.info("SCM-root                           : {}", scmRoot);
		}
		logger.info(FUNCTION_LINE_BREAK);

		try
		{
			runModeImpl.execute(PATCH, getConfiguration(), pomVersion);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage());
		}
	}
}
