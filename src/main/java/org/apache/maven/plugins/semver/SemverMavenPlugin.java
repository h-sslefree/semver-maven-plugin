package org.apache.maven.plugins.semver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

public abstract class SemverMavenPlugin extends AbstractMojo {

  protected Log log = getLog();

  protected int DEVELOPMENT = 0;
  protected int RELEASE = 1;

  protected Repository repo;

  public static enum RUN_MODE {
    RELEASE("release"), NATIVE("native");

    private String key;

    private RUN_MODE(String key) {
      this.key = key;
    }

    public String getKey() {
      return this.key;
    };
  }

  @Parameter(defaultValue = "release")
  public String runMode;

  @Parameter(property = "project", defaultValue = "${project}", readonly = true, required = true)
  public MavenProject project;

  @Parameter(property = "username", defaultValue = "")
  public String scmUsername = ""; 

  @Parameter(property = "password", defaultValue = "")
  public String scmPassword= "";

  @Parameter(property = "tag")
  public String preparedReleaseTag;

  protected void cleanupGitLocalAndRemoteTags(String releaseVersion) throws IOException, GitAPIException {
    FileRepositoryBuilder repoBuilder = new FileRepositoryBuilder();
    repoBuilder.addCeilingDirectory(project.getBasedir());
    repoBuilder.findGitDir(project.getBasedir());

    repo = repoBuilder.build();

    log.info("Check for lost-tags");
    log.info("------------------------------------------------------------------------");

    if (!(scmPassword.isEmpty() || scmUsername.isEmpty())) {

      CredentialsProvider cp = new UsernamePasswordCredentialsProvider(scmUsername, scmPassword);

      Git currentProject = new Git(repo);
      currentProject.pull().setCredentialsProvider(cp).call();
      List<Ref> refs = currentProject.tagList().call();
      log.debug("Remote tags: " + refs.toString());
      if (refs.size() > 0) {
        boolean found = false;
        for (Ref ref : refs) {
          if (ref.getName().contains(releaseVersion)) {
            found = true;
            log.info("Delete lost local-tag                 : " + ref.getName().substring(10));
            currentProject.tagDelete().setTags(ref.getName()).call();
            RefSpec refSpec = new RefSpec().setSource(null).setDestination(ref.getName());
            log.info("Delete lost remote-tag                : " + ref.getName().substring(10));
            currentProject.push().setRemote("origin").setRefSpecs(refSpec).setCredentialsProvider(cp).call();
          }
        }
        if (!found) {
          log.info("No local or remote lost tags found");
        }
      } else {
        log.info("No local or remote lost tags found");
      }
      currentProject.close();
    } else {
      log.error("Could not load tags from remote repo. Failed to determine credentials");
      log.error("Please enter username and password for GIT-repo (-Dusername=#username# -Dpassword=#password#)");
    }

    log.info("------------------------------------------------------------------------");
  }

  protected void createReleaseProperties(String developmentVersion, String releaseVersion) {
    String mavenProjectRelease = "project.rel." + project.getGroupId() + "\\\u003A" + project.getArtifactId() + "\u003D" + releaseVersion;
    String mavenProjectDevelopment = "project.dev." + project.getGroupId() + "\\\u003A" + project.getArtifactId() + "\u003D" + developmentVersion;
    String mavenProjectScm = "scm.tag=" + releaseVersion;

    File releaseProperties = new File("release.properties");

    try {
      if (releaseProperties.exists()) {
        log.info("Old release.properties removed    : " + releaseProperties.getAbsolutePath());
        releaseProperties.delete();
      }
      Writer output = new BufferedWriter(new FileWriter(releaseProperties)); // clears file every time
      output.append(mavenProjectRelease + "\n");
      output.append(mavenProjectDevelopment + "\n");
      output.append(mavenProjectScm);
      output.close();
      log.info("New release.properties prepared   : " + releaseProperties.getAbsolutePath());
    } catch (IOException err) {
      log.error(err.getMessage());
    }
  }

}
