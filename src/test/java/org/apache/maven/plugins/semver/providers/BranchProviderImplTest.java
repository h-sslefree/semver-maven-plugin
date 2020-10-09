package org.apache.maven.plugins.semver.providers;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BranchProviderImplTest {

  @Mock private RepositoryProvider repositoryProvider;

  private BranchProvider branchProvider;

  @Before
  public void before() {
    branchProvider = new BranchProviderImpl(repositoryProvider);
  }

  @Test
  public void determineBranchVersionFromGitBranch() {
    String version = "1.0.0";
    String branchConversionUrl = "https://branch.conversionurl.test";
    String branchVersion =
        branchProvider.determineBranchVersionFromGitBranch(version, branchConversionUrl);

    assertEquals("1.0.0", branchVersion);
  }
}
