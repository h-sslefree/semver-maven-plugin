package org.apache.maven.plugins.semver.exceptions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RepositoryInitialisationException extends Exception {
  private static final Log logger = LogFactory.getLog(RepositoryInitialisationException.class);

  public RepositoryInitialisationException() {
    logger.error("This is not a valid SCM-repository");
    logger.error("Please run this goal in a valid SCM-repository");
  }
}
