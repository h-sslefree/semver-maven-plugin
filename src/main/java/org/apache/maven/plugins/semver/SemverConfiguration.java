package org.apache.maven.plugins.semver;

import org.apache.maven.plugins.semver.SemverMavenPlugin.RUNMODE;

public class SemverConfiguration {

  private RUNMODE runMode;
  private String branchVersion;
  
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
  
  
  
}
