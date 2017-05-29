package org.apache.maven.plugins.semver.factories;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.semver.SemverMavenPlugin;
import org.apache.maven.plugins.semver.providers.VersionProvider;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.util.Map;

/**
 *
 * <h1>FileWriterFactory</h1>
 *
 * <p>This class performs all disk related actions.</p>
 *
 * @author sido
 */
public class FileWriterFactory {

  private static final Logger LOG = LoggerFactory.getLogger(FileWriterFactory.class);

  private FileWriterFactory() {
  }

  /**
   * <p>Create the release.properties file</p>
   *
   * @param project {@link org.apache.maven.project.MavenProject}
   * @param finalVersions map with development, release and scm version
   */
  public static void createReleaseProperties(MavenProject project, Map<VersionProvider.FINAL_VERSION, String> finalVersions) {
    String mavenProjectRelease = "project.rel." + project.getGroupId() + "\\\u003A" + project.getArtifactId() + "\u003D" + finalVersions.get(VersionProvider.FINAL_VERSION.RELEASE);
    String mavenProjectDevelopment = "project.dev." + project.getGroupId() + "\\\u003A" + project.getArtifactId() + "\u003D" + finalVersions.get(VersionProvider.FINAL_VERSION.DEVELOPMENT);
    String mavenProjectScm = "scm.tag=" + finalVersions.get(VersionProvider.FINAL_VERSION.SCM);

    StringBuilder releaseText = new StringBuilder();
    releaseText.append(mavenProjectRelease);
    releaseText.append("\n");
    releaseText.append(mavenProjectDevelopment);
    releaseText.append("\n");
    releaseText.append(mavenProjectScm);

    writeFileToDisk("release.properties", releaseText.toString());

  }

  /**
   *
   *
   * <p>Backup the old pom to make sure when the build fails it can be set back.</p>
   *
   */
  public static void backupSemverPom() {
    LOG.info("Backup pom.xml");
    LOG.info(SemverMavenPlugin.MOJO_LINE_BREAK);
    try {
      File pomXmlOriginal = new File("pom.xml");
      File pomXmlSemverBackup = new File("pom.xml.semverBackup");
      if (pomXmlSemverBackup.exists()) {
        LOG.warn("Old pom.xml.semverBackup removed  : " + pomXmlSemverBackup.getAbsolutePath());
        boolean isDeleted = pomXmlSemverBackup.delete();
        if (!isDeleted) {
          LOG.error("File is not removed               : pom.xml.semverBackup");
        }
      }
      Files.copy(pomXmlOriginal.toPath(), pomXmlSemverBackup.toPath());

      LOG.info("New pom.xml.semverBackup prepared  : " + pomXmlSemverBackup.getAbsolutePath());

    } catch (IOException err) {
      LOG.error("semver-maven-plugin is terminating");
      LOG.error("Error when creating new pom.xml.semverBackup", err);
      Runtime.getRuntime().exit(1);
    }
    LOG.info(SemverMavenPlugin.FUNCTION_LINE_BREAK);
  }

  /**
   *
   * <h1>Rollback pom.xml</h1>
   * <p>Replace the current pom.xml with the pom.xml.semverBackup.</p>
   *
   */
  public static void rollbackPom() {
    File pomXml= new File("pom.xml");
    File pomXmlSemverBackup = new File("pom.xml.semverBackup");
    LOG.info(" * Replace pom.xml with            : pom.xml.semverBackup");
    try {
      Files.copy(pomXmlSemverBackup.toPath(), pomXml.toPath(), StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException err) {
      LOG.error(err.getMessage());
    }
  }

  /**
   * <h>Cleanup the backup pom.xml</h>
   * <p>Remove the pom.xml.semverBackup is exists.</p>
   *
   */
  public static void removeBackupSemverPom() {
    LOG.info("Cleanup pom.xml.semverBackup");
    LOG.info(SemverMavenPlugin.MOJO_LINE_BREAK);
    File pomXmlSemverBackup = new File("pom.xml.semverBackup");
    if (pomXmlSemverBackup.exists()) {
      LOG.info(" * Remove file                     : pom.xml.semverBackup");
      pomXmlSemverBackup.delete();
    }
    LOG.info(SemverMavenPlugin.FUNCTION_LINE_BREAK);
  }

  /**
   *
   * <p>Can we perform a rollback?</p>
   *
   * @return canRollback
   */
  public static boolean canRollBack() {
    boolean canRollback = false;
    File pomXmlSemverBackup = new File("pom.xml.semverBackup");
    if(pomXmlSemverBackup.exists()) {
      canRollback = true;
    } else {
      LOG.error("");
      LOG.error("There is no pom.xml.semverBackup present");
      LOG.error("The rollback can not be performed");
    }
    return canRollback;
  }

  /**
   * <p>Write actual file to disk</p>
   *
   * @param fileName    the name of the file
   * @param fileContent the full content for the pom.xml
   */
  public static void writeFileToDisk(String fileName, String fileContent) {
    try {
      if (fileName != null) {
        File file = new File(fileName);
        if (file.exists()) {
          LOG.info("Old file: [ " + fileName + " ] removed      : " + file.getAbsolutePath());
          boolean isDeleted = file.delete();
          if (!isDeleted) {
            LOG.error("File: [ " + fileName + " ] is not removed            : " + file.getAbsolutePath());
          }
        }
        LOG.info("New [ " + fileName + " ] is prepared        : " + file.getAbsolutePath());
        FileWriter writer = new FileWriter(file);
        Writer output = new BufferedWriter(writer);
        output.append(fileContent);
        output.close();
        writer.close();
      }
    } catch (IOException err) {
      LOG.error("semver-maven-plugin is terminating");
      LOG.error("Error when creating file [ " + fileName + " ]", err);
      Runtime.getRuntime().exit(1);
    }
  }

}
