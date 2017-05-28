package org.apache.maven.plugins.semver.providers;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.semver.SemverMavenPlugin;
import org.apache.maven.plugins.semver.configuration.SemverConfiguration;
import org.apache.maven.plugins.semver.exceptions.SemverException;
import org.eclipse.jgit.lib.Ref;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * <h1>VersionProvider</h1>
 * <p>The versionprovider is used to determine the different symantic-versioning versions to create git tags.</p>
 *
 * @author sido
 */
public class VersionProvider {

    private Log LOG;

    private SemverConfiguration configuration;

    private RepositoryProvider repositoryProvider;

    public enum FINAL_VERSION {
        DEVELOPMENT,
        BUILD_METADATA,
        SCM,
        RELEASE
    }

    /**
     *
     * <p>In the constructor the logging and the configuration is given.</p>
     * <p>These are elements which continue to return in the different methods.</p>
     *
     * @param LOG                {@link org.apache.maven.plugin.logging.Log}
     * @param configuration      {@link org.apache.maven.plugins.semver.configuration.SemverConfiguration}
     *
     */
    public VersionProvider(Log LOG, RepositoryProvider repositoryProvider, SemverConfiguration configuration) {
        this.LOG = LOG;
        this.repositoryProvider = repositoryProvider;
        this.configuration = configuration;
    }


    /**
     *
     * <p>Determine release versions from {@link SemverMavenPlugin.RAW_VERSION}.</p>
     *
     * @param rawVersions raw version map with development version patch, minor and major the {@link org.apache.maven.plugins.semver.SemverMavenPlugin.RAW_VERSION} enumeration is used to define the map
     * @return finalVersions
     */
    public Map<FINAL_VERSION, String> determineReleaseVersions(Map<SemverMavenPlugin.RAW_VERSION, String> rawVersions) {
        Map<FINAL_VERSION, String> finalVersions = new HashMap<>();
        finalVersions.put(FINAL_VERSION.DEVELOPMENT, rawVersions.get(SemverMavenPlugin.RAW_VERSION.DEVELOPMENT));
        finalVersions.put(FINAL_VERSION.RELEASE, rawVersions.get(SemverMavenPlugin.RAW_VERSION.RELEASE));
        finalVersions.put(FINAL_VERSION.SCM, rawVersions.get(SemverMavenPlugin.RAW_VERSION.RELEASE));
        return finalVersions;
    }

