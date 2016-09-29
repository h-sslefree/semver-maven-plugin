package com.bicat.semver.maven.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;

public abstract class SemverMavenPlugin extends AbstractMojo {

  @Parameter( property= "outputFormat", defaultValue="releaseProperties")
  public String ouputFormat;
  
}
