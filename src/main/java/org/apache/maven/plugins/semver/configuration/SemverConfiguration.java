package org.apache.maven.plugins.semver.configuration;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugins.semver.SemverMavenPlugin.RUNMODE;

/**
 *
 *
 * <p>Semver Configuration is used to merge 3 types of configuration:
 * <ul>
 *   <li>MAVEN-plugin configuration</li>
 *   <li>CLI configuration</li>
 *   <li>Default configuration</li>
 * </ul>
 * </p>
 *
 * @author sido
 */
public class SemverConfiguration {

  private static final String BRANCH_CONVERSION_URL = "http://versionizer.bicat.com/v2/convert/branch_to_milestone/";

  private RUNMODE runMode;
  private String branchVersion;
  private String scmUsername;
  private String scmPassword;
  private String branchConversionUrl;
  private String metaData;

  private MavenSession session;

  /**
   * <p>Combining user properties with configuration properties in {@link SemverConfiguration}</p>
   *
   * @return {@link SemverConfiguration}
   */
  public SemverConfiguration(MavenSession session) {
    this.session = session;
    mergeConfiguration();
  }

  /**
   *
   * <p>Merges the 3 kinds of configuration</p>
   */
  private void mergeConfiguration() {
    String userRunMode = "";
    String userBranchVersion = "";
    String userScmUsername = "";
    String userScmPassword = "";
    String userBranchConversionUrl = "";
    String userMetaData = "";
    if(session != null) {
      userRunMode = session.getUserProperties().getProperty("runMode");
      userBranchVersion = session.getUserProperties().getProperty("branchVersion");
      userScmUsername = session.getUserProperties().getProperty("username");
      userScmPassword = session.getUserProperties().getProperty("password");
      userBranchConversionUrl = session.getUserProperties().getProperty("branchConversionUrl");
      userMetaData = session.getUserProperties().getProperty("userMetaData");
    }

    if (userRunMode != null) {
      runMode = RUNMODE.convertToEnum(userRunMode);
    }
    if (runMode == RUNMODE.RELEASE_BRANCH) {
      if (userBranchVersion != null) {
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

  }

  /**
   *
   * <p>Get runmode.</p>
   *
   * @return
   */
  public RUNMODE getRunMode() {
    return runMode;
  }
  
  public void setRunMode(RUNMODE runMode) {
    this.runMode = runMode;
    mergeConfiguration();
  }
  
  public String getBranchVersion() {
    return branchVersion;
  }
  
  public void setBranchVersion(String branchVersion) {
    this.branchVersion = branchVersion;
    mergeConfiguration();
  }

  public String getScmUsername() {
    return this.scmUsername;
  }

  public void setScmUsername(String scmUsername) {
    this.scmUsername = scmUsername;
    mergeConfiguration();
  }

  public String getScmPassword() {
    return this.scmPassword;
  }

  public void setScmPassword(String scmPassword) {
    this.scmPassword = scmPassword;
    mergeConfiguration();
  }

  public String getBranchConversionUrl() {
    return this.branchConversionUrl;
  }

  public void setBranchConversionUrl(String branchConversionUrl) {
    this.branchConversionUrl= branchConversionUrl;
    mergeConfiguration();
  }

  public void setMetaData(String metaData) {
    this.metaData = metaData;
    mergeConfiguration();
  }

  public String getMetaData() {
    return this.metaData;
  }



}
