package ledgerly;

/** A token was rejected. The message says why. Finished. */
public class JwtException extends RuntimeException {
  public JwtException(String reason) {
    super(reason);
  }
}
