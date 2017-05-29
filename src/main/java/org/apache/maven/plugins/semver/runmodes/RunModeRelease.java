package org.apache.maven.plugins.semver.runmodes;

import org.apache.maven.Maven;
import org.apache.maven.plugins.semver.SemverMavenPlugin;
import org.apache.maven.plugins.semver.configuration.SemverConfiguration;
import org.apache.maven.plugins.semver.factories.FileWriterFactory;
import org.apache.maven.plugins.semver.providers.PomProvider;
import org.apache.maven.plugins.semver.providers.VersionProvider;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import java.util.Map;

/**
 * @author sido
 */
@Component(role = RunModeRelease.class)
public class RunModeRelease implements RunMode {

    @Requirement
    private PomProvider pomProvider;
    @Requirement
    private VersionProvider versionProvider;
    @Requirement
    private MavenProject project;

    @Override
    public void execute(SemverConfiguration configuration, Map<SemverMavenPlugin.RAW_VERSION, String> rawVersions) {
        Map<VersionProvider.FINAL_VERSION, String> finalVersions = versionProvider.determineReleaseVersions(rawVersions);
        FileWriterFactory.createReleaseProperties(project, finalVersions);
    }

}
