package org.apache.maven.plugins.semver.exceptions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * <p>Logs the error explicit in Log4J config and describes the SemverException.</p>
 *
 * @author sido
 */
public class SemverException extends Exception {

  private static final Log LOG = LogFactory.getLog(SemverException.class);

  /**
   *
   * <h>Constructor to initialize SemverException</h>
   *
   * @param header header of the error-messages
   * @param message body of the error-message
   */
  public SemverException(String header, String message) {
    LOG.error(header);
    LOG.error(message);
  }

}
