package org.apache.maven.plugins.semver.goal;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.semver.SemverMavenPlugin;

@Mojo( name = "clean-up-tags")
public class SemverMavenPluginGoalClean extends SemverMavenPlugin {

  private Log log = getLog();
  
  public void execute() throws MojoExecutionException, MojoFailureException {
    log.info("");
    
    
  }

}
