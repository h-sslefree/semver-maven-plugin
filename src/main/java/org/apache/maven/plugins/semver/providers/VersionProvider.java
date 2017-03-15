package org.apache.maven.plugins.semver.providers;

import org.apache.maven.Maven;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.semver.SemverMavenPlugin;
import org.apache.maven.plugins.semver.configuration.SemverConfiguration;
import org.apache.maven.plugins.semver.exceptions.SemverException;
import org.apache.maven.project.MavenProject;

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
public class VersionProvider {

    private Log LOG;

    private SemverConfiguration configuration;

    private MavenProject project;

    public enum FINAL_VERSION {
        DEVELOPMENT,
        BUILD_METADATA,
        SCM,
        RELEASE
    }

    /**
     *
     * <p></p>
     *
     *
     *
     * @param LOG                @see {@link org.apache.maven.plugin.logging.Log}
     * @param configuration      @see {@link org.apache.maven.plugins.semver.configuration.SemverConfiguration}
     *
     */
    public VersionProvider(Log LOG, SemverConfiguration configuration) {
        this.LOG = LOG;
        this.configuration = configuration;
    }


    /**
     *
     * <p>Determine release versions from {@link SemverMavenPlugin.RAW_VERSION}.</p>
     *
     * @param rawVersions @see   raw version map with development version patch, minor and major<br>
     *                           the @see {@link org.apache.maven.plugins.semver.SemverMavenPlugin.RAW_VERSION} enumeration is used to define the map
     * @return finalVersions
     */
    public Map<FINAL_VERSION, String> determineReleaseVersions(Map<SemverMavenPlugin.RAW_VERSION, String> rawVersions) {
        Map<FINAL_VERSION, String> finalVersions = new HashMap<FINAL_VERSION, String>();
        finalVersions.put(FINAL_VERSION.DEVELOPMENT, rawVersions.get(SemverMavenPlugin.RAW_VERSION.DEVELOPMENT));
        finalVersions.put(FINAL_VERSION.RELEASE, rawVersions.get(SemverMavenPlugin.RAW_VERSION.RELEASE));
        finalVersions.put(FINAL_VERSION.SCM, rawVersions.get(SemverMavenPlugin.RAW_VERSION.RELEASE));
        return finalVersions;
    }

    /**
     * <p>Create a specific version for production-version of the product.</p>
     *
     * @param rawVersions        raw version map with development version patch, minor and major<br>
     *                           the @see {@link org.apache.maven.plugins.semver.SemverMavenPlugin.RAW_VERSION} enumeration is used to define the map
     *
     * @return finalVersions
     *
     */
    public Map<FINAL_VERSION, String> determineReleaseBranchVersions(Map<SemverMavenPlugin.RAW_VERSION, String> rawVersions) {

        if(LOG != null) {
            LOG.info("NEW rawVersions on BRANCH: [ " +configuration.getBranchVersion() + " ]");
        }

        int patch = Integer.parseInt(rawVersions.get(SemverMavenPlugin.RAW_VERSION.PATCH));
        int minor = Integer.parseInt(rawVersions.get(SemverMavenPlugin.RAW_VERSION.MINOR));
        int major = Integer.parseInt(rawVersions.get(SemverMavenPlugin.RAW_VERSION.MAJOR));

        String releaseTag = determineReleaseTag(patch, minor, major);
        String buildMetaData = determineBuildMetaData(patch, minor, major);

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
        finalVersions.put(FINAL_VERSION.BUILD_METADATA, buildMetaData);
        finalVersions.put(FINAL_VERSION.SCM, scmVersion.toString());
        finalVersions.put(FINAL_VERSION.RELEASE, releaseVersion.toString());

        return finalVersions;
    }


    /**
     * <p>Use the semver-maven-plugin only. Wthout the release-maven-plugin.</p>
     *
     * @param rawVersions        raw version map with development version patch, minor and major<br>
     *                           the @see {@link org.apache.maven.plugins.semver.SemverMavenPlugin.RAW_VERSION} enumeration is used to define the map
     */
    public Map<FINAL_VERSION, String> determineReleaseNativeVersions(Map<SemverMavenPlugin.RAW_VERSION, String> rawVersions) {

        return null;
    }

    /**
     *
     *
     *
     * @param pomVersion
     * @throws SemverException
     */
    public boolean isVersionCorrupt(String pomVersion) throws SemverException {
        boolean isVersionCorrupt = false;
        LOG.info("Check on pom-version");
        if(pomVersion == null || pomVersion.isEmpty()) {
            isVersionCorrupt = true;
            LOG.error("The version in the pom.xml is NULL of empty please correct the pom.xml");
        } else if(!pomVersion.contains("-SNAPSHOT")) {
            isVersionCorrupt = true;
            LOG.error("The version in the pom.xml does not contain -SNAPSHOT. Please repair the version-string.");
        }
        LOG.info(SemverMavenPlugin.MOJO_LINE_BREAK);
        return isVersionCorrupt;
    }

    /**
     *
     * <p></p>
     *
     * @param patch         patch is the number to define a bugfix in symantic-versioning
     * @param minor         minor is the number to define a feature in symantic-versioning
     * @param major         major is the number to define a breaking change in symantic-versioning
     *
     * @return
     */
    private String determineReleaseTag(int patch, int minor, int major) {
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
     * @param patch         patch is the number to define a bugfix in symantic-versioning
     * @param minor         minor is the number to define a feature in symantic-versioning
     * @param major         major is the number to define a breaking change in symantic-versioning
     * @return
     */
    private String determineBuildMetaData(int patch, int minor, int major) {
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
