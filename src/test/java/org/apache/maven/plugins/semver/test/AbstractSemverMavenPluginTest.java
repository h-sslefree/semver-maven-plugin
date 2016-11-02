package org.apache.maven.plugins.semver.test;

import java.io.File;

import org.apache.maven.plugin.testing.MojoRule;
import org.junit.Rule;

/**
 * 
 * <p>
 * Bij het runnen van de tests moet eerst het project met maven zijn gebuild
 * </p>
 * LETOP de configuratie van de plugin in test wordt niet geladen!!!!!
 * 
 * 
 * @author sido
 *
 */
public class AbstractSemverMavenPluginTest {

	@Rule
	public MojoRule mojoRule = new MojoRule();

	protected File loadPom(String targetPom) {
		return new File("src/test/resources/org/apache/maven/plugins/semver/" + targetPom);
	}
}