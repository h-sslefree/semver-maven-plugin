package org.apache.maven.plugins.semver.runmodes;

import org.apache.maven.plugins.semver.SemverMavenPlugin;
import org.apache.maven.plugins.semver.configuration.SemverConfiguration;

import java.util.Map;

/**
 * @author sido
 */
public interface RunMode {

    /**
     * <ul>
     * <li>release: maak gebruik van normale semantic-versioning en release-plugin</li>
     * <li>release-rpm</li>
     * <li>native</li>
     * <li>native-rpm</li>
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

    void execute(SemverConfiguration configuration, Map<SemverMavenPlugin.RAW_VERSION, String> rawVersions);
}
