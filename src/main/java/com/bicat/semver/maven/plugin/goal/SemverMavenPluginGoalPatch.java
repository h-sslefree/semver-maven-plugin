package com.bicat.semver.maven.plugin.goal;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import com.bicat.semver.maven.plugin.SemverMavenPlugin;

@Mojo(name = "patch", requiresProject = true )
public class SemverMavenPluginGoalPatch extends SemverMavenPlugin {

  @Parameter(property="project", defaultValue = "${project}", readonly = true, required = true)
  public MavenProject project;
  
  public void execute() throws MojoExecutionException, MojoFailureException {
    Log log = getLog();
    log.info("Semver-goal      : PATCH");
    log.info("Run-mode         : " + runMode);
    log.info("Version from POM : " + project.getVersion());
    
    
  }

}
