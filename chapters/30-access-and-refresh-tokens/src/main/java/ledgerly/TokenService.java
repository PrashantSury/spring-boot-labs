package ledgerly;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Access tokens (short JWTs) and refresh tokens (long, random, stored, rotated). */
@Service
public class TokenService {

  /** What /auth/login and /auth/refresh return. */
  public record TokenPair(String accessToken, String refreshToken, long expiresIn) {}

  private final JwtCodec codec;
  private final LabClock clock;
  private final RefreshTokenStore store;
  private final long accessTtl;
  private final long refreshTtl;

  public TokenService(JwtCodec codec, LabClock clock, RefreshTokenStore store,
      @Value("${ledgerly.jwt.access-ttl-seconds}") long accessTtl,
      @Value("${ledgerly.jwt.refresh-ttl-seconds}") long refreshTtl) {
    this.codec = codec;
    this.clock = clock;
    this.store = store;
    this.accessTtl = accessTtl;
    this.refreshTtl = refreshTtl;
  }

  // Task 1: issue a pair for a user who just logged in.
  //   access  = codec.issue(username, accessTtl)                         (15 min JWT)
  //   refresh = 32 random bytes from SecureRandom, Base64 URL-safe without padding (NOT a JWT)
  //   store.save(refresh, username, now + refreshTtl); return new TokenPair(access, refresh, accessTtl)
  public TokenPair issue(String username) {
    throw new UnsupportedOperationException("Task 1: TokenService.issue");
  }

  // Task 2: exchange a refresh token for a NEW pair, and ROTATE: the old refresh token is revoked.
  //   unknown -> InvalidRefreshToken("unknown"); expired -> InvalidRefreshToken("expired")
  //   (Task 3 adds the revoked case.)  Then store.revoke(old) and return issue(row.username()).
  // Task 3: REUSE DETECTION. A revoked token presented again means someone holds a stolen copy.
  //   -> store.revokeAll(row.username()) (kills the thief's AND the user's tokens),
  //      then throw InvalidRefreshToken("reused"). Check this BEFORE expiry.
  public TokenPair refresh(String refreshToken) {
    throw new UnsupportedOperationException("Task 2: TokenService.refresh");
  }

  // Task 4: logout = revoke this refresh token (unknown tokens: do nothing, still succeed).
  //   The access token is NOT revoked: it keeps working until it expires. That is the stateless trade-off.
  public void logout(String refreshToken) {
    throw new UnsupportedOperationException("Task 4: TokenService.logout");
  }
}
