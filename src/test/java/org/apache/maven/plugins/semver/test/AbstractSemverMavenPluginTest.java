package org.apache.maven.plugins.semver.test;

import java.io.File;
import org.junit.Rule;

/**
 * When running the tests the project needs to be build The plugin configuration will not be loaded
 */
public class AbstractSemverMavenPluginTest {

  @Rule public MojoRule mojoRule = new MojoRule();

  protected File loadPom(String targetPom) {
    return new File("src/test/resources/org/apache/maven/plugins/semver/goals/" + targetPom);
  }
}
