package ledgerly;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** A single transfer above 50,000 is refused. Finished. */
@ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
public class LimitExceeded extends RuntimeException {

  public LimitExceeded() {
    super("transfer limit is 50000");
  }
}
