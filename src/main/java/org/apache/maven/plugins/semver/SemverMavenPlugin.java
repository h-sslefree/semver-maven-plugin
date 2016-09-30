package org.apache.maven.plugins.semver;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

public abstract class SemverMavenPlugin extends AbstractMojo {

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
  
  
}
