package org.apache.maven.plugins.semver;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.semver.configuration.SemverConfiguration;
import org.apache.maven.plugins.semver.exceptions.SemverException;
import org.apache.maven.plugins.semver.factories.BranchFactory;
import org.apache.maven.plugins.semver.factories.FileWriterFactory;
import org.apache.maven.plugins.semver.factories.VersionFactory;
import org.apache.maven.project.MavenProject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * <p>Abstract class to use as template for each goal in the plugin.</p>
 * <p>
 * <p>Possible usages are:</p>
 * <ul>Possible runModes are:
 * <li>When {@link RUNMODE} = RELEASE then determine version from POM-version</li>
 * <li>When {@link RUNMODE} = RELEASE_RPM then determine version from POM-version</li>
 * <li>When {@link RUNMODE} = RELEASE_BRANCH then determine version from GIT-branch</li>
 * <li>When {@link RUNMODE} = RELEASE_BRANCH_HOSEE then determine version from POM-version (without maven-release-plugin)</li>
 * <li>When {@link RUNMODE} = NATIVE then determine version from POM-version (without maven-release-plugin)</li>
 * <li>When {@link RUNMODE} = NATIVE_BRANCH then determine version from POM-version (without maven-release-plugin)</li>
 * <li>When {@link RUNMODE} = RUNMODE_NOT_SPECIFIED does nothing</li>
 * </ul>
 * <ul>Possible value for the branchConversionUrl is
 * <li>branchConversionUrl = http://localhost/determineBranchVersion</li>
 * </ul>
 * <ul>Turn metaData on or off
 * <li>metaData = true/false</li>
 * </ul>
 *
 * @author sido
 */
public abstract class SemverMavenPlugin extends AbstractMojo {

    protected final Log LOG = getLog();

    public static final String MOJO_LINE_BREAK = "------------------------------------------------------------------------";
    private static final String FUNCTION_LINE_BREAK = "************************************************************************";

    protected Git currentGitRepo;

    protected CredentialsProvider credProvider;

