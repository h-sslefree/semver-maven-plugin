package org.apache.maven.plugins.semver;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.semver.configuration.SemverConfiguration;
import org.apache.maven.plugins.semver.exceptions.SemverException;
import org.apache.maven.project.MavenProject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.*;
import java.util.List;

public abstract class SemverMavenPlugin extends AbstractMojo {

  private SemverConfiguration configuration;

  private static final String BRANCH_CONVERSION_URL = "http://versionizer.bicat.com/v2/convert/branch_to_milestone/";

  protected static final String MOJO_LINE_BREAK = "------------------------------------------------------------------------";
  private static final String FUNCTION_LINE_BREAK = "************************************************************************";
  protected Log log = getLog();
  protected Git currentGitRepo;
  protected CredentialsProvider credProvider;
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
  private boolean isRemoteEnabled = false;
  @Parameter(property = "runMode", required = true, defaultValue = "RELEASE")
  private RUNMODE runMode;
  @Parameter(property = "branchVersion")
  private String branchVersion;
  @Parameter(property = "branchConversionUrl", defaultValue = BRANCH_CONVERSION_URL)
  private String branchConversionUrl;

  /**
   * <p>Override runMode through configuration properties</p>
   *
   * @param runMode get runMode from plugin configuration
   */
  public void setRunMode(RUNMODE runMode) {
    this.runMode = runMode;
  }

  /**
   * <p>Override branchVersion through configuration properties</p>
   *
   * @param branchVersion get branchVersion from plugin configuration
   */
  public void setBranchVersion(String branchVersion) {
    this.branchVersion = branchVersion;
  }

  /**
   * <p>Combining user properties with configuration properties in {@link SemverConfiguration}</p>
   *
   * @return {@link SemverConfiguration}
   */
  public SemverConfiguration getConfiguration() {
    if (configuration == null) {
      configuration = new SemverConfiguration();
      String userRunMode = session.getUserProperties().getProperty("runMode");
      String userBranchVersion = session.getUserProperties().getProperty("branchVersion");
      String userScmUsername = session.getUserProperties().getProperty("username");
      String userScmPassword = session.getUserProperties().getProperty("password");
      String userBranchConversionUrl = session.getUserProperties().getProperty("branchConversionUrl");

      if (userRunMode != null) {
        runMode = RUNMODE.convertToEnum(userRunMode);
      }
      if (runMode == RUNMODE.RELEASE_RPM) {
        if (userBranchVersion != null) {
          branchVersion = userBranchVersion;
        }
        if (branchVersion == null) {
          branchVersion = determineBranchVersionFromGitBranch();
        }
      } else {
        branchVersion = "";
      }

      if (scmUsername == null || scmUsername.isEmpty()) {
        scmUsername = userScmUsername;
        if (scmUsername == null || scmUsername.isEmpty()) {
          scmUsername = "";
          //TODO:SH Get username from settings.xml via plugin config
        }
      }

      if (scmPassword == null || scmPassword.isEmpty()) {
        scmPassword = userScmPassword;
        if (scmPassword == null || scmPassword.isEmpty()) {
          scmPassword = "";
          //TODO:SH Get password from settings.xml via plugin config
        }
      }

      if ((branchConversionUrl == null || branchConversionUrl.isEmpty()) ||
              (userBranchConversionUrl != null && !userBranchConversionUrl.equals(branchConversionUrl))) {
        branchConversionUrl = userBranchConversionUrl;
      } else {
        branchConversionUrl = BRANCH_CONVERSION_URL;
      }

      if (runMode != null) {
        configuration.setRunMode(runMode);
      }
      if (branchVersion != null) {
        configuration.setBranchVersion(branchVersion);
      }
      if (scmUsername != null) {
        configuration.setScmUsername(scmUsername);
      }
      if (scmPassword != null) {
        configuration.setScmPassword(scmPassword);
      }
      if (branchConversionUrl != null) {
        configuration.setBranchConversionUrl(branchConversionUrl);
      }
      return configuration;
    } else {
      return configuration;
    }
  }

