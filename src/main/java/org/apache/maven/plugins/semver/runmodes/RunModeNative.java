package org.apache.maven.plugins.semver.runmodes;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.apache.maven.plugins.semver.configuration.SemverConfiguration;
import org.apache.maven.plugins.semver.factories.FileWriterFactory;
import org.apache.maven.plugins.semver.goals.SemverGoal;
import org.apache.maven.plugins.semver.providers.PomProvider;
import org.apache.maven.plugins.semver.providers.RepositoryProvider;
import org.apache.maven.plugins.semver.providers.VersionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named
@Singleton
public class RunModeNative implements RunMode {

  private final Logger logger = LoggerFactory.getLogger(RunModeNative.class);

  private final PomProvider pomProvider;
  private final VersionProvider versionProvider;
  private final RepositoryProvider repositoryProvider;

  @Inject
  public RunModeNative(
      PomProvider pomProvider,
      VersionProvider versionProvider,
      RepositoryProvider repositoryProvider) {
    this.pomProvider = requireNonNull(pomProvider);
    this.versionProvider = requireNonNull(versionProvider);
    this.repositoryProvider = requireNonNull(repositoryProvider);
  }

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
          versionProvider.determineReleaseVersions(rawVersions);
      pomProvider.createReleasePom(finalVersions);
      pomProvider.createNextDevelopmentPom(
          finalVersions.get(VersionProvider.FINAL_VERSION.DEVELOPMENT));
      FileWriterFactory.removeBackupSemverPom();
    } catch (Exception e) {
      logger.error(e.getMessage());
    }
  }
}
