package org.apache.maven.plugins.semver.providers;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.DefaultModelWriter;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.semver.SemverMavenPlugin;
import org.apache.maven.plugins.semver.factories.FileWriterFactory;
import org.apache.maven.project.MavenProject;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * <h>PomProvider</h>
 * <p></p>
 *
 * @author sido
 */
public class PomProvider {

  private Log LOG;

  private RepositoryProvider repositoryProvider;

  private MavenProject project;

  public PomProvider(Log LOG, RepositoryProvider repositoryProvider, MavenProject project) {
    this.LOG = LOG;
    this.repositoryProvider = repositoryProvider;
    this.project = project;
  }

  /**
   *
   *
   *
   * @param finalVersions final versions from the plugin-foals
   */
  public void createReleasePom(Map<VersionProvider.FINAL_VERSION, String> finalVersions) {
    LOG.info("Create release-pom");
    LOG.info(SemverMavenPlugin.MOJO_LINE_BREAK);
    MavenProject releasePom = project;
    String scmTag = finalVersions.get(VersionProvider.FINAL_VERSION.SCM);
    releasePom.setVersion(finalVersions.get(VersionProvider.FINAL_VERSION.RELEASE));
    releasePom.getScm().setTag(scmTag);
    FileWriterFactory.writeFileToDisk(LOG, "pom.xml", modelToStringXml(releasePom.getModel()));
    String commitMessage = "[SEMVER] Create new release-pom for tag : [ " + scmTag + " ]";
    LOG.info(SemverMavenPlugin.MOJO_LINE_BREAK);
    LOG.info("Commit new release-pom             : " + commitMessage);
    repositoryProvider.commit(commitMessage);
    LOG.info("Create local scm-tag               : " + scmTag);
    repositoryProvider.createTag(scmTag);
    LOG.info("Push new release-pom and scm-tag");
    repositoryProvider.push();
    LOG.info(SemverMavenPlugin.FUNCTION_LINE_BREAK);
  }

  /**
   *
   *
   *
   * @param developmentVersion developmentVersion
   */
  public void createNextDevelopmentPom(String developmentVersion) {
    LOG.info("Create next development-pom");
    LOG.info(SemverMavenPlugin.MOJO_LINE_BREAK);
    MavenProject nextDevelopementPom = project;
    nextDevelopementPom.setVersion(developmentVersion);
    nextDevelopementPom.getScm().setTag("");
    FileWriterFactory.writeFileToDisk(LOG, "pom.xml", modelToStringXml(nextDevelopementPom.getModel()));
    String commitMessage = "[SEMVER] Create next development-pom with version : [ " + developmentVersion + " ]";
    LOG.info(SemverMavenPlugin.MOJO_LINE_BREAK);
    LOG.info("Commit next development-pom        : " + commitMessage);
    repositoryProvider.commit(commitMessage);
    LOG.info("Push next development-pom");
    repositoryProvider.push();
    LOG.info(SemverMavenPlugin.FUNCTION_LINE_BREAK);
  }

  private String modelToStringXml(Model model) {

    MavenXpp3Writer modelWriter = new MavenXpp3Writer();

    StringWriter output = new StringWriter();
    String result = "";
    try {
      modelWriter.write(output, model);
      result = output.getBuffer().toString();
    } catch (IOException e) {
      LOG.error("Cannot convert model to pom: " + e.getMessage());
    }
    return result;
  }

}
