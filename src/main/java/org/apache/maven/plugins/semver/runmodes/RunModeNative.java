package org.apache.maven.plugins.semver.runmodes;

import org.apache.maven.plugins.semver.configuration.SemverConfiguration;
import org.apache.maven.plugins.semver.factories.FileWriterFactory;
import org.apache.maven.plugins.semver.goals.SemverGoal;
import org.apache.maven.plugins.semver.providers.PomProvider;
import org.apache.maven.plugins.semver.providers.RepositoryProvider;
import org.apache.maven.plugins.semver.providers.VersionProvider;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;

import java.util.Map;

/**
 *
 * <h1>RunModeNative</h1>
 *
 * <p></p>
 *
 * @author sido
 */
@Component(role = RunModeNative.class)
public class RunModeNative implements RunMode {

    @Requirement
    private Logger LOG;

    @Requirement
    private PomProvider pomProvider;
    @Requirement
    private VersionProvider versionProvider;
    @Requirement
    private RepositoryProvider repositoryProvider;

    @Override
    public void execute(SemverGoal.SEMVER_GOAL semverGoal, SemverConfiguration configuration, String pomVersion) {
        try {
            Map<VersionProvider.RAW_VERSION, String> rawVersions = versionProvider.determineRawVersions(semverGoal, configuration.getRunMode(), configuration.getBranchVersion(), configuration.getMetaData(), pomVersion);
            RunMode.checkRemoteRepository(repositoryProvider, versionProvider, configuration, rawVersions.get(VersionProvider.RAW_VERSION.SCM));
            FileWriterFactory.backupSemverPom();
            Map<VersionProvider.FINAL_VERSION, String> finalVersions = versionProvider.determineReleaseVersions(rawVersions);
            pomProvider.createReleasePom(finalVersions);
            pomProvider.createNextDevelopmentPom(finalVersions.get(VersionProvider.FINAL_VERSION.DEVELOPMENT));
            FileWriterFactory.removeBackupSemverPom();
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
    }

}
