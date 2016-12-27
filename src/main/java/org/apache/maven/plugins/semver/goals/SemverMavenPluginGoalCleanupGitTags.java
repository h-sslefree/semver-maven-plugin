package org.apache.maven.plugins.semver.goals;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.semver.SemverMavenPlugin;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.RefSpec;

/**
 * 
 * 
 * @author sido
 *
 */
@Deprecated
@Mojo(name = "cleanup-git-tags")
public class SemverMavenPluginGoalCleanupGitTags extends SemverMavenPlugin {
  
  public void execute() throws MojoExecutionException, MojoFailureException {

    String version = project.getVersion();
    String scmConnection = project.getScm().getConnection();
    File scmRoot = project.getBasedir();

    log.info("Semver-goal                       : CLEANUP-GIT-TAGS");
    log.info("Run-mode                          : " + getConfiguration().getRunMode());
    log.info("Version from POM                  : " + version);
    log.info("SCM-connection                    : " + scmConnection);
    log.info("SCM-root                          : " + scmRoot);
    log.info("------------------------------------------------------------------------");
    
    try {
      cleanupGitRemoteTags(scmConnection, scmRoot);
    } catch (IOException e) {
      log.error("Error when determining config", e);
    } catch (GitAPIException e) {
      log.error("Error when determining GIT-repo", e);
    }
  }
  
  private void cleanupGitRemoteTags(String scmConnection, File scmRoot) throws IOException, GitAPIException {
    log.info("Determine local and remote GIT-tags for GIT-repo");
    log.info("------------------------------------------------------------------------");
    try {
	  initializeRepository();
	} catch (Exception e) {
      log.error(e.getMessage());
	}
    currentGitRepo.pull().setCredentialsProvider(credProvider).call();
    List<Ref> refs = currentGitRepo.tagList().call();
    if(refs.size() > 0) {
      boolean found = false;
      for (Ref ref : refs) {
        if(ref.getName().contains(preparedReleaseTag)) {
          found = true;
          log.info("Delete local GIT-tag                 : " + ref.getName().substring(10));
          currentGitRepo.tagDelete().setTags(ref.getName()).call();
          RefSpec refSpec = new RefSpec().setSource(null).setDestination(ref.getName());
          log.info("Delete remote GIT-tag                : " + ref.getName().substring(10));
          currentGitRepo.push().setRemote("origin").setRefSpecs(refSpec).setCredentialsProvider(credProvider).call();
        } 
      }
      if (!found) {
        log.info("No local or remote prepared GIT-tags found");
      }
    } else {
      log.info("No local or remote prepared GIT-tags found");  
    }
    
    currentGitRepo.close();
    log.info("------------------------------------------------------------------------");
    
  }

}
