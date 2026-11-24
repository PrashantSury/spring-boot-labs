package ledgerly;

/** Unknown, expired, revoked or reused refresh token. Finished. */
public class InvalidRefreshToken extends RuntimeException {
  public InvalidRefreshToken(String reason) {
    super(reason);
  }
}