  /**
   * <p>Initialize GIT-repo for determining branch and tag information.</p>
   *
   * @throws SemverException exception for not initializing local and remote repository
   */
  protected void initializeRepository() throws SemverException {
    log.info(FUNCTION_LINE_BREAK);
    if (currentGitRepo == null && credProvider == null) {
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
        log.error(" - This is not a valid GIT-repository.");
        log.error(" - Please run this goal in a valid GIT-repository");
        log.error(" - Could not initialize repostory", err);
        throw new SemverException("This is not a valid GIT-repository", "Please run this goal in a valid GIT-repository");
      }
      if (!(getConfiguration().getScmPassword().isEmpty() || getConfiguration().getScmUsername().isEmpty())) {
        isRemoteEnabled = true;
        credProvider = new UsernamePasswordCredentialsProvider(getConfiguration().getScmUsername(), getConfiguration().getScmPassword());
        log.info(" - GIT-credential provider is initialized");
      } else {
        log.warn(" - There is no connection to the remote GIT-repository");
        log.debug(" - To make a connection to the remote please enter '-Dusername=#username# -Dpassword=#password#' on commandline to initialize the remote repository correctly");
      }
    } else {
      log.debug("GIT repository and the credentialsprovider are already initialized");
    }
    log.info("GIT-repository initializing finished");
    log.info(FUNCTION_LINE_BREAK);
  }

  /**
   * <p>When {@link RUNMODE} = RELEASE_RPM then determine branchVersion from GIT-branch</p>
   *
   * @return branchVersion
   */
  private String determineBranchVersionFromGitBranch() {
    String value = null;
    log.info(MOJO_LINE_BREAK);
    log.info("Determine current branchVersion from GIT-repository");
    try {
      initializeRepository();
    } catch (Exception err) {
      log.error("Could not initialize GIT-repository", err);
    }

    try {
      String branch = currentGitRepo.getRepository().getBranch();
      log.info("Current branch                    : " + branch);
      if (branch != null && !branch.isEmpty()) {
        if (branch.matches("\\d+.\\d+.\\d+.*")) {
          log.info("Current branch matches            : \\d+.\\d+.\\d+.*");
          value = branch;
        } else if (branch.matches("v\\d+_\\d+_\\d+.*")) {
          log.info("Current branch matches            : v\\d+_\\d+_\\d+.*");
          String rawBranch = branch.replaceAll("v", "").replaceAll("_", ".");
          value = rawBranch.substring(0, StringUtils.ordinalIndexOf(rawBranch, ".", 3));
        } else if (branch.equals("master")) {
          log.info("Current branch matches            : master");
          value = determineVersionFromMasterBranch(branch);
        } else {
          log.error("Current branch does not match        : diget.diget.diget");
          log.error("And current branch does not match    : v+diget.diget.diget+*");
          log.error("And current branch does is not       : master");
          log.error("Branch is not set, semantic versioning for RPM is terminated");
          Runtime.getRuntime().exit(1);
        }
      } else {
        log.error("Current branch is empty or null");
        log.error("Branch is not set, semantic versioning for RPM is terminated");
        Runtime.getRuntime().exit(1);
      }
    } catch (Exception err) {
      log.error("An error occured while trying to reach GIT-repo: ", err);
    }
    log.info("------------------------------------------------------------------------");

    return value;
  }

  private String determineVersionFromMasterBranch(String branch) {
    String branchVersion = "";
    log.info("Setup HttpClient connection to: " + getConfiguration().getBranchConversionUrl() + branch) ;
    CloseableHttpClient httpClient = HttpClientBuilder.create().build();
    CloseableHttpResponse response = null;
    try {
      HttpGet httpGet = new HttpGet(getConfiguration().getBranchConversionUrl() + branch);
      httpGet.addHeader("Content-Type", "application/json");
      response = httpClient.execute(httpGet);
      log.info("Versionizer returned response-code: " + response.getStatusLine());
      HttpEntity entity = response.getEntity();
      branchVersion = EntityUtils.toString(entity);
      if (branchVersion != null) {
        log.info("Versionizer returned branch version: " + branchVersion);
      } else {
        log.error("No branch version could be determined");
      }
    } catch (IOException err) {
      log.error("Could not make request to versionizer", err);
    } finally {
      try {
        if(response != null) {
          response.close();
        }
        if(httpClient != null) {
          httpClient.close();
        }
      } catch (IOException err) {
        log.error("Could not close request to versionizer", err);
      }
    }
    return branchVersion;
  }

  /**
   * @param scmVersion
   * @throws IOException
   * @throws GitAPIException
   */
  protected void cleanupGitLocalAndRemoteTags(String scmVersion) throws SemverException, IOException, GitAPIException {
    log.info("Check for lost-tags");
    log.info(MOJO_LINE_BREAK);
    try {
      initializeRepository();
    } catch (Exception e) {
      log.error("Could not initialize GIT-repository", e);
    }
    if (isRemoteEnabled) {
      currentGitRepo.pull().setCredentialsProvider(credProvider).call();
      List<Ref> refs = currentGitRepo.tagList().call();
      log.debug("Remote tags: " + refs.toString());
      if (refs.isEmpty()) {
        boolean found = false;
        for (Ref ref : refs) {
          if (ref.getName().contains(scmVersion)) {
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
    } else {
      log.warn("Remote is not initialized. Could not delete remote tags");
    }
    currentGitRepo.close();
    log.info(MOJO_LINE_BREAK);
  }

  protected void createReleaseNative(String developmentVersion, String releaseVersion) {
    // TODO:SH Create a native build for test-purposes only. This way we can ditch the release-plugin
  }

  /**
   * @param developmentVersion
   * @param major              semantic major-version to determine release-version and scm-tag version
   * @param minor              semantic minor-version to determine release-version and scm-tag version
   * @param patch              semantic patch-version to determine release-version and scm-tag version
   */
  protected void createReleaseRpm(String developmentVersion, int major, int minor, int patch) {

    log.info("NEW versions on RPM base");

    String releaseVersion = branchVersion + "-" + String.format("%03d%03d%03d", major, minor, patch);

    String buildMetaData = major + "." + minor + "." + patch;
    String scmVersion = releaseVersion + "+" + buildMetaData;

    log.info("New DEVELOPMENT-version               : " + developmentVersion);
    log.info("New RPM GIT build metadata            : " + buildMetaData);
    log.info("New RPM GIT-version                   : " + scmVersion);
    log.info("New RPM RELEASE-version               : " + releaseVersion);
    log.info(MOJO_LINE_BREAK);

    createReleaseProperties(developmentVersion, releaseVersion, scmVersion);
  }

  /**
   * @param developmentVersion needed by the pom to determine next development version
   * @param releaseVersion     releaseVersion is used in the release-pom for the JENKINS-build
   * @param scmVersion         scmVersion is used for tagging the version in GIT
   */
  protected void createReleaseProperties(String developmentVersion, String releaseVersion, String scmVersion) {
    String mavenProjectRelease = "project.rel." + project.getGroupId() + "\\\u003A" + project.getArtifactId() + "\u003D" + releaseVersion;
    String mavenProjectDevelopment = "project.dev." + project.getGroupId() + "\\\u003A" + project.getArtifactId() + "\u003D" + developmentVersion;
    String mavenProjectScm = "scm.tag=" + scmVersion;

    try {
      File releaseProperties = new File("release.properties");
      if (releaseProperties.exists()) {
        log.info("Old release.properties removed    : " + releaseProperties.getAbsolutePath());
        boolean isDeleted = releaseProperties.delete();
        if (!isDeleted) {
          log.error("File: release.properties.xml is not removed");
        }
      }
      FileWriter fileWriter = new FileWriter(releaseProperties);
      StringBuilder releaseText = new StringBuilder();
      releaseText.append(mavenProjectRelease);
      releaseText.append("\n");
      releaseText.append(mavenProjectDevelopment);
      releaseText.append("\n");
      releaseText.append(mavenProjectScm);

      log.info("New release.properties prepared   : " + releaseProperties.getAbsolutePath());

      writeReleaseProperties(fileWriter, releaseText);
      fileWriter.close();
    } catch (IOException err) {
      log.error("Semver plugin is terminating");
      log.error("Error when creating new release.properties", err);
      Runtime.getRuntime().exit(1);
    }


  }

  /**
   * <p>Write actual file to disk</p>
   *
   * @param fileWriter  the fileWriter for release.properties
   * @param releaseText the full content for the release.properties
   */
  private void writeReleaseProperties(FileWriter fileWriter, StringBuilder releaseText) {
    try {
      Writer output = new BufferedWriter(fileWriter);
      output.append(releaseText.toString());
      output.close();
    } catch (IOException err) {
      log.error("Semver plugin is terminating");
      log.error("Error when creating new release.properties", err);
      Runtime.getRuntime().exit(1);
    }
  }

  /**
   * <p></p>
   *
   * @author sido
   */
  public enum VERSION {
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
  public enum RUNMODE {
    RELEASE,
    RELEASE_RPM,
    NATIVE,
    NATIVE_RPM,
    RUNMODE_NOT_SPECIFIED;

    public static RUNMODE convertToEnum(String runMode) {
      RUNMODE value = RUNMODE_NOT_SPECIFIED;
      if (runMode != null) {
        if ("RELEASE".equals(runMode)) {
          value = RELEASE;
        } else if ("RELEASE_RPM".equals(runMode)) {
          value = RELEASE_RPM;
        } else if ("NATIVE".equals(runMode)) {
          value = NATIVE;
        } else if ("NATIVE_RPM".equals(runMode)) {
          value = NATIVE_RPM;
        }
      }
      return value;
    }
  }

}
