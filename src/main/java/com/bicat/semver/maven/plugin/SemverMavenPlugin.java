package com.bicat.semver.maven.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

public abstract class SemverMavenPlugin extends AbstractMojo {

  @Parameter(defaultValue = "release")
  public String runMode;

  
  
  
}
