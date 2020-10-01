package org.apache.maven.plugins.semver.test;

import static org.apache.commons.logging.LogFactory.getLog;

import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.maven.plugins.semver.configuration.SemverConfiguration;
import org.junit.Test;

public class BranchConversionUrlTest extends AbstractSemverMavenPluginTest {

  private static final Log LOG = getLog(BranchConversionUrlTest.class);

  @Test
  public void testBranchConversionUrl() {
    SemverConfiguration config = new SemverConfiguration(null);

    CloseableHttpClient httpClient = null;
    CloseableHttpResponse response = null;
    try {
      httpClient = HttpClients.createDefault();
      HttpGet httpGet = new HttpGet(config.getBranchConversionUrl());
      httpGet.addHeader("Content-Type", "application/json");
      response = httpClient.execute(httpGet);
      LOG.info("Versionizer returned response-code: " + response.getStatusLine());
      String branchVersion = EntityUtils.toString(response.getEntity());
      if (branchVersion != null) {
        LOG.info("Versionizer returned branch version: " + branchVersion);
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
  }
}
