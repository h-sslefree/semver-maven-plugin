package org.apache.maven.plugins.semver.factories;

import static java.lang.String.format;
import static java.nio.file.Files.copy;
import static java.nio.file.Files.delete;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.apache.maven.plugins.semver.SemverMavenPlugin.FUNCTION_LINE_BREAK;
import static org.apache.maven.plugins.semver.SemverMavenPlugin.MOJO_LINE_BREAK;

import java.io.*;
import java.util.Map;
import org.apache.maven.plugins.semver.providers.VersionProvider.FINAL_VERSION;
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

  private static final Logger logger = LoggerFactory.getLogger(FileWriterFactory.class);
  public static final String SEMVER_BACKUP = "pom.xml.semverBackup";

  private FileWriterFactory() {}

  /**
   * Create the release.properties file
   *
   * @param project {@link org.apache.maven.project.MavenProject}
   * @param finalVersions map with development, release and scm version
   */
  public static void createReleaseProperties(
      MavenProject project, Map<FINAL_VERSION, String> finalVersions) {
    String mavenProjectRelease =
        "project.rel."
            + project.getGroupId()
            + "\\\u003A"
            + project.getArtifactId()
            + "\u003D"
            + finalVersions.get(FINAL_VERSION.RELEASE);
    String mavenProjectDevelopment =
        "project.dev."
            + project.getGroupId()
            + "\\\u003A"
            + project.getArtifactId()
            + "\u003D"
            + finalVersions.get(FINAL_VERSION.DEVELOPMENT);
    String mavenProjectScm = "scm.tag=" + finalVersions.get(FINAL_VERSION.SCM);

    String releaseText =
        mavenProjectRelease + "\n" + mavenProjectDevelopment + "\n" + mavenProjectScm;
    writeFileToDisk("release.properties", releaseText);
  }

  /** Backup the old pom to make sure when the build fails it can be set back. */
  public static void backupSemverPom() {
    logger.info("Backup pom.xml");
    logger.info(MOJO_LINE_BREAK);
    try {
      File pomXmlOriginal = new File("pom.xml");
      File pomXmlSemverBackup = new File(SEMVER_BACKUP);
      if (pomXmlSemverBackup.exists()) {
        logger.warn(
            format("Old pom.xml.semverBackup removed  : %s", pomXmlSemverBackup.getAbsolutePath()));
        delete(pomXmlSemverBackup.toPath());
      }
      copy(pomXmlOriginal.toPath(), pomXmlSemverBackup.toPath());

      logger.info(
          format("New pom.xml.semverBackup prepared  : %s", pomXmlSemverBackup.getAbsolutePath()));

    } catch (IOException err) {
      logger.error("semver-maven-plugin is terminating");
      logger.error("Error when creating new pom.xml.semverBackup", err);
      Runtime.getRuntime().exit(1);
    }
    logger.info(FUNCTION_LINE_BREAK);
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
    logger.info(" * Replace pom.xml with            : pom.xml.semverBackup");
    try {
      copy(pomXmlSemverBackup.toPath(), pomXml.toPath(), REPLACE_EXISTING);
    } catch (IOException err) {
      logger.error(err.getMessage());
    }
  }

  /**
   * <h>Cleanup the backup pom.xml</h>
   *
   * <p>Remove the pom.xml.semverBackup is exists.
   */
  public static void removeBackupSemverPom() {
    logger.info("Cleanup pom.xml.semverBackup");
    logger.info(MOJO_LINE_BREAK);
    File pomXmlSemverBackup = new File(SEMVER_BACKUP);
    if (pomXmlSemverBackup.exists()) {
      try {
        logger.info(" * Remove file                     : pom.xml.semverBackup");
        delete(pomXmlSemverBackup.toPath());
      } catch (IOException e) {
        logger.error(format(" * File could not be removed: [ %s ]", e.getMessage()));
        Runtime.getRuntime().exit(1);
      }
    }
    logger.info(FUNCTION_LINE_BREAK);
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
      logger.error("");
      logger.error("There is no pom.xml.semverBackup present");
      logger.error("The rollback can not be performed");
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
          logger.info(
              format("Old file: [ %s ] removed      : %s", fileName, file.getAbsolutePath()));
          delete(file.toPath());
        }
        logger.info(format("New [ %s ] is prepared        : %s", fileName, file.getAbsolutePath()));

        output.append(fileContent);
      } catch (IOException err) {
        logger.error("semver-maven-plugin is terminating");
        logger.error(format("Error when creating file [ %s ]", err));
        Runtime.getRuntime().exit(1);
      }
    }
  }
}