    @Parameter(property = "project", defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;
    @Parameter(property = "username", defaultValue = "")
    protected String scmUsername = "";
    @Parameter(property = "password", defaultValue = "")
    protected String scmPassword = "";
    @Parameter(property = "tag")
    protected String preparedReleaseTag;
    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    protected MavenSession session;
    @Parameter(property = "runMode", required = true, defaultValue = "RELEASE")
    private RUNMODE runMode;
    @Parameter(property = "branchVersion")
    private String branchVersion;
    @Parameter(property = "metaData")
    private String metaData;
    @Parameter(property = "branchConversionUrl")
    private String branchConversionUrl;

    private SemverConfiguration configuration;

    private boolean isRemoteEnabled = false;


    /**
     * <p>Override runMode through configuration properties</p>
     *
     * @param runMode get runMode from plugin configuration
     */
    public void setRunMode(RUNMODE runMode) {
        this.runMode = runMode;
    }

    /**
     * <p>Override branchVersion through configuration properties</p>
     *
     * @param branchVersion get branchVersion from plugin configuration
     */
    public void setBranchVersion(String branchVersion) {
        this.branchVersion = branchVersion;
    }


    /**
     * <p>Override branchConversionUrl through configuration properties</p>
     *
     * @param branchConversionUrl get branchConversionUrl from plugin configuration
     */
    public void setBranchConversionUrl(String branchConversionUrl) {
        this.branchConversionUrl = branchConversionUrl;
    }

    /**
     * <p>Create a postfix for the versionTag</p>
     *
     * @param metaData for example "-solr"
     */
    public void setMetaData(String metaData) {
        this.metaData = metaData;
    }

    /**
     * <p>Initialize GIT-repo for determining branch and tag information.</p>
     *
     * @throws SemverException exception for not initializing local and remote repository
     */
    protected void initializeRepository() throws SemverException {

        LOG.info(FUNCTION_LINE_BREAK);
        if (currentGitRepo == null && credProvider == null) {
            LOG.info("Initializing GIT-repository");
            FileRepositoryBuilder repoBuilder = new FileRepositoryBuilder();
            repoBuilder.addCeilingDirectory(project.getBasedir());
            repoBuilder.findGitDir(project.getBasedir());
            Repository repo = null;
            try {
                repo = repoBuilder.build();
                currentGitRepo = new Git(repo);
                LOG.info(" - GIT-repository is initialized");
            } catch (Exception err) {
                LOG.error(" - This is not a valid GIT-repository.");
                LOG.error(" - Please run this goal in a valid GIT-repository");
                LOG.error(" - Could not initialize repostory", err);
                throw new SemverException("This is not a valid GIT-repository", "Please run this goal in a valid GIT-repository");
            }
            if (!(getConfiguration().getScmPassword().isEmpty() || getConfiguration().getScmUsername().isEmpty())) {
                isRemoteEnabled = true;
                credProvider = new UsernamePasswordCredentialsProvider(getConfiguration().getScmUsername(), getConfiguration().getScmPassword());
                LOG.info(" - GIT-credential provider is initialized");
            } else {
                LOG.warn(" - There is no connection to the remote GIT-repository");
                LOG.debug(" - To make a connection to the remote please enter '-Dusername=#username# -Dpassword=#password#' on commandline to initialize the remote repository correctly");
            }
        } else {
            LOG.debug("GIT repository and the credentialsprovider are already initialized");
        }
        LOG.info("GIT-repository initializing finished");
        LOG.info(FUNCTION_LINE_BREAK);
    }



    /**
     *
     *
     * <p>Executes the configured runMode for each goal.</p>
     *
     *
     * @param rawVersions rawVersions are the versions determined by the goal
     */
    protected void executeRunMode(Map<RAW_VERSION, String> rawVersions) {
        if (getConfiguration().getRunMode() == RUNMODE.RELEASE) {
            Map<VersionFactory.FINAL_VERSION, String> finalVersions = VersionFactory.determineReleaseVersions(LOG, getConfiguration(), project, rawVersions);
            FileWriterFactory.createReleaseProperties(LOG, project, finalVersions);
        } else if (getConfiguration().getRunMode() == RUNMODE.NATIVE) {
            FileWriterFactory.backupSemverPom(LOG, project);
            Map<VersionFactory.FINAL_VERSION, String> finalVersions = VersionFactory.determineReleaseNativeVersions(getLog(), getConfiguration(), project, rawVersions);
        } else if(getConfiguration().getRunMode() == RUNMODE.RELEASE_BRANCH || getConfiguration().getRunMode() == RUNMODE.RELEASE_BRANCH_HOSEE) {
            Map<VersionFactory.FINAL_VERSION, String> finalVersions = VersionFactory.determineReleaseBranchVersions(getLog(), getConfiguration(), project, rawVersions);
            FileWriterFactory.createReleaseProperties(LOG, project, finalVersions);
        }
    }

    /**
     * <p>When a <i>release:rollback</i> is performed local git-tags have to be cleaned to perform the next release.</p>
     *
     * @param scmVersion scmVersion
     * @throws IOException
     * @throws GitAPIException
     */
    protected void cleanupGitLocalAndRemoteTags(String scmVersion) throws SemverException, IOException, GitAPIException {
        LOG.info("Check for lost-tags");
        LOG.info(MOJO_LINE_BREAK);
        try {
            initializeRepository();
        } catch (Exception e) {
            LOG.error("Could not initialize GIT-repository", e);
        }
        if (isRemoteEnabled) {
            currentGitRepo.pull().setCredentialsProvider(credProvider).call();
            List<Ref> refs = currentGitRepo.tagList().call();
            LOG.debug("Remote tags: " + refs.toString());
            if (refs.isEmpty()) {
                boolean found = false;
                for (Ref ref : refs) {
                    if (ref.getName().contains(scmVersion)) {
                        found = true;
                        LOG.info("Delete lost local-tag                 : " + ref.getName().substring(10));
                        currentGitRepo.tagDelete().setTags(ref.getName()).call();
                        RefSpec refSpec = new RefSpec().setSource(null).setDestination(ref.getName());
                        LOG.info("Delete lost remote-tag                : " + ref.getName().substring(10));
                        currentGitRepo.push().setRemote("origin").setRefSpecs(refSpec).setCredentialsProvider(credProvider).call();
                    }
                }
                if (!found) {
                    LOG.info("No local or remote lost tags found");
                }
            } else {
                LOG.info("No local or remote lost tags found");
            }
        } else {
            LOG.warn("Remote is not initialized. Could not delete remote tags");
        }
        currentGitRepo.close();
        LOG.info(MOJO_LINE_BREAK);
    }

    /**
     * <p>Get merged configuration</p>
     *
     * @return SemverConfiguration
     */
    public SemverConfiguration getConfiguration() {
        try {
            initializeRepository();
        } catch (Exception err) {
            LOG.error("Could not initialize GIT-repository", err);
        }

        if (configuration == null) {
            configuration = new SemverConfiguration(session);
            configuration.setScmUsername(scmUsername);
            configuration.setScmPassword(scmPassword);
            configuration.setRunMode(runMode);
            configuration.setBranchConversionUrl(branchConversionUrl);
            configuration.setBranchVersion(BranchFactory.determineBranchVersionFromGitBranch(LOG, currentGitRepo, getConfiguration().getBranchConversionUrl(), branchVersion));
            configuration.setMetaData(metaData);
        }
        return configuration;
    }

    /**
     * <p>Version-type is mentoined here.</p>
     *
     * @author sido
     */
    public enum RAW_VERSION {
        DEVELOPMENT,
        RELEASE,
        MAJOR,
        MINOR,
        PATCH;
    }

    /**
     * <ul>
     * <li>release: maak gebruik van normale semantic-versioning en release-plugin</li>
     * <li>release-rpm</li>
     * <li>native</li>
     * <li>native-rpm</li>
     * </ul>
     *
     * @author sido
     */
    public enum RUNMODE {
        RELEASE,
        RELEASE_BRANCH,
        RELEASE_BRANCH_HOSEE,
        NATIVE,
        NATIVE_BRANCH,
        RUNMODE_NOT_SPECIFIED;

        public static RUNMODE convertToEnum(String runMode) {
            RUNMODE value = RUNMODE_NOT_SPECIFIED;
            if (runMode != null) {
                if ("RELEASE".equals(runMode)) {
                    value = RELEASE;
                } else if ("RELEASE_BRANCH".equals(runMode)) {
                    value = RELEASE_BRANCH;
                } else if ("RELEASE_BRANCH_HOSEE".equals(runMode)) {
                    value = RELEASE_BRANCH_HOSEE;
                } else if ("NATIVE".equals(runMode)) {
                    value = NATIVE;
                } else if ("NATIVE_BRANCH".equals(runMode)) {
                    value = NATIVE_BRANCH;
                }
            }
            return value;
        }
    }

}
