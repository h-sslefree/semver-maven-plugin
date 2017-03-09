package org.apache.maven.plugins.semver.factories;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.semver.SemverMavenPlugin;
import org.apache.maven.plugins.semver.configuration.SemverConfiguration;
import org.apache.maven.project.MavenProject;

import java.io.*;

/**
 *
 * <h1>VersionFactory</h1>
 *
 * <p>The versionfactory is used to determine the different symantic-versioning versions to create git tags.</p>
 *
 * @author sido
 */
public class VersionFactory {

    private VersionFactory() {
    }

    /**
     * <p>Create a specific version for production-version of the product.</p>
     *
     * @param LOG                @see {@link org.apache.maven.plugin.logging.Log}
     * @param configuration      @see {@link org.apache.maven.plugins.semver.configuration.SemverConfiguration}
     * @param project            @see {@link org.apache.maven.project.MavenProject}
     * @param developmentVersion next development version for pom.xml
     * @param major              semantic major-version to determine release-version and scm-tag version
     * @param minor              semantic minor-version to determine release-version and scm-tag version
     * @param patch              semantic patch-version to determine release-version and scm-tag version
     */
    public static void createReleaseBranch(Log LOG, SemverConfiguration configuration, MavenProject project, String developmentVersion, int major, int minor, int patch) {

        if(LOG != null) {
            LOG.info("NEW versions on BRANCH base");
        }

        String releaseTag = major + "." + minor + "." + patch;
        if (configuration.getRunMode() == SemverMavenPlugin.RUNMODE.RELEASE_BRANCH_HOSEE) {
            releaseTag = String.format("%03d%03d%03d", major, minor, patch);
        }

        String releaseVersion = configuration.getBranchVersion() + "-" + releaseTag;
        String buildMetaData = major + "." + minor + "." + patch;
        String scmVersion = releaseVersion;
        if (configuration.getRunMode() == SemverMavenPlugin.RUNMODE.RELEASE_BRANCH_HOSEE) {
            scmVersion = releaseVersion + "+" + buildMetaData;
        }
        if (!configuration.getMetaData().isEmpty()) {
            scmVersion = scmVersion + "+" + configuration.getMetaData();
        }
        if(LOG != null) {
            LOG.info("New DEVELOPMENT-version                  : " + developmentVersion);
            LOG.info("New BRANCH GIT build metadata            : " + buildMetaData);
            LOG.info("New BRANCH GIT-version                   : " + scmVersion);
            LOG.info("New BRANCH RELEASE-version               : " + releaseVersion);
            LOG.info(SemverMavenPlugin.MOJO_LINE_BREAK);
        }

        FileWriterFactory.createReleaseProperties(LOG, project, developmentVersion, releaseVersion, scmVersion);
    }

    /**
     * <p>Use the semver-maven-plugin only. Wthout the release-maven-plugin.</p>
     *
     * @param LOG                @see {@link org.apache.maven.plugin.logging.Log}
     * @param configuration      @see {@link org.apache.maven.plugins.semver.configuration.SemverConfiguration}
     * @param project            @see {@link org.apache.maven.project.MavenProject}
     * @param developmentVersion next development version for pom.xml
     * @param releaseVersion     current release version and tag
     */
    public static void createReleaseNative(Log LOG, SemverConfiguration configuration, MavenProject project, String developmentVersion, String releaseVersion) {
        FileWriterFactory.backupSemverPom(LOG, project);

    }


}
