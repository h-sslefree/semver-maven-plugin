package org.apache.maven.plugins.semver.goals;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.semver.SemverMavenPlugin;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

@Mojo(name = "cleanup-git-tags")
public class SemverMavenPluginGoalCleanupGitTags extends SemverMavenPlugin {

  public void execute() throws MojoExecutionException, MojoFailureException {

    String version = project.getVersion();
    String scmConnection = project.getScm().getConnection();
    File scmRoot = project.getBasedir();

    log.info("Semver-goal                       : CLEANUP-GIT-TAGS");
    log.info("Run-mode                          : " + runMode);
    log.info("Version from POM                  : " + version);
    log.info("SCM-connection                    : " + scmConnection);
    log.info("SCM-root                          : " + scmRoot);
    log.info("------------------------------------------------------------------------");
    
    try {
      cleanupGitRemoteTags(scmConnection, scmRoot);
    } catch (IOException e) {
      log.error(e.getMessage());
    } catch (GitAPIException e) {
      log.error(e.getMessage());
    }
  }
  
  private void cleanupGitRemoteTags(String scmConnection, File scmRoot) throws IOException, GitAPIException {
    
    FileRepositoryBuilder repoBuilder = new FileRepositoryBuilder();
    repoBuilder.addCeilingDirectory(scmRoot);
    repoBuilder.findGitDir(scmRoot);

    repo = repoBuilder.build();

    log.info("Determine local and remote GIT-tags for GIT-repo");
    log.info("------------------------------------------------------------------------");
    
    CredentialsProvider cp = new UsernamePasswordCredentialsProvider(scmUsername, scmPassword);
    
    Git currentProject = new Git(repo);
    currentProject.pull().setCredentialsProvider(cp).call();
    List<Ref> refs = currentProject.tagList().call();
    if(refs.size() > 0) {
      boolean found = false;
      for (Ref ref : refs) {
        if(ref.getName().contains(preparedReleaseTag)) {
          found = true;
          log.info("Delete local GIT-tag                 : " + ref.getName().substring(10));
          currentProject.tagDelete().setTags(ref.getName()).call();
          RefSpec refSpec = new RefSpec().setSource(null).setDestination(ref.getName());
          log.info("Delete remote GIT-tag                : " + ref.getName().substring(10));
          currentProject.push().setRemote("origin").setRefSpecs(refSpec).setCredentialsProvider(cp).call();
        } 
      }
      if (!found) {
        log.info("No local or remote prepared GIT-tags found");
      }
    } else {
      log.info("No local or remote prepared GIT-tags found");  
    }
    
    currentProject.close();
    log.info("------------------------------------------------------------------------");
    
  }

}
