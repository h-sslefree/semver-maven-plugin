package org.apache.maven.plugins.semver.exceptions;

/**
 *
 *
 * <h1></h1>
 *
 * @author sido
 */
public class SemverExceptionMessages {

  public static final String MESSAGE_ERROR_SCM_CREDENTIALS =
      "Please check your SCM-credentials to fix this issue";
  public static final String MESSAGE_ERROR_PERFORM_ROLLBACK =
      "Please run semver:rollback to return to initial state";

  private SemverExceptionMessages() {}
}
