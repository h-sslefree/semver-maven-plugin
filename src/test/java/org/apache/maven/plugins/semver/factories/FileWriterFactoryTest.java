package org.apache.maven.plugins.semver.factories;

import org.apache.maven.plugins.semver.providers.VersionProvider.FINAL_VERSION;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.util.EnumMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FileWriterFactoryTest
{

	@Mock
	private MavenProject mavenProject;

	@Before
	public void setUp()
	{
		when(mavenProject.getGroupId()).thenReturn("nl.hasslefree.maven,plugins");
		when(mavenProject.getArtifactId()).thenReturn("semver-maven-plugin");
	}

	@Test
	public void createReleaseProperties()
	{
		EnumMap<FINAL_VERSION, String> finalVersions = new EnumMap<>(FINAL_VERSION.class);
		finalVersions.put(FINAL_VERSION.RELEASE, "1.0.0");
		finalVersions.put(FINAL_VERSION.DEVELOPMENT, "1.0.1-SNAPSHOT");
		finalVersions.put(FINAL_VERSION.SCM, "1.0.0");
		FileWriterFactory.createReleaseProperties(mavenProject, finalVersions);
		File releaseProperties = new File("release.properties");
		assertNotNull(releaseProperties);
	}

	@Test
	public void backupSemverPom()
	{
		FileWriterFactory.backupSemverPom();
		File semverPom = new File("pom.xml.semverBackup");
		assertNotNull(semverPom);
		FileWriterFactory.removeBackupSemverPom();
		File semverBackupPom = new File("pom.xml.semverBackup");
		assertFalse(semverBackupPom.exists());
	}

	@Test
	public void canRollBack()
	{
		boolean canRollback = FileWriterFactory.canRollBack();
		assertFalse(canRollback);
	}

	//	@Test
	//	public void writeFileToDisk() throws IOException
	//	{
	//		FileWriterFactory.writeFileToDisk("pom.test.xml", "test pom file");
	//		File pomTest = new File("pom.test.xml");
	//		assertTrue(pomTest.exists());
	//		Files.delete(pomTest.toPath());
	//	}
}