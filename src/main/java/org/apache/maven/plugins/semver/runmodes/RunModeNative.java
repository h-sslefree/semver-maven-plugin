package org.apache.maven.plugins.semver.runmodes;

import static java.util.Objects.requireNonNull;
import static org.apache.maven.plugins.semver.factories.FileWriterFactory.removeBackupSemverPom;
import static org.apache.maven.plugins.semver.runmodes.RunMode.checkRemoteRepository;

import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.apache.maven.plugins.semver.configuration.SemverConfiguration;
import org.apache.maven.plugins.semver.factories.FileWriterFactory;
import org.apache.maven.plugins.semver.goals.SemverGoal.SEMVER_GOAL;
import org.apache.maven.plugins.semver.providers.PomProvider;
import org.apache.maven.plugins.semver.providers.RepositoryProvider;
import org.apache.maven.plugins.semver.providers.VersionProvider;
import org.apache.maven.plugins.semver.providers.VersionProvider.FINAL_VERSION;
import org.apache.maven.plugins.semver.providers.VersionProvider.RAW_VERSION;
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
      SEMVER_GOAL semverGoal, SemverConfiguration configuration, String pomVersion) {
    try {
      Map<RAW_VERSION, String> rawVersions =
          versionProvider.determineRawVersions(
              semverGoal,
              configuration.getRunMode(),
              configuration.getBranchVersion(),
              configuration.getMetaData(),
              pomVersion);
      if (configuration.pushTags()) {
        checkRemoteRepository(
            repositoryProvider, versionProvider, configuration, rawVersions.get(RAW_VERSION.SCM));
      }
      FileWriterFactory.backupSemverPom();
      Map<FINAL_VERSION, String> finalVersions =
          versionProvider.determineReleaseVersions(rawVersions);
      pomProvider.createReleasePom(finalVersions);
      pomProvider.createNextDevelopmentPom(finalVersions.get(FINAL_VERSION.DEVELOPMENT));
      removeBackupSemverPom();
    } catch (Exception e) {
      logger.error(e.getMessage());
    }
  }
}
