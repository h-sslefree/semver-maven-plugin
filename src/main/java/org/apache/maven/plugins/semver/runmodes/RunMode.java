package org.apache.maven.plugins.semver.runmodes;

import org.apache.maven.plugins.semver.configuration.SemverConfiguration;
import org.apache.maven.plugins.semver.goals.SemverGoals;
import org.apache.maven.plugins.semver.providers.RepositoryProvider;
import org.apache.maven.plugins.semver.providers.VersionProvider;

/**
 * <h1></h1>
 *
 * @author sido
 */
public interface RunMode {

    /**
     * <ul>
     * <li>RELEASE: uses basic semantic-versioning and the release-plugin to create a tag</li>
     * <li>RELEASE_BRANCH: uses semantic-versioning with branch-prefix and the release-plugin to create a tag</li>
     * <li>RELEASE_BRANCH_RPM: uses semantic-versioning with branch-prefix and a parsed variant of the semantic-versioning as a release-number for RPM. It also uses the release-plugin to create a tag</li>
     * <li>NATIVE (default): uses basic semantic-versioning to create a tag</li>
     * <li>NATIVE_BRANCH: uses semantic-versioning with branch-prefix to create a tag</li>
     * <li>NATIVE_BRANCH_RPM: uses semantic-versioning with branch-prefix and a parsed variant of the semantic-versioning as a release-number for RPM</li>
     * </ul>
     */
    enum RUNMODE {
        RELEASE,
        RELEASE_BRANCH,
        RELEASE_BRANCH_RPM,
        NATIVE,
        NATIVE_BRANCH,
        NATIVE_BRANCH_RPM,
        RUNMODE_NOT_SPECIFIED;

        public static RUNMODE convertToEnum(String runMode) {
            RUNMODE value = RUNMODE_NOT_SPECIFIED;
            if (runMode != null) {
                if ("RELEASE".equals(runMode)) {
                    value = RELEASE;
                } else if ("RELEASE_BRANCH".equals(runMode)) {
                    value = RELEASE_BRANCH;
                } else if ("RELEASE_BRANCH_RPM".equals(runMode)) {
                    value = RELEASE_BRANCH_RPM;
                } else if ("NATIVE".equals(runMode)) {
                    value = NATIVE;
                } else if ("NATIVE_BRANCH".equals(runMode)) {
                    value = NATIVE_BRANCH;
                } else if ("NATIVE_BRANCH_RPM".equals(runMode)) {
                    value = NATIVE_BRANCH_RPM;
                }
            }
            return value;
        }
    }

    static void checkRemoteVersionTags(RepositoryProvider provider, SemverConfiguration configuration, String scmTag) {
        if (configuration.checkRemoteVersionTags()) {
            if (provider.isRemoteVersionCorrupt(scmTag)) {
                Runtime.getRuntime().exit(1);
            }
        }
    }

    /**
     * @param semverGoal
     * @param configuration
     * @param pomVersion
     */
    void execute(SemverGoals.SEMVER_GOAL semverGoal, SemverConfiguration configuration, String pomVersion);
}
