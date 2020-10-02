package org.apache.maven.plugins.semver.providers;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.maven.plugins.semver.SemverMavenPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named
@Singleton
public class BranchProviderImpl implements BranchProvider {

  private final Logger logger = LoggerFactory.getLogger(BranchProviderImpl.class);

  private final RepositoryProvider repositoryProvider;

  @Inject
  public BranchProviderImpl(RepositoryProvider repositoryProvider) {
    this.repositoryProvider = requireNonNull(repositoryProvider);
  }

  @Override
  public String determineBranchVersionFromGitBranch(
      String branchVersion, String branchConversionUrl) {
    String value = null;
    if (branchVersion == null || branchVersion.isEmpty()) {
      logger.info(SemverMavenPlugin.MOJO_LINE_BREAK);
      logger.info("Determine current branchVersion from GIT-repository");
      try {
        String branch = "master";
        if (repositoryProvider != null && repositoryProvider.isInitialized()) {
          branch = repositoryProvider.getCurrentBranch();
        }
        logger.info("Current branch                    : [ {} ]", branch);
        if (branch != null && !branch.isEmpty()) {
          if (branch.matches("\\d+.\\d+.\\d+.*")) {
            logger.info("Current branch matches            : \\d+.\\d+.\\d+.*");
            value = branch;
          } else if (branch.matches("v\\d+_\\d+_\\d+.*")) {
            logger.info("Current branch matches            : v\\d+_\\d+_\\d+.*");
            String rawBranch = branch.replaceAll("v", "").replaceAll("_", ".");
            value = rawBranch.substring(0, StringUtils.ordinalIndexOf(rawBranch, ".", 3));
          } else if (branch.equals("master")) {
            logger.info("Current branch matches            : [ master ]");
            value = determineVersionFromMasterBranch(branch, branchConversionUrl);
          } else if (branch.matches("^[a-z0-9]*")) {
            logger.warn("Current branch matches md5-hash   : ^[a-z0-9]");
            logger.warn("Application is running tests");
          } else {
            logger.error("Current branch does not match any known formats");
            logger.error(" * Branch does not match         : [ digit.digit.digit ]");
            logger.error(" * Branch does not match         : [ v+digit.digit.digit+* ]");
            logger.error(" * Branch does is not            : [ master ]");
            logger.error("Branch is not set, semantic versioning for RPM is terminated");
            Runtime.getRuntime().exit(1);
          }
        } else {
          logger.error("Current branch is empty or null");
          logger.error("Branch is not set, semantic versioning for RPM is terminated");
          Runtime.getRuntime().exit(1);
        }
      } catch (Exception err) {
        logger.error("An error occured while trying to reach GIT-repo: ", err);
      }
      logger.info(SemverMavenPlugin.MOJO_LINE_BREAK);
    } else {
      value = branchVersion;
    }
    return value;
  }

  /**
   * <h>Master branch version detemination</h>
   *
   * <p>Which new version is to be determined from the master-branch. This is done by an external
   * service defined in the configuration of the plugin
   *
   * <p>Example:
   *
   * <pre>{@code
   * <configuration>
   *     <branchConversionUrl>http://branchvconversion.com/</branchConversionUrl>
   * </configuration>
   * }</pre>
   *
   * @param branch branch from which a version has te be determined
   * @return masterBranchVersion
   */
  private String determineVersionFromMasterBranch(String branch, String branchConversionUrl) {
    String branchVersion = "";
    logger.info("Setup connection to               : {}{}", branchConversionUrl, branch);
    CloseableHttpClient httpClient = HttpClientBuilder.create().build();
    CloseableHttpResponse response = null;
    try {
      HttpGet httpGet = new HttpGet(branchConversionUrl + branch);
      httpGet.addHeader("Content-Type", "application/json");
      response = httpClient.execute(httpGet);
      logger.info("Conversion-service status         : [ {} ]", response.getStatusLine());
      HttpEntity entity = response.getEntity();
      branchVersion = EntityUtils.toString(entity);
      if (branchVersion != null) {
        logger.info("Conversion-service branch         : [ {} ]", branchVersion);
      } else {
        logger.error("No branch version could be determined");
      }
    } catch (IOException err) {
      logger.error("Could not make request to conversion-service", err);
    } finally {
      try {
        if (response != null) {
          response.close();
        }
        if (httpClient != null) {
          httpClient.close();
        }
      } catch (IOException err) {
        logger.error("Could not close request to conversion-service", err);
      }
    }
    return branchVersion;
  }
}
