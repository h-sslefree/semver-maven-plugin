package org.apache.maven.plugins.semver.runmodes;

import org.apache.maven.plugins.semver.configuration.SemverConfiguration;
import org.apache.maven.plugins.semver.factories.FileWriterFactory;
import org.apache.maven.plugins.semver.goals.SemverGoals;
import org.apache.maven.plugins.semver.providers.PomProvider;
import org.apache.maven.plugins.semver.providers.RepositoryProvider;
import org.apache.maven.plugins.semver.providers.VersionProvider;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;

import java.util.Map;

/**
 * @author sido
 */
@Component(role = RunModeRelease.class)
public class RunModeRelease implements RunMode {

    @Requirement
    private Logger LOG;

    @Requirement
    private PomProvider pomProvider;
    @Requirement
    private VersionProvider versionProvider;
    @Requirement
    private RepositoryProvider repositoryProvider;
    @Requirement
    private MavenProject project;

    @Override
    public void execute(SemverGoals.SEMVER_GOAL semverGoal, SemverConfiguration configuration, String pomVersion) {
        try {
            Map<VersionProvider.RAW_VERSION, String> rawVersions = versionProvider.determineRawVersions(semverGoal, configuration.getRunMode(), configuration.getBranchVersion(), configuration.getMetaData(), pomVersion);
            RunMode.checkRemoteVersionTags(repositoryProvider, configuration, rawVersions.get(VersionProvider.RAW_VERSION.SCM));
            Map<VersionProvider.FINAL_VERSION, String> finalVersions = versionProvider.determineReleaseVersions(rawVersions);
            FileWriterFactory.createReleaseProperties(project, finalVersions);
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
    }

}
