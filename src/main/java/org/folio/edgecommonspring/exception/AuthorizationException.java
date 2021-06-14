package org.folio.edgecommonspring.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Specific exception for handlig edge-authorization process
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class AuthorizationException extends RuntimeException {

  public AuthorizationException(String message) {
    super(message);
  }
}
