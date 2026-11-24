package ledgerly;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Finished. Answers 409. */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateUserException extends RuntimeException {
  public DuplicateUserException(String username) {
    super("Username " + username + " is taken");
  }
}
