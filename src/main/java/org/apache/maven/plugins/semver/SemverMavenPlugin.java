package org.apache.maven.plugins.semver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.jgit.lib.Repository;

public abstract class SemverMavenPlugin extends AbstractMojo {

  protected Log log = getLog();

  protected int DEVELOPMENT = 0;
  protected int RELEASE = 1;

  protected Repository repo;

  public static enum RUN_MODE {
    RELEASE("release"),
    NATIVE("native");
    
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
  public String scmUsername;
  
  @Parameter(property = "password", defaultValue = "")
  public String scmPassword;
  
  @Parameter(property = "tag")
  public String preparedReleaseTag;
  
  
  protected void createReleaseProperties(String developmentVersion, String releaseVersion) {
    String mavenProjectRelease = "project.rel."+ project.getGroupId() + "\\\u003A" + project.getArtifactId() + "\u003D" + releaseVersion; 
    String mavenProjectDevelopment = "project.dev." + project.getGroupId() + "\\\u003A" + project.getArtifactId() + "\u003D" + developmentVersion;
    String mavenProjectScm = "scm.tag="+ releaseVersion; 
    
    File releaseProperties = new File("release.properties"); 
    
    try {
      if(releaseProperties.exists()) {
        log.info("Old release.properties removed    : " + releaseProperties.getAbsolutePath());
        releaseProperties.delete();
      }
      Writer output = new BufferedWriter(new FileWriter(releaseProperties));  //clears file every time
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
