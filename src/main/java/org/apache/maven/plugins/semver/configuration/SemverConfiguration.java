package org.apache.maven.plugins.semver.configuration;

import org.apache.maven.plugins.semver.SemverMavenPlugin.RUNMODE;

public class SemverConfiguration {

  private RUNMODE runMode;
  private String branchVersion;
  private String scmUsername;
  private String scmPassword;
  private String branchConversionUrl;

  public RUNMODE getRunMode() {
    return runMode;
  }
  
  public void setRunMode(RUNMODE runMode) {
    this.runMode = runMode;
  }
  
  public String getBranchVersion() {
    return branchVersion;
  }
  
  public void setBranchVersion(String branchVersion) {
    this.branchVersion = branchVersion;
  }

  public String getScmUsername() {
    return this.scmUsername;
  }

  public void setScmUsername(String scmUsername) {
    this.scmUsername = scmUsername;
  }

  public String getScmPassword() {
    return this.scmPassword;
  }

  public void setScmPassword(String scmPassword) {
    this.scmPassword = scmPassword;
  }

  public String getBranchConversionUrl() {
    return this.branchConversionUrl;
  }

  public void setBranchConversionUrl(String branchConversionUrl) {
    this.branchConversionUrl= branchConversionUrl;
  }

}
