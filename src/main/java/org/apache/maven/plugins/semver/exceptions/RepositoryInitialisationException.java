package org.apache.maven.plugins.semver.exceptions;

public class RepositoryInitialisationException extends Exception {
  public RepositoryInitialisationException() {
    super("This is not a valid SCM-repository \nPlease run this goal in a valid SCM-repository");
  }
}
