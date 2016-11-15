package org.apache.maven.plugins.semver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

public abstract class SemverMavenPlugin extends AbstractMojo {

  protected Log log = getLog();

  protected Git currentGitRepo;
  protected CredentialsProvider credProvider;

  public static enum VERSION {
    DEVELOPMENT(0), 
    RELEASE(1), 
    MAJOR(2), 
    MINOR(3), 
    PATCH(4);

    private int index;

    private VERSION(int index) {
      this.index = index;
    }

    public int getIndex() {
      return this.index;
    }

  }

  /**
   * <ul>
   * <li>release: maak gebruik van normale semantic-versioning en release-plugin</li>
   * <li>release-rpm</li>
   * <li>native</li>
   * <li>native-rpm</li>
   * 
   * @author sido
   */
  public static enum RUNMODE {
    RELEASE, 
    RELEASE_RPM, 
    NATIVE, 
    NATIVE_RPM, 
    RUNMODE_NOT_SPECIFIED;

    public static RUNMODE convertToEnum(String runMode) {
      RUNMODE value = RUNMODE_NOT_SPECIFIED;
      if (runMode != null) {
        if (runMode.equals("RELEASE")) {
          value = RELEASE;
        } else if (runMode.equals("RELEASE_RPM")) {
          value = RELEASE_RPM;
        } else if (runMode.equals("NATIVE")) {
          value = NATIVE;
        } else if (runMode.equals("NATIVE_RPM")) {
          value = NATIVE_RPM;
        }
      }
      return value;
    }
  }

  @Parameter(property = "runMode", required = true, defaultValue="RELEASE")
  private RUNMODE runMode;

  @Parameter(property = "project", defaultValue = "${project}", readonly = true, required = true)
  protected MavenProject project;

  @Parameter(property = "username", defaultValue = "")
  protected String scmUsername = "";

  @Parameter(property = "password", defaultValue = "")
  protected String scmPassword = "";

  @Parameter(property = "tag")
  protected String preparedReleaseTag;

  @Parameter(defaultValue = "${session}", readonly = true, required = true)
  protected MavenSession session;

  @Parameter(property = "branchVersion")
  private String branchVersion;

  public void setRunMode(RUNMODE runMode) {
    this.runMode = runMode;
  }

  public void setBranchVersion(String branchVersion) {
    this.branchVersion = branchVersion;
  }

  public SemverConfiguration getConfiguration() {
    SemverConfiguration config = new SemverConfiguration();
    String userRunMode = session.getUserProperties().getProperty("runMode");
    String userBranchVersion = session.getUserProperties().getProperty("branchVersion");

    if (userRunMode != null) {
      runMode = RUNMODE.convertToEnum(userRunMode);
    }
    if(runMode == RUNMODE.RELEASE_RPM) {
      if (userBranchVersion != null) {
        branchVersion = userBranchVersion;
      } 
      if(branchVersion == null) {
        branchVersion = determineBranchVersionFromGitBranch();
      }
    } else {
      branchVersion = "";
    }

    if (runMode != null) config.setRunMode(runMode);
    if (branchVersion != null) config.setBranchVersion(branchVersion);
    return config;
  }

  private String determineBranchVersionFromGitBranch() {
	String value = null;
	log.info("------------------------------------------------------------------------");  
	log.info("Determine current branchVersion from GIT-repository");	
	try {
      initializeRepository();	
	} catch (Exception err) {
	  log.error(err.getMessage());
	}
	
	try {  
	  String branch = currentGitRepo.getRepository().getBranch();
	  log.info("Current branch                    : " + branch);
	  if(branch.matches("\\d+.\\d+.\\d+.*")) {
        log.info("Current branch matches            : \\d+.\\d+.\\d+.*");
        value = branch;
	  } else if (branch.matches("v\\d+_\\d+_\\d+.*")) {
		log.info("Current branch matches            : v\\d+_\\d+_\\d+.*");
		String rawBranch = branch.replaceAll("v", "").replaceAll("_", ".");
		value = rawBranch.substring(0, StringUtils.ordinalIndexOf(rawBranch, ".", 3));
 	  } else {
		log.error("Current branch does not match    : diget.diget.diget");
		log.error("Branch is not set, semantic versioning for RPM is terminated");
	  }
	} catch(Exception err) {
	  log.error("An error occured while trying to reach GIT-repo: " + err.getMessage());
	}
	log.info("------------------------------------------------------------------------");  
	
	return value;
  }
  
