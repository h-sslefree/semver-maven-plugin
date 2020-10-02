package org.apache.maven.plugins.semver.goals;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.semver.SemverMavenPlugin;
import org.apache.maven.plugins.semver.providers.BranchProvider;
import org.apache.maven.plugins.semver.providers.PomProvider;
import org.apache.maven.plugins.semver.providers.RepositoryProvider;
import org.apache.maven.plugins.semver.providers.VersionProvider;
import org.eclipse.jgit.lib.Ref;

import javax.inject.Inject;
import java.io.File;
import java.util.List;

import static java.text.MessageFormat.format;

/**
 * used to be a goal that was used before a build on a BUILD-server (HUDSON).
 *
 * <p>Can be phased out when the BUILD-server jobs are obsolete.
 *
 * @author sido
 * @deprecated
 */
@Deprecated
@Mojo(name = "cleanup-git-tags")
public class SemverMavenPluginGoalCleanupGitTags extends SemverMavenPlugin
{

	@Inject
	public SemverMavenPluginGoalCleanupGitTags(VersionProvider versionProvider, PomProvider pomProvider,
			RepositoryProvider repositoryProvider, BranchProvider branchProvider)
	{
		super(versionProvider, pomProvider, repositoryProvider, branchProvider);
	}

	@Override
	public void execute()
	{

		String version = mavenProject.getVersion();
		String scmConnection = mavenProject.getScm().getConnection();
		File scmRoot = mavenProject.getBasedir();

		logger.info("Semver-goal                       : CLEANUP-GIT-TAGS");
		logger.info(format("Run-mode                          : {}", getConfiguration().getRunMode()));
		logger.info(format("Version from POM                  : {}", version));
		logger.info(format("SCM-connection                    : {}", scmConnection));
		logger.info(format("SCM-root                          : {}", scmRoot));
		logger.info(FUNCTION_LINE_BREAK);

		cleanupGitRemoteTags();
	}

	/**
	 * Cleanup lost GIT-tags before making a release on BUILD-server (for example HUDSON)
	 */
	private void cleanupGitRemoteTags()
	{
		logger.info("Determine local and remote SCM-tags for SCM-repo");
		logger.info(MOJO_LINE_BREAK);
		getRepositoryProvider().pull();
		List<Ref> refs = getRepositoryProvider().getLocalTags();
		if (!refs.isEmpty())
		{
			boolean found = false;
			for (Ref ref : refs)
			{
				if (ref.getName().contains(preparedReleaseTag))
				{
					found = true;
					logger.info(format("Delete local SCM-tag                 : {}", ref.getName().substring(10)));
					getRepositoryProvider().deleteTag(ref.getName());
					logger.info(format("Delete remote SCM-tag                : {}", ref.getName().substring(10)));
					getRepositoryProvider().pushTag();
				}
			}
			if (!found)
			{
				logger.info("No local or remote prepared SCM-tags found");
			}
		}
		else
		{
			logger.info("No local or remote prepared SCM-tags found");
		}

		getRepositoryProvider().closeRepository();
		logger.info(MOJO_LINE_BREAK);
	}
}
