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
import org.apache.maven.plugins.semver.runmodes.RunMode.RUN_MODE;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class to use as template for each goal in the plugin.
 *
 * <ul>
 *   Possible runModes are:
 *   <li>When {@link RUN_MODE} = RELEASE then determine version from POM-version
 *   <li>When {@link RUN_MODE} = RELEASE_BRANCH then determine version from GIT-branch
 *   <li>When {@link RUN_MODE} = RELEASE_BRANCH_RPM then determine version from POM-version for an
 *       RPM-artifact(with maven-release-plugin)
 *   <li>When {@link RUN_MODE} = NATIVE then determine version from POM-version (without
 *       maven-release-plugin)
 *   <li>When {@link RUN_MODE} = NATIVE_BRANCH then determine version from POM-version (without
 *       maven-release-plugin)
 *   <li>When {@link RUN_MODE} = NATIVE_BRANCH_RPM then determine version from POM-version for an
 *       RPM-artifact (without maven-release-plugin)
 *   <li>When {@link RUN_MODE} = RUNMODE_NOT_SPECIFIED does nothing
 * </ul>
 *
 * <ul>
 *   Add a tag to the GIT-version
 *   <li>tag = 1.0.0
 * </ul>
 *
 * <ul>
 *   Add the branchVersion to the GIT-tag
 *   <li>branchVersion = featureX
 * </ul>
 *
 * <ul>
 *   Possible value for the branchConversionUrl is
 *   <li>branchConversionUrl = http://localhost/determineBranchVersion
 * </ul>
 *
 * <ul>
 *   Add metaData to the GIT-version
 *   <li>metaData = beta
 * </ul>
 *
 * @author sido
 */
public abstract class SemverMavenPlugin extends AbstractMojo {

  public static final String MOJO_LINE_BREAK =
      "------------------------------------------------------------------------";
  public static final String FUNCTION_LINE_BREAK =
      "************************************************************************";

  protected final Logger LOG = LoggerFactory.getLogger(SemverMavenPlugin.class);

  @Parameter(property = "project", defaultValue = "${project}", readonly = true, required = true)
  protected MavenProject project;

  @Parameter(property = "session", defaultValue = "${session}", readonly = true, required = true)
  protected MavenSession session;

  @Parameter(property = "tag")
  protected String preparedReleaseTag;

  @Parameter(property = "runMode", required = true, defaultValue = "NATIVE")
  private RUN_MODE runMode;

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

  @Parameter(property = "checkRemoteRepository", defaultValue = "false")
  private Boolean checkRemoteVersionTags;

  private SemverConfiguration configuration;

  @Component private VersionProvider versionProvider;
  @Component private PomProvider pomProvider;
  @Component private RepositoryProvider repositoryProvider;
  @Component private BranchProvider branchProvider;

  protected RunMode runModeImpl;

  @Component(role = RunModeNative.class)
  private RunMode runModeNative;

  @Component(role = RunModeNativeBranch.class)
  private RunMode runModeNativeBranch;

  @Component(role = RunModeRelease.class)
  private RunMode runModeRelease;

  @Component(role = RunModeReleaseBranch.class)
  private RunMode runModeReleaseBranch;

  /**
   * Override runMode through configuration properties
   *
   * @param runMode get runMode from plugin configuration
   */
  public void setRunMode(RUN_MODE runMode) {
    this.runMode = runMode;
  }

  /**
   * Override branchVersion through configuration properties
   *
   * @param branchVersion get branchVersion from plugin configuration
   */
  public void setBranchVersion(String branchVersion) {
    this.branchVersion = branchVersion;
  }

  /**
   * Override branchConversionUrl through configuration properties
   *
   * @param branchConversionUrl get branchConversionUrl from plugin configuration
   */
  public void setBranchConversionUrl(String branchConversionUrl) {
    this.branchConversionUrl = branchConversionUrl;
  }

  /**
   * Create a postfix for the versionTag
   *
   * @param metaData for example "-solr"
   */
  public void setMetaData(String metaData) {
    this.metaData = metaData;
  }

  protected VersionProvider getVersionProvider() {
    return this.versionProvider;
  }

  protected RepositoryProvider getRepositoryProvider() {
    return this.repositoryProvider;
  }

  /**
   * Determine configuration for semver-maven-plugin.
   *
   * @return {@link SemverConfiguration}
   */
  public SemverConfiguration getConfiguration() {
    if (configuration == null) {
      configuration = new SemverConfiguration(session);
      configuration.setScmUsername(scmUsername);
      configuration.setScmPassword(scmPassword);
      configuration.setRunMode(runMode);
      configuration.setBranchConversionUrl(branchConversionUrl);
      configuration.setMetaData(metaData);
      configuration.setCheckRemoteVersionTags(checkRemoteVersionTags);
      initializeRunMode(runMode);
    }
    return configuration;
  }

  /**
   *
   *
   * <h1>Initialize configured {@link RUN_MODE}.</h1>
   *
   * @param runMode configured RUN_MODE
   */
  private void initializeRunMode(RUN_MODE runMode) {
    switch (runMode) {
      case NATIVE:
        LOG.info("Initialize NATIVE-runmode implementation");
        this.runModeImpl = runModeNative;
        break;
      case NATIVE_BRANCH:
        LOG.info("Initialize NATIVE_BRANCH-runmode implementation");
        this.runModeImpl = runModeNativeBranch;
        initializeBranchVersion();
        break;
      case NATIVE_BRANCH_RPM:
        LOG.info("Initialize NATIVE_BRANCH_RPM-runmode implementation");
        this.runModeImpl = runModeNativeBranch;
        initializeBranchVersion();
        break;
      case RELEASE:
        LOG.info("Initialize RELEASE-runmode implementation");
        this.runModeImpl = runModeRelease;
        break;
      case RELEASE_BRANCH:
        LOG.info("Initialize RELEASE_BRANCH-runmode implementation");
        this.runModeImpl = runModeReleaseBranch;
        initializeBranchVersion();
        break;
      case RELEASE_BRANCH_RPM:
        LOG.info("Initialize RELEASE_BRANCH_RPM-runmode implementation");
        this.runModeImpl = runModeReleaseBranch;
        initializeBranchVersion();
        break;
      default:
        LOG.info("Initialize DEFAULT-runmode implementation");
        this.runModeImpl = runModeNative;
        break;
    }
  }

  /**
   *
   *
   * <h1>Initialize branchVersion</h1>
   *
   * <p>If a branchVersion or branchVersionConversion-url is given then a branchVersion can be
   * determined.
   */
  private void initializeBranchVersion() {
    if (branchProvider != null) {
      configuration.setBranchVersion(
          branchProvider.determineBranchVersionFromGitBranch(branchVersion, branchConversionUrl));
    } else {
      configuration.setBranchVersion(branchVersion);
    }
  }
}
