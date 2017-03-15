package org.apache.maven.plugins.semver.factories;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.semver.SemverMavenPlugin;
import org.apache.maven.plugins.semver.providers.RepositoryProvider;
import org.eclipse.jgit.api.Git;

import java.io.IOException;

/**
 * @author sido
 */
public class BranchFactory {


    private BranchFactory() {}


    /**
     * <p>Determine branchVersion from GIT-branch</p>
     *
     * @param LOG
     * @param repositoryProvider
     * @param branchConversionUrl
     * @param branchVersion
     * @return branchVersion
     */
    public static String determineBranchVersionFromGitBranch(Log LOG, RepositoryProvider repositoryProvider, String branchConversionUrl, String branchVersion) {
        String value = null;
        if (branchVersion == null || branchVersion.isEmpty()) {
            LOG.info(SemverMavenPlugin.MOJO_LINE_BREAK);
            LOG.info("Determine current branchVersion from GIT-repository");
            try {
                String branch = repositoryProvider.getCurrentBranch();
                LOG.info("Current branch                    : " + branch);
                if (branch != null && !branch.isEmpty()) {
                    if (branch.matches("\\d+.\\d+.\\d+.*")) {
                        LOG.info("Current branch matches            : \\d+.\\d+.\\d+.*");
                        value = branch;
                    } else if (branch.matches("v\\d+_\\d+_\\d+.*")) {
                        LOG.info("Current branch matches            : v\\d+_\\d+_\\d+.*");
                        String rawBranch = branch.replaceAll("v", "").replaceAll("_", ".");
                        value = rawBranch.substring(0, StringUtils.ordinalIndexOf(rawBranch, ".", 3));
                    } else if (branch.equals("master")) {
                        LOG.info("Current branch matches            : master");
                        value = determineVersionFromMasterBranch(LOG, branchConversionUrl, branch);
                    } else if (branch.matches("^[a-z0-9]*")) {
                        LOG.warn("Current branch matches md5-hash       : ^[a-z0-9]");
                        LOG.warn("Application is running tests");
                    } else {
                        LOG.error("Current branch does not match        : digit.digit.digit");
                        LOG.error("And current branch does not match    : v+digit.digit.digit+*");
                        LOG.error("And current branch does is not       : master");
                        LOG.error("Branch is not set, semantic versioning for RPM is terminated");
                        Runtime.getRuntime().exit(1);
                    }
                } else {
                    LOG.error("Current branch is empty or null");
                    LOG.error("Branch is not set, semantic versioning for RPM is terminated");
                    Runtime.getRuntime().exit(1);
                }
            } catch (Exception err) {
                LOG.error("An error occured while trying to reach GIT-repo: ", err);
            }
            LOG.info("------------------------------------------------------------------------");
        } else {
            value = branchVersion;
        }
        return value;
    }

    /**
     * <p>Which new version is to be determined from the master-branch.</p>
     *
     * @param LOG
     * @param branchConversionUrl
     * @param branch branch
     * @return
     */
    private static String determineVersionFromMasterBranch(Log LOG, String branchConversionUrl, String branch) {
        String branchVersion = "";
        LOG.info("Setup connection to            : " + branchConversionUrl + branch);
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        CloseableHttpResponse response = null;
        try {
            HttpGet httpGet = new HttpGet(branchConversionUrl + branch);
            httpGet.addHeader("Content-Type", "application/json");
            response = httpClient.execute(httpGet);
            LOG.info("Versionizer returned response-code: " + response.getStatusLine());
            HttpEntity entity = response.getEntity();
            branchVersion = EntityUtils.toString(entity);
            if (branchVersion != null) {
                LOG.info("Versionizer returned branchversion: " + branchVersion);
            } else {
                LOG.error("No branch version could be determined");
            }
        } catch (IOException err) {
            LOG.error("Could not make request to versionizer", err);
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
                if (httpClient != null) {
                    httpClient.close();
                }
            } catch (IOException err) {
                LOG.error("Could not close request to versionizer", err);
            }
        }
        return branchVersion;
    }

}
