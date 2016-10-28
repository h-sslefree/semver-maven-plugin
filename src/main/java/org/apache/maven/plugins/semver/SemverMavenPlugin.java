package org.apache.maven.plugins.semver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.apache.maven.execution.MavenSession;
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

  protected Repository repo;

  public static enum VERSION {
    DEVELOPMENT(0), RELEASE(1), MAJOR(2), MINOR(3), PATCH(4);

    private int index;

    private VERSION(int index) {
      this.index = index;
    }

    public int getIndex() {
      return this.index;
    }

  }

  /**
   * <ul>
   * <li>release: maak gebruik van normale semantic-versioning en release-plugin</li>
   * <li>release-rpm</li>
   * <li>native</li>
   * <li>native-rpm</li>
   * 
   * @author sido
   */
  public static enum RUNMODE {
    RELEASE, RELEASE_RPM, NATIVE, NATIVE_RPM, RUNMODE_NOT_SPECIFIED;

    public static RUNMODE convertToEnum(String runMode) {
      RUNMODE value = RUNMODE_NOT_SPECIFIED;
      if (runMode != null) {
        if (runMode.equals("RELEASE")) {
          value = RELEASE;
        } else if (runMode.equals("RELEASE_RPM")) {
          value = RELEASE_RPM;
        } else if (runMode.equals("NATIVE")) {
          value = NATIVE;
        } else if (runMode.equals("NATIVE_RPM")) {
          value = NATIVE_RPM;
        }
      }
      return value;
    }
  }

  @Parameter(property = "runMode", required = true, name="runMode can be RELEASE, RELEASE_RPM, NATIVE and NATIVE_RPM")
  private RUNMODE runMode;

  @Parameter(property = "project", defaultValue = "${project}", readonly = true, required = true)
  protected MavenProject project;

  @Parameter(property = "username", defaultValue = "")
  protected String scmUsername = "";

  @Parameter(property = "password", defaultValue = "")
  protected String scmPassword = "";

  @Parameter(property = "tag")
  protected String preparedReleaseTag;

  @Parameter(defaultValue = "${session}", readonly = true, required = true)
  protected MavenSession session;

  @Parameter(property = "branchVersion", defaultValue = "6.4.0")
  private String branchVersion;

  public void setRunMode(RUNMODE runMode) {
    this.runMode = runMode;
  }

  public void setBranchVersion(String branchVersion) {
    this.branchVersion = branchVersion;
  }

  protected SemverConfiguration getConfiguration() {
    SemverConfiguration config = new SemverConfiguration();
    String userRunMode = session.getUserProperties().getProperty("runMode");
    String userBranchVersion = session.getUserProperties().getProperty("branchVersion");

    if (userRunMode != null) {
      runMode = RUNMODE.convertToEnum(userRunMode);
    }
    if (userBranchVersion != null) {
      branchVersion = userBranchVersion;
    }

    if (runMode != null) config.setRunMode(runMode);
    if (branchVersion != null) config.setBranchVersion(branchVersion);
    return config;
  }

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

  protected void createReleaseNative(String developmentVersion, String releaseVersion) {
    // TODO Auto-generated method stub

  }

  protected void createReleaseRpm(String developmentVersion, int major, int minor, int patch) {

    log.info("NEW versions on RPM base");
    
    String releaseVersion = branchVersion + "-" + String.format("%03d%03d%03d", major, minor, patch);
    
    log.info("New DEVELOPMENT-version               : " + developmentVersion);
    log.info("New RPM GIT-version                   : " + releaseVersion);
    log.info("New RPM RELEASE-version               : " + releaseVersion);
    log.info("------------------------------------------------------------------------");

    createReleaseProperties(developmentVersion, releaseVersion);
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
