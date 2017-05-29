package org.apache.maven.plugins.semver.providers;

/**
  *
  * <h>BranchProvider</h>
  * <p>When a version has a branch in it's GIT-tag, the branch-provider can be used to determine the branch for GIT.</p>
  *
  * @author sido
  */
public interface BranchProvider {

    String determineBranchVersionFromGitBranch(String branchVersion, String branchConversionUrl);
}
