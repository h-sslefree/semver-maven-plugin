package org.apache.maven.plugins.semver.providers;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.semver.SemverMavenPlugin;
import org.apache.maven.plugins.semver.configuration.SemverConfiguration;
import org.apache.maven.plugins.semver.exceptions.SemverException;
import org.apache.maven.project.MavenProject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author sido
 */
public class RepositoryProvider {

    private Log LOG;

    private Git repository;
    private CredentialsProvider provider;

    public RepositoryProvider(Log LOG, MavenProject project, SemverConfiguration configuration) {
        this.LOG = LOG;
        try {
            repository = initializeRepository(project);
            provider = initializeCredentialsProvider(configuration);
        } catch (SemverException err) {
            LOG.error(err.getMessage());
        }
    }

    /**
     * <p>Initialize GIT-repo for determining branch and tag information.</p>
     *
     * @throws SemverException exception for not initializing local and remote repository
     */
    private Git initializeRepository(MavenProject project) throws SemverException {
        Git repository = null;
        LOG.info(SemverMavenPlugin.FUNCTION_LINE_BREAK);
        LOG.info("Initializing GIT-repository");
        FileRepositoryBuilder repoBuilder = new FileRepositoryBuilder();
        repoBuilder.addCeilingDirectory(project.getBasedir());
        repoBuilder.findGitDir(project.getBasedir());
        Repository repo = null;
        try {
            repo = repoBuilder.build();
            repository = new Git(repo);
            LOG.info(" - GIT-repository is initialized");
        } catch (Exception err) {
            LOG.error(" - This is not a valid GIT-repository.");
            LOG.error(" - Please run this goal in a valid GIT-repository");
            LOG.error(" - Could not initialize repostory", err);
            throw new SemverException("This is not a valid GIT-repository", "Please run this goal in a valid GIT-repository");
        }
        LOG.info("GIT-repository initializing finished");
        LOG.info(SemverMavenPlugin.FUNCTION_LINE_BREAK);
        return repository;
    }

    /**
     * <p>Initialize credentialsprovider to acces remote GIT repository.</p>
     *
     * @return credentialsProvider initialized credentialsProvider
     */
    private CredentialsProvider initializeCredentialsProvider(SemverConfiguration configuration) {
        CredentialsProvider provider = null;
        if (!(configuration.getScmPassword().isEmpty() || configuration.getScmUsername().isEmpty())) {
            provider = new UsernamePasswordCredentialsProvider(configuration.getScmUsername(), configuration.getScmPassword());
            LOG.info(" - GIT-credential provider is initialized");
        }
        return provider;
    }

    public boolean pull() {
        boolean isSuccess = true;
        try {
            repository.pull().setCredentialsProvider(provider).call();
        } catch (GitAPIException err) {
            isSuccess = false;
            LOG.error(err.getMessage());
        }
        return isSuccess;
    }

    public boolean pushTag(String tag) {
        boolean isSuccess = true;
        RefSpec refSpec = new RefSpec().setSource(null).setDestination(tag);
        try {
            repository.push().setRemote("origin").setRefSpecs(refSpec).setCredentialsProvider(provider).call();
        } catch (GitAPIException err) {
            isSuccess = false;
            LOG.error(err.getMessage());
        }
        return isSuccess;
    }

    public String getCurrentBranch() {
        String currentBranch = "";
        try {
            currentBranch = repository.getRepository().getBranch();
        } catch (IOException err) {
            LOG.error(err.getMessage());
        }
        return currentBranch;

    }

    public List<Ref> getTags() {
        List<Ref> tags = new ArrayList<Ref>();
        try {
            tags = repository.tagList().call();
        } catch (GitAPIException err) {
            LOG.error(err.getMessage());
        }
        return tags;
    }

    public boolean deleteTag(String tag) {
        boolean isSuccess = true;
        try {
            repository.tagDelete().setTags(tag).call();
        } catch (GitAPIException err) {
            isSuccess = false;
            LOG.error(err.getMessage());
        }
        return isSuccess;
    }

    public void closeRepository() {
        repository.close();
    }

    public boolean checkChanges() {
        boolean isChanged = false;
        LOG.info("Check on local or remote changes");
        LOG.debug("Perform GIT-pull");
        pull();
        try {
            LOG.info("Check on local changes");
            Status status = repository.status().call();
            if(status.getUncommittedChanges().size() > 0) {
                LOG.error("There are uncomitted changes. Please commit and push the open changes.");
                isChanged = true;
                System.exit(0);
            }
        } catch (GitAPIException err) {
            LOG.error(err.getMessage());
            isChanged = true;
            System.exit(0);
        }
        return isChanged;
    }


}
