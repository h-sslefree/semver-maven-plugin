package org.apache.maven.plugins.semver;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.semver.configuration.SemverConfiguration;
import org.apache.maven.plugins.semver.providers.BranchProvider;
import org.apache.maven.plugins.semver.providers.PomProvider;
import org.apache.maven.plugins.semver.providers.RepositoryProvider;
import org.apache.maven.plugins.semver.providers.VersionProvider;
import org.apache.maven.plugins.semver.runmodes.*;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * <p>Abstract class to use as template for each goal in the plugin.</p>
 * <p>
 * <p>Possible usages are:</p>
 * <ul>Possible runModes are:
 * <li>When {@link RunMode.RUNMODE} = RELEASE then determine version from POM-version</li>
 * <li>When {@link RunMode.RUNMODE} = RELEASE_BRANCH then determine version from GIT-branch</li>
 * <li>When {@link RunMode.RUNMODE} = RELEASE_BRANCH_RPM then determine version from POM-version (without maven-release-plugin)</li>
 * <li>When {@link RunMode.RUNMODE} = NATIVE then determine version from POM-version (without maven-release-plugin)</li>
 * <li>When {@link RunMode.RUNMODE} = NATIVE_BRANCH then determine version from POM-version (without maven-release-plugin)</li>
 * <li>When {@link RunMode.RUNMODE} = RUNMODE_NOT_SPECIFIED does nothing</li>
 * </ul>
 * <ul>Add a tag to the GIT-version
 * <li>tag = 1</li>
 * </ul>
 * <ul>Add the branchVersion to the GIT-tag
 * <li>branchVersion = featureX</li>
 * </ul>
 * <ul>Possible value for the branchConversionUrl is
 * <li>branchConversionUrl = http://localhost/determineBranchVersion</li>
 * </ul>
 * <ul>Add metaData to the GIT-version
 * <li>metaData = beta</li>
 * </ul>
 *
 * @author sido
 */
public abstract class SemverMavenPlugin extends AbstractMojo {

  public static final String MOJO_LINE_BREAK = "------------------------------------------------------------------------";
  public static final String FUNCTION_LINE_BREAK = "************************************************************************";

  protected final Logger LOG = LoggerFactory.getLogger(SemverMavenPlugin.class);

  @Parameter(property = "project", defaultValue = "${project}", readonly = true, required = true)
  protected MavenProject project;
  @Parameter(property = "session", defaultValue = "${session}", readonly = true, required = true)
  protected MavenSession session;
  @Parameter(property = "tag")
  protected String preparedReleaseTag;
  @Parameter(property = "runMode", required = true, defaultValue = "NATIVE")
  private RunMode.RUNMODE runMode;
  @Parameter(property = "username", defaultValue = "")
  private String scmUsername = "";
  @Parameter(property = "password", defaultValue = "")
  private String scmPassword = "";
  @Parameter(property = "branchVersion")
  private String branchVersion;
  @Parameter(property = "branchConversionUrl")
  private String branchConversionUrl;
  @Parameter(property = "metaData")
  private String metaData;
  @Parameter(property = "checkRemoteVersionTags", defaultValue = "false")
  private Boolean checkRemoteVersionTags;

  private SemverConfiguration configuration;

  @Component
  private VersionProvider versionProvider;
  @Component
  private PomProvider pomProvider;
  @Component
  private RepositoryProvider repositoryProvider;
  @Component
  private BranchProvider branchProvider;

  @Component(role = RunModeNative.class)
  private RunMode runModeNative;
  @Component(role = RunModeNativeBranch.class)
  private RunMode runModeNativeBranch;
  @Component(role = RunModeRelease.class)
  private RunMode runModeRelease;
  @Component(role = RunModeReleaseBranch.class)
  private RunMode runModeReleaseBranch;

  /**
   * <p>Override runMode through configuration properties</p>
   *
   * @param runMode get runMode from plugin configuration
   */
  public void setRunMode(RunMode.RUNMODE runMode) {
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
  protected void executeRunMode(Map<VersionProvider.RAW_VERSION, String> rawVersions) {
    if (configuration.getRunMode() == RunMode.RUNMODE.RELEASE) {
      runModeRelease.execute(getConfiguration(), rawVersions);
    } else if (configuration.getRunMode() == RunMode.RUNMODE.RELEASE_BRANCH || configuration.getRunMode() == RunMode.RUNMODE.RELEASE_BRANCH_RPM) {
      runModeReleaseBranch.execute(getConfiguration(), rawVersions);
    } else if (configuration.getRunMode() == RunMode.RUNMODE.NATIVE) {
      runModeNative.execute(getConfiguration(), rawVersions);
    } else if (configuration.getRunMode() == RunMode.RUNMODE.NATIVE_BRANCH || configuration.getRunMode() == RunMode.RUNMODE.NATIVE_BRANCH_RPM) {
      runModeNativeBranch.execute(getConfiguration(), rawVersions);
    }
  }

  protected VersionProvider getVersionProvider() {
    return this.versionProvider;
  }

  protected RepositoryProvider getRepositoryProvider() {
    return this.repositoryProvider;
  }

  public SemverConfiguration getConfiguration() {
    if (configuration == null) {
      configuration = new SemverConfiguration(session);
      configuration.setScmUsername(scmUsername);
      configuration.setScmPassword(scmPassword);
      configuration.setRunMode(runMode);
      configuration.setBranchConversionUrl(branchConversionUrl);
      if (runMode == RunMode.RUNMODE.NATIVE_BRANCH || runMode == RunMode.RUNMODE.NATIVE_BRANCH_RPM || runMode == RunMode.RUNMODE.RELEASE_BRANCH || runMode == RunMode.RUNMODE.RELEASE_BRANCH_RPM) {
        if (branchProvider != null) {
          configuration.setBranchVersion(branchProvider.determineBranchVersionFromGitBranch(branchVersion, branchConversionUrl));
        } else {
          configuration.setBranchVersion(branchVersion);
        }
      }
      configuration.setMetaData(metaData);
      configuration.setCheckRemoteVersionTags(checkRemoteVersionTags);
    }
    return configuration;
  }


}
