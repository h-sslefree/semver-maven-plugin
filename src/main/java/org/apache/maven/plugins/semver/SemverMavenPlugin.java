package org.apache.maven.plugins.semver;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

public abstract class SemverMavenPlugin extends AbstractMojo {

  @Parameter(defaultValue = "release")
  public String runMode;

  @Parameter(property = "project", defaultValue = "${project}", readonly = true, required = true)
  public MavenProject project;
  
  
}