    /**
     * <p>Determine release-versions from {@link SemverMavenPlugin.RAW_VERSION}.</p>
     * <p>This version contains also the buildmeta-data and branch information.</p>
     *
     * @param rawVersions raw version map with development version patch, minor and major the {@link org.apache.maven.plugins.semver.SemverMavenPlugin.RAW_VERSION} enumeration is used to define the map
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
            LOG.info("New DEVELOPMENT-version                 : " + rawVersions.get(SemverMavenPlugin.RAW_VERSION.DEVELOPMENT));
            LOG.info("New BRANCH GIT build metadata           : " + buildMetaData);
            LOG.info("New BRANCH GIT-version                  : " + scmVersion);
            LOG.info("New BRANCH RELEASE-version              : " + releaseVersion);
            LOG.info(SemverMavenPlugin.MOJO_LINE_BREAK);
        }
        Map<FINAL_VERSION, String> finalVersions = new HashMap<>();
        finalVersions.put(FINAL_VERSION.DEVELOPMENT, rawVersions.get(SemverMavenPlugin.RAW_VERSION.DEVELOPMENT));
        finalVersions.put(FINAL_VERSION.BUILD_METADATA, buildMetaData);
        finalVersions.put(FINAL_VERSION.SCM, scmVersion.toString());
        finalVersions.put(FINAL_VERSION.RELEASE, releaseVersion.toString());

        return finalVersions;
    }

    /**
     *
     * <p>Determine general release-tag.</p>
     * <p>Examples:</p>
     * <ul><b>NORMAL-release</b>
     *   <li>1.1.1</li>
     * </ul>
     * <ul><b>BRANCH-release</b>
     *   <li>1.1.1-001001001</li>
     * </ul>
     *
     * @param patch patch is the number to define a bugfix in symantic-versioning
     * @param minor minor is the number to define a feature in symantic-versioning
     * @param major major is the number to define a breaking change in symantic-versioning
     *
     * @return release tag
     */
    public String determineReleaseTag(int patch, int minor, int major) {
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
     * @param patch patch is the number to define a bugfix in symantic-versioning
     * @param minor minor is the number to define a feature in symantic-versioning
     * @param major major is the number to define a breaking change in symantic-versioning
     *
     * @return build metadata
     */
    public String determineBuildMetaData(int patch, int minor, int major) {
        StringBuilder buildMetaData = new StringBuilder();
        if (configuration != null && configuration.getRunMode() == SemverMavenPlugin.RUNMODE.RELEASE_BRANCH_HOSEE) {
            String buildMetaDataBranch = major + "." + minor + "." + patch;
            buildMetaData.append("+");
            buildMetaData.append(buildMetaDataBranch);
        }
        if (configuration != null && !configuration.getMetaData().isEmpty()) {
            buildMetaData.append("+");
            buildMetaData.append(configuration.getMetaData());
        }
        return buildMetaData.toString();
    }

    /**
     * <p>Determine if the version in the pom.xml is corrupt.</p>
     * <p>If this is the case then exit the semver-plugin.</p>
     *
     *
     * @param pomVersion get pom version from project
     * @throws SemverException native semver exception
     * @return is version corrupt?
     */
    public boolean isVersionCorrupt(String pomVersion) throws SemverException {
        boolean isVersionCorrupt = false;
        LOG.info("Check on pom-version");
        LOG.info(SemverMavenPlugin.MOJO_LINE_BREAK);
        if(pomVersion == null || pomVersion.isEmpty()) {
            isVersionCorrupt = true;
            LOG.error("");
            LOG.error("The version in the pom.xml is NULL of empty please correct the pom.xml");
            LOG.error("");
        } else if(!pomVersion.contains("-SNAPSHOT")) {
            isVersionCorrupt = true;
            LOG.error("");
            LOG.error("The version in the pom.xml does not contain -SNAPSHOT. Please repair the version-string");
            LOG.error("");
        } else {
            LOG.info("Pom-version is correct             : " + pomVersion);
        }
        LOG.info(SemverMavenPlugin.FUNCTION_LINE_BREAK);
        return isVersionCorrupt;
    }

    /**
     *
     * <p>Determine if remote version is corrupt.</p>
     *
     * @param rawLocalVersion the pomVersion which has to be evaluated
     * @return is corrupt or not
     */
    public boolean isRemoteVersionCorrupt(String rawLocalVersion) {
        boolean isRemoteVersionCorrupt  = false;
        DefaultArtifactVersion localVersion;
        LOG.info("Check remote-tag with local-tag    : [ " + rawLocalVersion + " ]");
        if(rawLocalVersion.contains("-SNAPSHOT")) {
            localVersion = new DefaultArtifactVersion(rawLocalVersion.replaceFirst("-SNAPSHOT", ""));
        } else {
            localVersion = new DefaultArtifactVersion(rawLocalVersion);
        }
        Map<String, Ref> remoteTags = repositoryProvider.getRemoteTags();
        for(Map.Entry<String, Ref> remoteTag : remoteTags.entrySet()) {
            DefaultArtifactVersion remoteVersion = new DefaultArtifactVersion(remoteTag.getKey());
            LOG.debug(" * Compare remote-tag [ " + remoteVersion + " ] with local-tag [ " + localVersion + " ]");
            if(remoteVersion.compareTo(localVersion) > 0) {
                LOG.error("Local version is corrupt          : [ local: " + localVersion + " ] [ remote: " + remoteVersion + " ] ");
                isRemoteVersionCorrupt = true;
            }
        }
        return isRemoteVersionCorrupt;
    }





}
