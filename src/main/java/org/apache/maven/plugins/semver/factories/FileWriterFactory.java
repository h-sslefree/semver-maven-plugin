package org.apache.maven.plugins.semver.factories;

import static java.lang.String.format;
import static java.nio.file.Files.copy;
import static java.nio.file.Files.delete;

import java.io.*;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import org.apache.maven.plugins.semver.SemverMavenPlugin;
import org.apache.maven.plugins.semver.providers.VersionProvider;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 * <h1>FileWriterFactory</h1>
 *
 * <p>This class performs all disk related actions.
 *
 * @author sido
 */
public class FileWriterFactory {

  private static final Logger LOG = LoggerFactory.getLogger(FileWriterFactory.class);
  public static final String SEMVER_BACKUP = "pom.xml.semverBackup";

  private FileWriterFactory() {}

  /**
   * Create the release.properties file
   *
   * @param project {@link org.apache.maven.project.MavenProject}
   * @param finalVersions map with development, release and scm version
   */
  public static void createReleaseProperties(
      MavenProject project, Map<VersionProvider.FINAL_VERSION, String> finalVersions) {
    String mavenProjectRelease =
        "project.rel."
            + project.getGroupId()
            + "\\\u003A"
            + project.getArtifactId()
            + "\u003D"
            + finalVersions.get(VersionProvider.FINAL_VERSION.RELEASE);
    String mavenProjectDevelopment =
        "project.dev."
            + project.getGroupId()
            + "\\\u003A"
            + project.getArtifactId()
            + "\u003D"
            + finalVersions.get(VersionProvider.FINAL_VERSION.DEVELOPMENT);
    String mavenProjectScm = "scm.tag=" + finalVersions.get(VersionProvider.FINAL_VERSION.SCM);

    String releaseText =
        mavenProjectRelease + "\n" + mavenProjectDevelopment + "\n" + mavenProjectScm;
    writeFileToDisk("release.properties", releaseText);
  }

  /** Backup the old pom to make sure when the build fails it can be set back. */
  public static void backupSemverPom() {
    LOG.info("Backup pom.xml");
    LOG.info(SemverMavenPlugin.MOJO_LINE_BREAK);
    try {
      File pomXmlOriginal = new File("pom.xml");
      File pomXmlSemverBackup = new File(SEMVER_BACKUP);
      if (pomXmlSemverBackup.exists()) {
        LOG.warn(
            format("Old pom.xml.semverBackup removed  : %s", pomXmlSemverBackup.getAbsolutePath()));
        delete(pomXmlSemverBackup.toPath());
      }
      copy(pomXmlOriginal.toPath(), pomXmlSemverBackup.toPath());

      LOG.info(
          format("New pom.xml.semverBackup prepared  : %s", pomXmlSemverBackup.getAbsolutePath()));

    } catch (IOException err) {
      LOG.error("semver-maven-plugin is terminating");
      LOG.error("Error when creating new pom.xml.semverBackup", err);
      Runtime.getRuntime().exit(1);
    }
    LOG.info(SemverMavenPlugin.FUNCTION_LINE_BREAK);
  }

  /**
   *
   *
   * <h1>Rollback pom.xml</h1>
   *
   * <p>Replace the current pom.xml with the pom.xml.semverBackup.
   */
  public static void rollbackPom() {
    File pomXml = new File("pom.xml");
    File pomXmlSemverBackup = new File(SEMVER_BACKUP);
    LOG.info(" * Replace pom.xml with            : pom.xml.semverBackup");
    try {
      copy(pomXmlSemverBackup.toPath(), pomXml.toPath(), StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException err) {
      LOG.error(err.getMessage());
    }
  }

  /**
   * <h>Cleanup the backup pom.xml</h>
   *
   * <p>Remove the pom.xml.semverBackup is exists.
   */
  public static void removeBackupSemverPom() {
    LOG.info("Cleanup pom.xml.semverBackup");
    LOG.info(SemverMavenPlugin.MOJO_LINE_BREAK);
    File pomXmlSemverBackup = new File(SEMVER_BACKUP);
    if (pomXmlSemverBackup.exists()) {
      try {
        LOG.info(" * Remove file                     : pom.xml.semverBackup");
        delete(pomXmlSemverBackup.toPath());
      } catch (IOException e) {
        LOG.error(format(" * File could not be removed: [ %s ]", e.getMessage()));
        Runtime.getRuntime().exit(1);
      }
    }
    LOG.info(SemverMavenPlugin.FUNCTION_LINE_BREAK);
  }

  /**
   * Can we perform a rollback?
   *
   * @return canRollback
   */
  public static boolean canRollBack() {
    boolean canRollback = false;
    File pomXmlSemverBackup = new File(SEMVER_BACKUP);
    if (pomXmlSemverBackup.exists()) {
      canRollback = true;
    } else {
      LOG.error("");
      LOG.error("There is no pom.xml.semverBackup present");
      LOG.error("The rollback can not be performed");
    }
    return canRollback;
  }

  /**
   * Write actual file to disk
   *
   * @param fileName the name of the file
   * @param fileContent the full content for the pom.xml
   */
  public static void writeFileToDisk(String fileName, String fileContent) {
    if (fileName != null) {
      File file = new File(fileName);
      try (FileWriter writer = new FileWriter(file);
          Writer output = new BufferedWriter(writer)) {
        if (file.exists()) {
          LOG.info(format("Old file: [ %s ] removed      : %s", fileName, file.getAbsolutePath()));
          delete(file.toPath());
        }
        LOG.info(format("New [ %s ] is prepared        : %s", fileName, file.getAbsolutePath()));

        output.append(fileContent);
      } catch (IOException err) {
        LOG.error("semver-maven-plugin is terminating");
        LOG.error(format("Error when creating file [ %s ]", err));
        Runtime.getRuntime().exit(1);
      }
    }
  }
}
