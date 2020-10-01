package org.apache.maven.plugins.semver.runmodes;

import java.util.Map;
import org.apache.maven.plugins.semver.configuration.SemverConfiguration;
import org.apache.maven.plugins.semver.factories.FileWriterFactory;
import org.apache.maven.plugins.semver.goals.SemverGoal;
import org.apache.maven.plugins.semver.providers.PomProvider;
import org.apache.maven.plugins.semver.providers.RepositoryProvider;
import org.apache.maven.plugins.semver.providers.VersionProvider;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;

/**
 *
 *
 * <h1>Branch</h1>
 *
 * @author sido
 */
@Component(role = RunModeNativeBranch.class)
public class RunModeNativeBranch implements RunMode {

  @Requirement private Logger LOG;

  @Requirement private PomProvider pomProvider;
  @Requirement private VersionProvider versionProvider;
  @Requirement private RepositoryProvider repositoryProvider;

  @Override
  public void execute(
      SemverGoal.SEMVER_GOAL semverGoal, SemverConfiguration configuration, String pomVersion) {
    try {
      Map<VersionProvider.RAW_VERSION, String> rawVersions =
          versionProvider.determineRawVersions(
              semverGoal,
              configuration.getRunMode(),
              configuration.getBranchVersion(),
              configuration.getMetaData(),
              pomVersion);
      RunMode.checkRemoteRepository(
          repositoryProvider,
          versionProvider,
          configuration,
          rawVersions.get(VersionProvider.RAW_VERSION.SCM));
      FileWriterFactory.backupSemverPom();
      Map<VersionProvider.FINAL_VERSION, String> finalVersions =
          versionProvider.determineReleaseBranchVersions(
              rawVersions,
              configuration.getRunMode(),
              configuration.getMetaData(),
              configuration.getBranchVersion());
      pomProvider.createReleasePom(finalVersions);
      pomProvider.createNextDevelopmentPom(
          finalVersions.get(VersionProvider.FINAL_VERSION.DEVELOPMENT));
      FileWriterFactory.removeBackupSemverPom();
    } catch (Exception e) {
      LOG.error(e.getMessage());
    }
  }
}
