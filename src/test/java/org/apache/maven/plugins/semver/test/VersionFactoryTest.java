package org.apache.maven.plugins.semver.test;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.semver.factories.VersionFactory;
import org.junit.Test;

/**
 *
 *
 *
 * @author sido
 */
public class VersionFactoryTest extends AbstractSemverMavenPluginTest {

    private final Log LOG = null;

    @Test
    public void createReleaseBranchTest() {
        VersionFactory.createReleaseBranch(LOG, null, null, "", 0, 0,0 );
    }


}



