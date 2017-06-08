package org.apache.maven.plugins.semver.configuration;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugins.semver.runmodes.RunMode;

/**
 * <p>Semver Configuration is used to merge 3 types of configuration:
 * <ul>
 * <li>MAVEN-plugin configuration</li>
 * <li>CLI configuration</li>
 * <li>Default configuration</li>
 * </ul>
 * </p>
 *
 * @author sido
 */
public class SemverConfiguration {

  private static final String BRANCH_CONVERSION_URL = "";

  private RunMode.RUNMODE runMode;
  private String branchVersion;
  private String scmUsername;
  private String scmPassword;
  private String branchConversionUrl;
  private String metaData;
  private Boolean checkRemoteVersionTags;

  private MavenSession session;

  /**
   * <p>Combining user properties with configuration properties in {@link SemverConfiguration}</p>
   *
   * @param session MavenSession
   */
  public SemverConfiguration(MavenSession session) {
    this.session = session;
    mergeConfiguration();
  }

  /**
   * <p>Merges the 3 kinds of configuration</p>
   */
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
      userCheckRemoteVersionTags = Boolean.valueOf(session.getUserProperties().getProperty("checkRemoteVersionTags"));
    }

    if (userRunMode != null && !userRunMode.isEmpty()) {
      runMode = RunMode.RUNMODE.convertToEnum(userRunMode);
    }
    if (runMode == RunMode.RUNMODE.RELEASE_BRANCH || runMode == RunMode.RUNMODE.RELEASE_BRANCH_RPM || runMode == RunMode.RUNMODE.NATIVE_BRANCH || runMode == RunMode.RUNMODE.NATIVE_BRANCH_RPM) {
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
        //TODO:SH Get username from settings.xml via plugin config
      }
    }

    if (scmPassword == null || scmPassword.isEmpty()) {
      scmPassword = userScmPassword;
      if (scmPassword == null || scmPassword.isEmpty()) {
        scmPassword = "";
        //TODO:SH Get password from settings.xml via plugin config
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
      if (userCheckRemoteVersionTags != null && !userCheckRemoteVersionTags) {
        checkRemoteVersionTags = userCheckRemoteVersionTags;
      } else {
        checkRemoteVersionTags = false;
      }
    }

  }

  /**
   * <p>Get RUNMODE</p>
   * <ul>Possible runModes are:
   * <li>When {@link RunMode.RUNMODE} = RELEASE then determine version from POM-version</li>
   * <li>When {@link RunMode.RUNMODE} = RELEASE_BRANCH then determine version from GIT-branch</li>
   * <li>When {@link RunMode.RUNMODE} = RELEASE_BRANCH_HOSEE then determine version from POM-version (without maven-release-plugin)</li>
   * <li>When {@link RunMode.RUNMODE} = NATIVE then determine version from POM-version (without maven-release-plugin)</li>
   * <li>When {@link RunMode.RUNMODE} = NATIVE_BRANCH then determine version from POM-version (without maven-release-plugin)</li>
   * <li>When {@link RunMode.RUNMODE} = RUNMODE_NOT_SPECIFIED does nothing</li>
   * </ul>
   *
   * @return RUNMODE
   */
  public RunMode.RUNMODE getRunMode() {
    return runMode;
  }

  /**
   * <p>Set RUNMODE and merge with configuration.</p>
   *
   * @param runMode kind of runMode the plugin is configured with
   */
  public void setRunMode(RunMode.RUNMODE runMode) {
    this.runMode = runMode;
    mergeConfiguration();
  }

  /**
   * <p>Returns the branchVersion of the current branch in which the parent project is in</p>
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
   * <p>To commit tags in SCM you have to have the username of the repostory</p>
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
   * <p>To commit tags in SCM you have to have the password of the repostory</p>
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
   * <p>
   * THe branchConversionUrl is used to determine which version contains <br>
   * is the master-branch. THis is necessary to determine the next version of the <br>
   * <b>master</b>-brnach.
   * </p>
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
   * <p>Version metaData is used to describe the version that is tagged.</p>
   * <p>
   * For example:<br>
   * RPM-version: 6.4.0-002001003+2.1.5<br>
   * THe +2.1.3 is the metaData from the version<br>
   * It is parsed from 002001003.
   * </p>
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
   * @param checkRemoteVersionTags
   */
  public void setCheckRemoteVersionTags(Boolean checkRemoteVersionTags) {
    this.checkRemoteVersionTags = checkRemoteVersionTags;
    mergeConfiguration();
  }

  /**
   *
   * <h1></h1>
   *
   * <p></p>
   *
   * @return
   */
  public boolean checkRemoteVersionTags() {
    return this.checkRemoteVersionTags;
  }


}
