package org.apache.maven.plugins.semver.runmodes;

import static java.util.Objects.requireNonNull;
import static org.apache.maven.plugins.semver.factories.FileWriterFactory.createReleaseProperties;
import static org.apache.maven.plugins.semver.runmodes.RunMode.checkRemoteRepository;

import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.apache.maven.plugins.semver.configuration.SemverConfiguration;
import org.apache.maven.plugins.semver.goals.SemverGoal.SEMVER_GOAL;
import org.apache.maven.plugins.semver.providers.RepositoryProvider;
import org.apache.maven.plugins.semver.providers.VersionProvider;
import org.apache.maven.plugins.semver.providers.VersionProvider.FINAL_VERSION;
import org.apache.maven.plugins.semver.providers.VersionProvider.RAW_VERSION;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named
@Singleton
public class RunModeRelease implements RunMode {
  private final Logger logger = LoggerFactory.getLogger(RunModeRelease.class);

  private final MavenProject mavenProject;
  private final VersionProvider versionProvider;
  private final RepositoryProvider repositoryProvider;

  @Inject
  public RunModeRelease(
      MavenProject mavenProject,
      VersionProvider versionProvider,
      RepositoryProvider repositoryProvider) {
    this.mavenProject = requireNonNull(mavenProject);
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
      Map<FINAL_VERSION, String> finalVersions =
          versionProvider.determineReleaseVersions(rawVersions);
      createReleaseProperties(mavenProject, finalVersions);
    } catch (Exception e) {
      logger.error(e.getMessage());
    }
  }
}
