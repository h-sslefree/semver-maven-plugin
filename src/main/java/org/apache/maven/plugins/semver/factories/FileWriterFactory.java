package org.apache.maven.plugins.semver.factories;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.semver.SemverMavenPlugin;
import org.apache.maven.plugins.semver.providers.VersionProvider;
import org.apache.maven.project.MavenProject;

import java.io.*;
import java.nio.file.*;
import java.util.Map;

/**
 * @author sido
 */
public class FileWriterFactory {

  private FileWriterFactory() {
  }

  /**
   * <p>Create the release.properties file</p>
   *
   * @param LOG           @see {@link org.apache.maven.plugin.logging.Log}
   * @param project       @see {@link org.apache.maven.project.MavenProject}
   * @param finalVersions map with development, release and scm version
   */
  public static void createReleaseProperties(Log LOG, MavenProject project, Map<VersionProvider.FINAL_VERSION, String> finalVersions) {
    String mavenProjectRelease = "project.rel." + project.getGroupId() + "\\\u003A" + project.getArtifactId() + "\u003D" + finalVersions.get(VersionProvider.FINAL_VERSION.RELEASE);
    String mavenProjectDevelopment = "project.dev." + project.getGroupId() + "\\\u003A" + project.getArtifactId() + "\u003D" + finalVersions.get(VersionProvider.FINAL_VERSION.DEVELOPMENT);
    String mavenProjectScm = "scm.tag=" + finalVersions.get(VersionProvider.FINAL_VERSION.SCM);

    StringBuilder releaseText = new StringBuilder();
    releaseText.append(mavenProjectRelease);
    releaseText.append("\n");
    releaseText.append(mavenProjectDevelopment);
    releaseText.append("\n");
    releaseText.append(mavenProjectScm);

    writeFileToDisk(LOG, "release.properties", releaseText.toString());

  }

  /**
   * <p>Backup the old pom to make sure when the build fails it can be set back.</p>
   *
   * @param LOG @see {@link org.apache.maven.plugin.logging.Log}
   */
  public static void backupSemverPom(Log LOG) {
    if (LOG != null) {
      LOG.debug("Backup pom.xml");
      LOG.debug(SemverMavenPlugin.MOJO_LINE_BREAK);
    }
    try {
      File pomXmlOriginal = new File("pom.xml");
      File pomXmlSemverBackup = new File("pom.xml.semverBackup");
      if (pomXmlSemverBackup.exists()) {
        if (LOG != null) {
          LOG.debug("Old pom.xml.semverBackup removed  : " + pomXmlSemverBackup.getAbsolutePath());
        }
        boolean isDeleted = pomXmlSemverBackup.delete();
        if (!isDeleted) {
          if (LOG != null) {
            LOG.error("File is not removed               : pom.xml.semverBackup");
          }
        }
      }
      Files.copy(pomXmlOriginal.toPath(), pomXmlSemverBackup.toPath());

      if (LOG != null) {
        LOG.debug("New pom.xml.semverBackup prepared : " + pomXmlSemverBackup.getAbsolutePath());
      }

    } catch (IOException err) {
      if (LOG != null) {
        LOG.error("semver-maven-plugin is terminating");
        LOG.error("Error when creating new pom.xml.semverBackup", err);
      }
      Runtime.getRuntime().exit(1);
    }
    if (LOG != null) {
      LOG.debug(SemverMavenPlugin.FUNCTION_LINE_BREAK);
    }
  }

  public static void rollbackPom(Log LOG) {
    LOG.info("");
    LOG.info("Rollback pom.xml");
    LOG.info(SemverMavenPlugin.MOJO_LINE_BREAK);
    File pomXml= new File("pom.xml");
    File pomXmlSemverBackup = new File("pom.xml.semverBackup");
    LOG.info(" * Replace pom.xml with           : pom.xml.semverBackup");
    try {
      Files.copy(pomXmlSemverBackup.toPath(), pomXml.toPath(), StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException err) {
      LOG.error(err);
    }

    LOG.info(SemverMavenPlugin.FUNCTION_LINE_BREAK);
  }

  public static void removeBackupSemverPom(Log LOG) {
    LOG.info("");
    LOG.info("Clean backup pom.xml");
    LOG.info(SemverMavenPlugin.MOJO_LINE_BREAK);
    File pomXmlSemverBackup = new File("pom.xml.semverBackup");
    if (pomXmlSemverBackup.exists()) {
      LOG.info(" * Backup pom.xml exists!");
      LOG.info(" * Remove file                     : pom.xml.semverBackup");
      pomXmlSemverBackup.delete();
    }
    LOG.info(SemverMavenPlugin.FUNCTION_LINE_BREAK);
  }

  /**
   *
   * <p>Can we perform a rollback?</p>
   *
   * @param LOG logging for maven plugin
   * @return canRollback
   */
  public static boolean canRollBack(Log LOG) {
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
   * @param LOG         @see {@link org.apache.maven.plugin.logging.Log}
   * @param fileName    the name of the file
   * @param fileContent the full content for the pom.xml
   */
  public static void writeFileToDisk(Log LOG, String fileName, String fileContent) {
    try {
      if (fileName != null) {
        File file = new File(fileName);
        if (file.exists()) {
          if (LOG != null) {
            LOG.info("Old file: [ " + fileName + " ] removed      : " + file.getAbsolutePath());
          }
          boolean isDeleted = file.delete();
          if (!isDeleted) {
            if (LOG != null) {
              LOG.error("File: [ " + fileName + " ] is not removed            : " + file.getAbsolutePath());
            }
          }
        }
        if (LOG != null) {
          LOG.info("New [ " + fileName + " ] is prepared        : " + file.getAbsolutePath());
        }
        FileWriter writer = new FileWriter(file);
        Writer output = new BufferedWriter(writer);
        output.append(fileContent);
        output.close();
        writer.close();
      }
    } catch (IOException err) {
      if (LOG != null) {
        LOG.error("semver-maven-plugin is terminating");
        LOG.error("Error when creating file [ " + fileName + " ]", err);
        Runtime.getRuntime().exit(1);
      }
    }
  }

}
