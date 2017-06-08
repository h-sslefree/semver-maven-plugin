package org.apache.maven.plugins.semver.runmodes;

import org.apache.maven.plugins.semver.SemverMavenPlugin;
import org.apache.maven.plugins.semver.configuration.SemverConfiguration;
import org.apache.maven.plugins.semver.factories.FileWriterFactory;
import org.apache.maven.plugins.semver.goals.SemverGoal;
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
@Component(role = RunModeReleaseBranch.class)
public class RunModeReleaseBranch implements RunMode {

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
    public void execute(SemverGoal.SEMVER_GOAL goal, SemverConfiguration configuration, String pomVersion) {
        try {
            Map<VersionProvider.RAW_VERSION, String> rawVersions = versionProvider.determineRawVersions(goal, configuration.getRunMode(), configuration.getBranchVersion(), configuration.getMetaData(), pomVersion);
            if (!repositoryProvider.isRemoteVersionCorrupt(rawVersions.get(VersionProvider.RAW_VERSION.SCM))) {
                Map<VersionProvider.FINAL_VERSION, String> finalVersions = versionProvider.determineReleaseBranchVersions(rawVersions, configuration.getRunMode(), configuration.getMetaData(), configuration.getBranchVersion());
                FileWriterFactory.createReleaseProperties(project, finalVersions);
            } else {
                Runtime.getRuntime().exit(1);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
    }

}
