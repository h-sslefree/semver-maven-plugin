package org.apache.maven.plugins.semver.goals;

/**
 *
 * <h1>SemverGaol</h1>
 *
 *
 * @author sido
 */
public class SemverGoal {

    public enum SEMVER_GOAL {
        MAJOR("MAJOR"),
        MINOR("MINOR"),
        PATCH("PATCH"),
        ROLLBACK("ROLLBACK");

        private String description = "";

        private SEMVER_GOAL(String description) {
            this.description = description;
        }

        public String getDescription() {
            return this.description;
        }
    }

    private SemverGoal() {}


}
