package org.apache.maven.plugins.semver.configuration;

import javax.inject.Inject;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugins.semver.runmodes.RunMode.RUN_MODE;

/**
 * Semver Configuration is used to merge 3 types of configuration:
 *
 * <ul>
 *   <li>MAVEN-plugin configuration (determined in the xml-configuration)
 *   <li>CLI configuration (give in parameters, for example <i>-Dusername=#name#</i>)
 *   <li>Default configuration (determined by the plugin)
 * </ul>
 *
 * @author sido
 */
public class SemverConfiguration {

  private static final String BRANCH_CONVERSION_URL = "";

  private RUN_MODE runMode;
  private String branchVersion;
  private String scmUsername;
  private String scmPassword;
  private String branchConversionUrl;
  private String metaData;
  private Boolean checkRemoteVersionTags;

  private final MavenSession session;

  /**
   * Combining user properties with configuration properties in {@link SemverConfiguration}
   *
   * @param session MavenSession
   */
  @Inject
  public SemverConfiguration(MavenSession session) {
    this.session = session;
    mergeConfiguration();
  }

  /** Merges the 3 kinds of configuration */
  private void mergeConfiguration() {
    String userRunMode = "";
    String userBranchVersion = "";
    String userScmUsername = "";
    String userScmPassword = "";
    String userBranchConversionUrl = "";
    String userMetaData = "";
    Boolean userCheckRemoteVersionTags = false;
    if (session != null) {
      userRunMode = session.getUserProperties().getProperty("runMode");
      userBranchVersion = session.getUserProperties().getProperty("branchVersion");
      userScmUsername = session.getUserProperties().getProperty("username");
      userScmPassword = session.getUserProperties().getProperty("password");
      userBranchConversionUrl = session.getUserProperties().getProperty("branchConversionUrl");
      userMetaData = session.getUserProperties().getProperty("userMetaData");
      userCheckRemoteVersionTags =
          Boolean.valueOf(session.getUserProperties().getProperty("checkRemoteRepository"));
    }

    if (userRunMode != null && !userRunMode.isEmpty()) {
      runMode = RUN_MODE.convertToEnum(userRunMode);
    }
    if (runMode == RUN_MODE.RELEASE_BRANCH
        || runMode == RUN_MODE.RELEASE_BRANCH_RPM
        || runMode == RUN_MODE.NATIVE_BRANCH
        || runMode == RUN_MODE.NATIVE_BRANCH_RPM) {
      if (userBranchVersion != null && !userBranchVersion.isEmpty()) {
        branchVersion = userBranchVersion;
      }
      if (branchVersion == null) {
        branchVersion = "";
      }
    }

    if (scmUsername == null || scmUsername.isEmpty()) {
      scmUsername = userScmUsername;
      if (scmUsername == null || scmUsername.isEmpty()) {
        scmUsername = "";
        // TODO:SH Get username from settings.xml via plugin config
      }
    }

    if (scmPassword == null || scmPassword.isEmpty()) {
      scmPassword = userScmPassword;
      if (scmPassword == null || scmPassword.isEmpty()) {
        scmPassword = "";
        // TODO:SH Get password from settings.xml via plugin config
      }
    }

    if (branchConversionUrl == null || branchConversionUrl.isEmpty()) {
      if (userBranchConversionUrl != null && !userBranchConversionUrl.isEmpty()) {
        branchConversionUrl = userBranchConversionUrl;
      } else {
        branchConversionUrl = BRANCH_CONVERSION_URL;
      }
    }

    if (metaData == null || metaData.isEmpty()) {
      if (userMetaData != null && !userMetaData.isEmpty()) {
        metaData = userMetaData;
      } else {
        metaData = "";
      }
    }

    if (checkRemoteVersionTags == null || !checkRemoteVersionTags) {
      if (!userCheckRemoteVersionTags) {
        checkRemoteVersionTags = userCheckRemoteVersionTags;
      } else {
        checkRemoteVersionTags = false;
      }
    }
  }

