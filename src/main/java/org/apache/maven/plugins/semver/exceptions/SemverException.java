package org.apache.maven.plugins.semver.exceptions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 *
 * @author sido
 */
public class SemverException extends Exception {

  private static final Log log = LogFactory.getLog(SemverException.class);

  /**
   *
   * @param header
   * @param message
   */
  public SemverException(String header, String message) {
    log.error(header);
    log.error(message);
  }

}
