package org.apache.maven.plugins.semver.providers;

import static org.junit.Assert.*;

import java.io.File;
import org.apache.maven.plugins.semver.exceptions.RepositoryInitialisationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RepositoryProviderImplTest {

  private RepositoryProvider repositoryProvider;

  @Before
  public void setUp() {
    repositoryProvider = new RepositoryProviderImpl();
  }

  @Test(expected = RepositoryInitialisationException.class)
  public void testInitialize() throws RepositoryInitialisationException {
    String scmUrl = "https://github.com/h-sslefree/test";
    String scmUsername = "test";
    String scmPassword = "xxxx";
    File baseDir = new File("");
    repositoryProvider.initialize(baseDir, scmUrl, scmUsername, scmPassword);
  }
}