  /**
   * Get RUNMODE
   *
   * <ul>
   *   Possible runModes are:
   *   <li>When {@link RUN_MODE} = RELEASE then determine version from POM-version
   *   <li>When {@link RUN_MODE} = RELEASE_BRANCH then determine version from GIT-branch
   *   <li>When {@link RUN_MODE} = RELEASE_BRANCH_RPM then determine version from POM-version
   *       (without maven-release-plugin)
   *   <li>When {@link RUN_MODE} = NATIVE then determine version from POM-version (without
   *       maven-release-plugin)
   *   <li>When {@link RUN_MODE} = NATIVE_BRANCH then determine version from POM-version (without
   *       maven-release-plugin)
   *   <li>When {@link RUN_MODE} = NATIVE_BRANCH_RPM then determine version from POM-version
   *       (without maven-release-plugin)
   *   <li>When {@link RUN_MODE} = RUNMODE_NOT_SPECIFIED does nothing
   * </ul>
   *
   * @return RUNMODE
   */
  public RUN_MODE getRunMode() {
    return runMode;
  }

  /**
   * Set RUNMODE and merge with configuration.
   *
   * @param runMode kind of runMode the plugin is configured with
   */
  public void setRunMode(RUN_MODE runMode) {
    this.runMode = runMode;
    mergeConfiguration();
  }

  /**
   * Returns the branchVersion of the current branch in which the parent project is in
   *
   * @return branchVersion
   */
  public String getBranchVersion() {
    return branchVersion;
  }

  public void setBranchVersion(String branchVersion) {
    this.branchVersion = branchVersion;
    mergeConfiguration();
  }

  /**
   * To commit tags in SCM you have to have the username of the repostory
   *
   * @return scmUsername
   */
  public String getScmUsername() {
    return this.scmUsername;
  }

  public void setScmUsername(String scmUsername) {
    this.scmUsername = scmUsername;
    mergeConfiguration();
  }

  /**
   * To commit tags in SCM you have to have the password of the repostory
   *
   * @return scmPassword
   */
  public String getScmPassword() {
    return this.scmPassword;
  }

  public void setScmPassword(String scmPassword) {
    this.scmPassword = scmPassword;
    mergeConfiguration();
  }

  /**
   * THe branchConversionUrl is used to determine which version contains is the master-branch. This
   * is necessary to determine the next version of the <b>master</b>-branch.
   *
   * @return branchConversionUrl
   */
  public String getBranchConversionUrl() {
    return this.branchConversionUrl;
  }

  public void setBranchConversionUrl(String branchConversionUrl) {
    this.branchConversionUrl = branchConversionUrl;
    mergeConfiguration();
  }

  /**
   * Version-metaData is used to describe the version that is tagged.
   *
   * <p>For example:
   *
   * <p>RPM-version: 6.4.0-002001003+2.1.5
   *
   * <p>The +2.1.3 is the metaData from the version
   *
   * <p>It is parsed from 002001003.
   *
   * @return metaData
   */
  public String getMetaData() {
    return this.metaData;
  }

  public void setMetaData(String metaData) {
    this.metaData = metaData;
    mergeConfiguration();
  }

  /**
   *
   *
   * <h1>Check remote version-tags</h1>
   *
   * <p>
   *
   * @param checkRemoteVersionTags set the check on remote versions flag
   */
  public void setCheckRemoteVersionTags(Boolean checkRemoteVersionTags) {
    this.checkRemoteVersionTags = checkRemoteVersionTags;
    mergeConfiguration();
  }

  /**
   *
   *
   * <h1>Check remote version-tags</h1>
   *
   * <p>Flag to determine if remote versions have to be tagged.
   *
   * @return is flag set?
   */
  public boolean checkRemoteVersionTags() {
    return this.checkRemoteVersionTags;
  }
}