  protected void initializeRepository() throws Exception {
	if(currentGitRepo == null && credProvider == null) {
      log.info("Initializing GIT-repository");
      FileRepositoryBuilder repoBuilder = new FileRepositoryBuilder();
	  repoBuilder.addCeilingDirectory(project.getBasedir());
	  repoBuilder.findGitDir(project.getBasedir());
	  Repository repo = null;
	  try {
		repo = repoBuilder.build();
		currentGitRepo = new Git(repo);
		log.info(" - GIT-repository is initialized");
	  } catch (Exception err) {
	    log.error("This is not a valid GIT-repository.");
	    log.error("Please run this goal in a valid GIT-repository");
	    throw new Exception("This is not a valid GIT-repository. \nPlease run this goal in a valid GIT-repository");
	  }
	  if (!(scmPassword.isEmpty() || scmUsername.isEmpty())) {
	    credProvider = new UsernamePasswordCredentialsProvider(scmUsername, scmPassword);
		log.info(" - GIT-credential provider is initialized");
	  } else {
	    log.error("There are no valid credentials for the GIT-repo entered");
	    log.error("Please enter '-Dusername=#username# -Dpassword=#password#' on commandline to initialize GIT correctly");
	    throw new Exception("There are no valid credentials for the GIT-repo entered"
	    		+ "\nPlease enter '-Dusername=#username# -Dpassword=#password#' on commandline to initialize GIT correctly");
	  }
	} else {
	  log.debug(" - GIT repository and the credentialsprovider are already initialized");
	}
  }
  
  protected void cleanupGitLocalAndRemoteTags(String releaseVersion) throws IOException, GitAPIException {
    log.info("Check for lost-tags");
    log.info("------------------------------------------------------------------------");
    try {
      initializeRepository();
	} catch (Exception e) {
	  log.error(e.getMessage());	
	}  
    currentGitRepo.pull().setCredentialsProvider(credProvider).call();
    List<Ref> refs = currentGitRepo.tagList().call();
    log.debug("Remote tags: " + refs.toString());
    if (refs.size() > 0) {
      boolean found = false;
      for (Ref ref : refs) {
        if (ref.getName().contains(releaseVersion)) {
          found = true;
          log.info("Delete lost local-tag                 : " + ref.getName().substring(10));
          currentGitRepo.tagDelete().setTags(ref.getName()).call();
          RefSpec refSpec = new RefSpec().setSource(null).setDestination(ref.getName());
          log.info("Delete lost remote-tag                : " + ref.getName().substring(10));
          currentGitRepo.push().setRemote("origin").setRefSpecs(refSpec).setCredentialsProvider(credProvider).call();
        }
      }
      if (!found) {
        log.info("No local or remote lost tags found");
      }
    } else {
      log.info("No local or remote lost tags found");
    }
    currentGitRepo.close();
    log.info("------------------------------------------------------------------------");
  }

  protected void createReleaseNative(String developmentVersion, String releaseVersion) {
    // TODO Auto-generated method stub

  }

  protected void createReleaseRpm(String developmentVersion, int major, int minor, int patch) {

    log.info("NEW versions on RPM base");
    
    String releaseVersion = branchVersion + "-" + String.format("%03d%03d%03d", major, minor, patch);
    
    String buildMetaData = major + "." + minor + "." + patch;
    String scmVersion = releaseVersion + "+" + buildMetaData;
    
    log.info("New DEVELOPMENT-version               : " + developmentVersion);
    log.info("New RPM GIT build metadata            : " + buildMetaData);
    log.info("New RPM GIT-version                   : " + scmVersion);
    log.info("New RPM RELEASE-version               : " + releaseVersion);
    log.info("------------------------------------------------------------------------");

    createReleaseProperties(developmentVersion, releaseVersion, scmVersion);
  }

  protected void createReleaseProperties(String developmentVersion, String releaseVersion, String scmVersion) {
    String mavenProjectRelease = "project.rel." + project.getGroupId() + "\\\u003A" + project.getArtifactId() + "\u003D" + releaseVersion;
    String mavenProjectDevelopment = "project.dev." + project.getGroupId() + "\\\u003A" + project.getArtifactId() + "\u003D" + developmentVersion;
    String mavenProjectScm = "scm.tag=" + scmVersion;

    File releaseProperties = new File("release.properties");

    try {
      if (releaseProperties.exists()) {
        log.info("Old release.properties removed    : " + releaseProperties.getAbsolutePath());
        releaseProperties.delete();
      }
      Writer output = new BufferedWriter(new FileWriter(releaseProperties)); // clears file every time
      output.append(mavenProjectRelease + "\n");
      output.append(mavenProjectDevelopment + "\n");
      output.append(mavenProjectScm);
      output.close();
      log.info("New release.properties prepared   : " + releaseProperties.getAbsolutePath());
    } catch (IOException err) {
      log.error(err.getMessage());
      System.exit(0);
    }
  }

}
