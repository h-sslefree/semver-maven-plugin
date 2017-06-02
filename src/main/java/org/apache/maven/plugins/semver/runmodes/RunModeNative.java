package org.apache.maven.plugins.semver.runmodes;

import org.apache.maven.plugins.semver.SemverMavenPlugin;
import org.apache.maven.plugins.semver.configuration.SemverConfiguration;
import org.apache.maven.plugins.semver.factories.FileWriterFactory;
import org.apache.maven.plugins.semver.providers.PomProvider;
import org.apache.maven.plugins.semver.providers.VersionProvider;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

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
    private PomProvider pomProvider;
    @Requirement
    private VersionProvider versionProvider;

    @Override
    public void execute(SemverConfiguration configuration, Map<VersionProvider.RAW_VERSION, String> rawVersions) {
        FileWriterFactory.backupSemverPom();
        Map<VersionProvider.FINAL_VERSION, String> finalVersions = versionProvider.determineReleaseVersions(rawVersions);
        pomProvider.createReleasePom(finalVersions);
        pomProvider.createNextDevelopmentPom(finalVersions.get(VersionProvider.FINAL_VERSION.DEVELOPMENT));
        FileWriterFactory.removeBackupSemverPom();
    }

}
