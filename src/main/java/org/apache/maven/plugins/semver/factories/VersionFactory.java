package org.apache.maven.plugins.semver.factories;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.semver.SemverMavenPlugin;
import org.apache.maven.plugins.semver.configuration.SemverConfiguration;
import org.apache.maven.project.MavenProject;
import org.eclipse.jgit.api.Git;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * <h1>VersionFactory</h1>
 *
 * <p>The versionfactory is used to determine the different symantic-versioning versions to create git tags.</p>
 *
 * @author sido
 */
public class VersionFactory {

    public enum FINAL_VERSION {
        DEVELOPMENT,
        BUILDMETADATA,
        SCM,
        RELEASE
    }

    private VersionFactory() {
    }


    /**
     *
     * @param LOG
     * @param configuration
     * @param project
     * @param rawVersions
     * @return
     */
    public static Map<FINAL_VERSION, String> determineReleaseVersions(Log LOG, SemverConfiguration configuration, MavenProject project, Map<SemverMavenPlugin.RAW_VERSION, String> rawVersions) {
        Map<FINAL_VERSION, String> finalVersions = new HashMap<FINAL_VERSION, String>();
        finalVersions.put(FINAL_VERSION.DEVELOPMENT, rawVersions.get(SemverMavenPlugin.RAW_VERSION.DEVELOPMENT));
        finalVersions.put(FINAL_VERSION.RELEASE, rawVersions.get(SemverMavenPlugin.RAW_VERSION.RELEASE));
        return finalVersions;
    }

    /**
     * <p>Create a specific version for production-version of the product.</p>
     *
     * @param LOG                @see {@link org.apache.maven.plugin.logging.Log}
     * @param configuration      @see {@link org.apache.maven.plugins.semver.configuration.SemverConfiguration}
     * @param project            @see {@link org.apache.maven.project.MavenProject}
     * @param rawVersions        raw version map with development version patch, minor and major<br>
     *                           the @see {@link org.apache.maven.plugins.semver.SemverMavenPlugin.RAW_VERSION} enumeration is used to define the map
     */
    public static Map<FINAL_VERSION, String> determineReleaseBranchVersions(Log LOG, SemverConfiguration configuration, MavenProject project, Map<SemverMavenPlugin.RAW_VERSION, String> rawVersions) {

        if(LOG != null) {
            LOG.info("NEW rawVersions on BRANCH base");
        }

        int patch = Integer.parseInt(rawVersions.get(SemverMavenPlugin.RAW_VERSION.PATCH));
        int minor = Integer.parseInt(rawVersions.get(SemverMavenPlugin.RAW_VERSION.MINOR));
        int major = Integer.parseInt(rawVersions.get(SemverMavenPlugin.RAW_VERSION.MAJOR));

        String releaseTag = determineReleaseTag(configuration, patch, minor, major);
        String buildMetaData = determineBuildMetaData(configuration, patch, minor, major);

        StringBuilder releaseVersion = new StringBuilder();
        if(configuration != null && !configuration.getBranchVersion().isEmpty()) {
            releaseVersion.append(configuration.getBranchVersion());
            releaseVersion.append("-");
        }
        releaseVersion.append(releaseTag);

        StringBuilder scmVersion = new StringBuilder();
        scmVersion.append(releaseVersion);
        scmVersion.append(buildMetaData);

        if(LOG != null) {
            LOG.info("New DEVELOPMENT-version                  : " + rawVersions.get(SemverMavenPlugin.RAW_VERSION.DEVELOPMENT));
            LOG.info("New BRANCH GIT build metadata            : " + buildMetaData);
            LOG.info("New BRANCH GIT-version                   : " + scmVersion);
            LOG.info("New BRANCH RELEASE-version               : " + releaseVersion);
            LOG.info(SemverMavenPlugin.MOJO_LINE_BREAK);
        }
        Map<FINAL_VERSION, String> finalVersions = new HashMap<FINAL_VERSION, String>();
        finalVersions.put(FINAL_VERSION.DEVELOPMENT, rawVersions.get(SemverMavenPlugin.RAW_VERSION.DEVELOPMENT));
        finalVersions.put(FINAL_VERSION.BUILDMETADATA, buildMetaData);
        finalVersions.put(FINAL_VERSION.SCM, scmVersion.toString());
        finalVersions.put(FINAL_VERSION.RELEASE, releaseVersion.toString());

        return finalVersions;
    }


    /**
     * <p>Use the semver-maven-plugin only. Wthout the release-maven-plugin.</p>
     *
     * @param LOG                @see {@link org.apache.maven.plugin.logging.Log}
     * @param configuration      @see {@link org.apache.maven.plugins.semver.configuration.SemverConfiguration}
     * @param project            @see {@link org.apache.maven.project.MavenProject}
     * @param rawVersions        raw version map with development version patch, minor and major<br>
     *                           the @see {@link org.apache.maven.plugins.semver.SemverMavenPlugin.RAW_VERSION} enumeration is used to define the map
     */
    public static Map<FINAL_VERSION, String> determineReleaseNativeVersions(Log LOG, SemverConfiguration configuration, MavenProject project, Map<SemverMavenPlugin.RAW_VERSION, String> rawVersions) {

        return null;
    }

    /**
     *
     * <p></p>
     *
     * @param configuration @see {@link org.apache.maven.plugins.semver.configuration.SemverConfiguration}
     * @param patch         patch is the number to define a bugfix in symantic-versioning
     * @param minor         minor is the number to define a feature in symantic-versioning
     * @param major         major is the number to define a breaking change in symantic-versioning
     *
     * @return
     */
    private static String determineReleaseTag(SemverConfiguration configuration, int patch, int minor, int major) {
        StringBuilder releaseTag = new StringBuilder();
        releaseTag.append(major);
        releaseTag.append(".");
        releaseTag.append(minor);
        releaseTag.append(".");
        releaseTag.append(patch);
        if (configuration != null && configuration.getRunMode() == SemverMavenPlugin.RUNMODE.RELEASE_BRANCH_HOSEE) {
            releaseTag = new StringBuilder();
            releaseTag.append(String.format("%03d%03d%03d", major, minor, patch));
        }
        return releaseTag.toString();
    }

    /**
     *
     * <p>Determine wether or not buildMetaData had to be added to the scmversion for GIT</p>
     *
     * @param configuration @see {@link org.apache.maven.plugins.semver.configuration.SemverConfiguration}
     * @param patch         patch is the number to define a bugfix in symantic-versioning
     * @param minor         minor is the number to define a feature in symantic-versioning
     * @param major         major is the number to define a breaking change in symantic-versioning
     * @return
     */
    private static String determineBuildMetaData(SemverConfiguration configuration, int patch, int minor, int major) {
        StringBuilder buildMetaData = new StringBuilder();
        if (configuration != null && configuration.getRunMode() == SemverMavenPlugin.RUNMODE.RELEASE_BRANCH_HOSEE) {
            StringBuilder buildMetaDataBranch = new StringBuilder();
            buildMetaDataBranch.append(major);
            buildMetaDataBranch.append(".");
            buildMetaDataBranch.append(minor);
            buildMetaDataBranch.append(".");
            buildMetaDataBranch.append(patch);
            buildMetaData.append("+");
            buildMetaData.append(buildMetaDataBranch.toString());
        }
        if (configuration != null && !configuration.getMetaData().isEmpty()) {
            buildMetaData.append("+");
            buildMetaData.append(configuration.getMetaData());
        }
        return buildMetaData.toString();
    }





}
