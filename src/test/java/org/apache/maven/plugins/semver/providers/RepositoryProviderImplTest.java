package org.apache.maven.plugins.semver.providers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;

@RunWith(MockitoJUnitRunner.class)
public class RepositoryProviderImplTest
{
	private RepositoryProvider repositoryProvider;

	@Before
	public void setUp()
	{
		repositoryProvider = new RepositoryProviderImpl();
	}

	@Test
	public void testInitialize()
	{
		String scmUrl = "https://github.com/h-sslefree/test";
		String scmUsername = "test";
		String scmPassword = "xxxx";
		File baseDir = new File("src/test/resources/org/apache/maven/plugins/semver/providers/repo");
		repositoryProvider.initialize(baseDir, scmUrl, scmUsername, scmPassword);
	}

	@Test
	public void testCreatTag() {
		repositoryProvider.createTag("1.0.0");
	}
}
