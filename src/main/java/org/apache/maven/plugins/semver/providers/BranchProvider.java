package org.apache.maven.plugins.semver.providers;

/**
 * <h>BranchProvider</h>
 *
 * <p>When a version has a branch in it's GIT-tag, the branch-provider can be used to determine the
 * branch for GIT.
 *
 * @author sido
 */
public interface BranchProvider {

  /**
   * Determine branchVersion from GIT-branch
   *
   * @param branchVersion branch version for the GIT-tag
   * @return branchVersion
   */
  String determineBranchVersionFromGitBranch(String branchVersion, String branchConversionUrl);
}
