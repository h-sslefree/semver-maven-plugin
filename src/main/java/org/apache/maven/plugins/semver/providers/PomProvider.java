package org.apache.maven.plugins.semver.providers;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.semver.factories.FileWriterFactory;
import org.apache.maven.project.MavenProject;

import java.util.Map;

/**
 * @author sido
 */
public class PomProvider {

    private Log LOG;

    private RepositoryProvider repositoryProvider;

    private MavenProject project;

    public PomProvider(Log LOG, RepositoryProvider repositoryProvider, MavenProject project) {
        this.LOG = LOG;
        this.repositoryProvider = repositoryProvider;
        this.project = project;
    }

    public boolean createReleasePom(Map<VersionProvider.FINAL_VERSION, String> finalVersions) {
        boolean isNewTagCreated = false;
        MavenProject releasePom = project;
        releasePom.setVersion(finalVersions.get(VersionProvider.FINAL_VERSION.RELEASE));
        FileWriterFactory.writeFileToDisk(LOG, releasePom.getModel().getPomFile());
        String scmTag = finalVersions.get(VersionProvider.FINAL_VERSION.SCM);
        String commitMessage = "[SEMVER] Create new release-pom for tag: [ " + scmTag + " ]";
        LOG.info("Commit local changes              : " + commitMessage);
        repositoryProvider.commit(commitMessage);
        LOG.info("Create local tag                  : " + scmTag);
        repositoryProvider.createTag(scmTag);
        LOG.info("Push local changes and tag        : " + project.getScm().getUrl());
        repositoryProvider.push();
        return isNewTagCreated;
    }

    public boolean createNextDevelopmentPom(String developmentVersion) {
        boolean isNextDevelopmentVersionCreated = false;
        MavenProject nextDevelopementPom = project;
        nextDevelopementPom.setVersion(developmentVersion);
        FileWriterFactory.writeFileToDisk(LOG, nextDevelopementPom.getModel().getPomFile());
        repositoryProvider.commit("[SEMVER] Create next development-pom with version: [ " + developmentVersion + " ]");
        repositoryProvider.push();
        return isNextDevelopmentVersionCreated;
    }

}
