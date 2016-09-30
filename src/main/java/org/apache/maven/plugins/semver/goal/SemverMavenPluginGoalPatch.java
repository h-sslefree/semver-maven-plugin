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

@Mojo(name = "patch", requiresProject = true)
public class SemverMavenPluginGoalPatch extends SemverMavenPlugin {

  public void execute() throws MojoExecutionException, MojoFailureException {
    Log log = getLog();
    log.info("Semver-goal                       : PATCH");
    log.info("Run-mode                          : " + runMode);
    log.info("Version from POM                  : " + project.getVersion());
    log.info("--------------------------------------------------");
    
    
    String version = project.getVersion();
   
    int majorVersion = 0;
    int minorVersion = 0;
    int patchVersion = 1;
    
    String[] rawVersion = version.split("\\.");
    if(rawVersion.length == 3) {
      log.debug("Set version-variables from POM.xml");
      log.debug("--------------------------------------------------");
      majorVersion = Integer.valueOf(rawVersion[0]);
      minorVersion = Integer.valueOf(rawVersion[1]);
      patchVersion = Integer.valueOf(rawVersion[2].substring(0, 1));
    }
    
    log.debug("MAJOR-version                    : " + majorVersion);
    log.debug("MINOR-version                    : " + minorVersion);
    log.debug("PATCH-version                    : " + patchVersion);
    log.debug("--------------------------------------------------");
    
    patchVersion = patchVersion + 1;
    
    String developmentVersion = majorVersion + "." + minorVersion + "." + patchVersion + "-SNAPSHOT";
    String releaseVersion = majorVersion + "." + minorVersion + "." + patchVersion;
    log.info("New DEVELOPMENT-version           : " + developmentVersion);
    log.info("New GIT-version                   : " + releaseVersion);
    log.info("New RELEASE-version               : " + releaseVersion);
    log.info("--------------------------------------------------");
    
    String mavenProjectRelease = "project.rel.com.bicat:" + project.getArtifactId() + "=" + releaseVersion; 
    String mavenProjectDevelopment = "project.dev.com.bicat:" + project.getArtifactId() + "=" + developmentVersion;
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
