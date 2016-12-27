package org.apache.maven.plugins.semver.test.runmodes;

import org.apache.commons.logging.Log;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.maven.plugins.semver.test.AbstractSemverMavenPluginTest;
import org.junit.Test;

import java.io.IOException;

import static org.apache.commons.logging.LogFactory.getLog;

/**
 * Created by sido on 27-12-16.
 */
public class SemverMavenPluginBranchConversionUrlTest extends AbstractSemverMavenPluginTest {

  private static final Log log = getLog(SemverMavenPluginBranchConversionUrlTest.class);

  @Test
  public void testBranchConversionUrl() {
    CloseableHttpClient httpClient = null;
    CloseableHttpResponse response = null;
    try {
      httpClient = HttpClients.createDefault();
      HttpGet httpGet = new HttpGet("http://versionizer.bicat.com/v2/convert/branch_to_milestone/master");
      httpGet.addHeader("Content-Type", "application/json");
      response = httpClient.execute(httpGet);
      log.info("Versionizer returned response-code: " + response.getStatusLine());
      String branchVersion = EntityUtils.toString(response.getEntity());
      if (branchVersion != null) {
        log.info("Versionizer returned branch version: " + branchVersion);
      } else {
        log.error("No branch version could be determined");
      }
    } catch (IOException err) {
      log.error("Could not make request to versionizer", err);
    } finally {
      try {
        response.close();
        httpClient.close();
      } catch (IOException err) {
        log.error("Could not close request to versionizer", err);
      }
    }
  }

}


