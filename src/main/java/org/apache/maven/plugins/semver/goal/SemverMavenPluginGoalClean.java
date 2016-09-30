package org.apache.maven.plugins.semver.goal;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.semver.SemverMavenPlugin;

@Mojo( name = "clean")
public class SemverMavenPluginGoalClean extends SemverMavenPlugin {

  public void execute() throws MojoExecutionException, MojoFailureException {
    Log log = getLog();
  }

}
