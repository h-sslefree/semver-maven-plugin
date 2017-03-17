package org.apache.maven.plugins.semver;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.semver.configuration.SemverConfiguration;
import org.apache.maven.plugins.semver.exceptions.SemverException;
import org.apache.maven.plugins.semver.providers.BranchProvider;
import org.apache.maven.plugins.semver.factories.FileWriterFactory;
import org.apache.maven.plugins.semver.providers.PomProvider;
import org.apache.maven.plugins.semver.providers.RepositoryProvider;
import org.apache.maven.plugins.semver.providers.VersionProvider;
import org.apache.maven.project.MavenProject;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;

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

  public static final String MOJO_LINE_BREAK = "------------------------------------------------------------------------";
  public static final String FUNCTION_LINE_BREAK = "************************************************************************";

  protected final Log LOG = getLog();

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
  @Parameter(property = "runMode", required = true, defaultValue = "NATIVE")
  private RUNMODE runMode;
  @Parameter(property = "branchVersion")
  private String branchVersion;
  @Parameter(property = "metaData")
  private String metaData;
  @Parameter(property = "branchConversionUrl")
  private String branchConversionUrl;

  private SemverConfiguration configuration;
  private RepositoryProvider repositoryProvider;
  private VersionProvider versionProvider;
  private BranchProvider branchProvider;
  private PomProvider pomProvider;


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
   * <p>Executes the configured runMode for each goal.</p>
   *
   * @param rawVersions rawVersions are the versions determined by the goal
   */
  protected void executeRunMode(Map<RAW_VERSION, String> rawVersions) {
    if (getConfiguration().getRunMode() == RUNMODE.RELEASE) {
      Map<VersionProvider.FINAL_VERSION, String> finalVersions = versionProvider.determineReleaseVersions(rawVersions);
      FileWriterFactory.createReleaseProperties(LOG, project, finalVersions);
    } else if (getConfiguration().getRunMode() == RUNMODE.NATIVE) {
      FileWriterFactory.backupSemverPom(LOG);
      Map<VersionProvider.FINAL_VERSION, String> finalVersions = versionProvider.determineReleaseVersions(rawVersions);
      pomProvider.createReleasePom(finalVersions);
      pomProvider.createNextDevelopmentPom(finalVersions.get(VersionProvider.FINAL_VERSION.DEVELOPMENT));
      FileWriterFactory.removeBackupSemverPom(LOG);
    } else if (getConfiguration().getRunMode() == RUNMODE.RELEASE_BRANCH || getConfiguration().getRunMode() == RUNMODE.RELEASE_BRANCH_HOSEE) {
      Map<VersionProvider.FINAL_VERSION, String> finalVersions = versionProvider.determineReleaseBranchVersions(rawVersions);
      FileWriterFactory.createReleaseProperties(LOG, project, finalVersions);
    }
  }

  /**
   * <p>When a <i>release:rollback</i> is performed local git-tags have to be cleaned to perform the next release.</p>
   *
   * @param scmVersion scmVersion
   * @throws SemverException
   * @throws IOException
   * @throws GitAPIException
   */
  protected void cleanupGitLocalAndRemoteTags(String scmVersion) throws SemverException, IOException, GitAPIException {
    LOG.info("Check for lost-tags");
    LOG.info(MOJO_LINE_BREAK);
    repositoryProvider.pull();
    List<Ref> refs = repositoryProvider.getTags();
    LOG.debug("Remote tags                      : " + refs.toString());
    if (refs.isEmpty()) {
      boolean found = false;
      for (Ref ref : refs) {
        if (ref.getName().contains(scmVersion)) {
          found = true;
          LOG.info("Delete lost local-tag                 : " + ref.getName().substring(10));
          repositoryProvider.deleteTag(ref.getName());
          LOG.info("Delete lost remote-tag                : " + ref.getName().substring(10));
          repositoryProvider.pushTag(ref.getName());
        }
      }
      if (!found) {
        LOG.info("No lost-tags where found          : local or remote");
      }
    } else {
      LOG.info("No lost-tags where found          : local or remote");
    }
    repositoryProvider.closeRepository();
    LOG.info(FUNCTION_LINE_BREAK);
  }

  /**
   * <p>Get merged configuration</p>
   *
   * @return SemverConfiguration
   */
  public SemverConfiguration getConfiguration() {
    if (configuration == null) {
      configuration = new SemverConfiguration(session);
      configuration.setScmUsername(scmUsername);
      configuration.setScmPassword(scmPassword);
      configuration.setRunMode(runMode);
      configuration.setBranchConversionUrl(branchConversionUrl);
      if (branchProvider != null) {
        configuration.setBranchVersion(branchProvider.determineBranchVersionFromGitBranch(branchVersion));
      } else {
        configuration.setBranchVersion(branchVersion);
      }
      configuration.setMetaData(metaData);
    }
    return configuration;
  }

  /**
   * <p>To use the {@link RepositoryProvider} this method is needed to get access.</p>
   *
   * @return {@link RepositoryProvider}
   */
  public RepositoryProvider getRepositoryProvider() {
    return repositoryProvider;
  }

  /**
   * <p>To use the {@link VersionProvider} this method is needed to get access.</p>
   *
   * @return {@link VersionProvider}
   */
  public VersionProvider getVersionProvider() {
    return versionProvider;
  }

  /**
   *
   * <p>In each goal this method is called to intialize all providers.</p>
   *
   */
  public void initializeProviders() {
    repositoryProvider = new RepositoryProvider(LOG, project, getConfiguration());
    branchProvider = new BranchProvider(LOG, repositoryProvider, branchConversionUrl);
    versionProvider = new VersionProvider(LOG, getConfiguration());
    pomProvider = new PomProvider(LOG, repositoryProvider, project);
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
