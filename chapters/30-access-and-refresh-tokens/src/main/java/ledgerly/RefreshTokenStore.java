package ledgerly;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * The "refresh_tokens table" (in memory here). It stores a SHA-256 HASH of each token, never the token:
 * a leaked table must not hand out working tokens. Finished; NOT one of your tasks.
 */
@Component
public class RefreshTokenStore {

  /** One row. revoked = used (rotated), logged out, or killed by reuse detection. */
  public record Row(String username, Instant expiresAt, boolean revoked) {}

  private final Map<String, Row> rows = new ConcurrentHashMap<>();

  public static String hash(String token) {
    try {
      return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  public void save(String token, String username, Instant expiresAt) {
    rows.put(hash(token), new Row(username, expiresAt, false));
  }

  public Optional<Row> find(String token) {
    return Optional.ofNullable(rows.get(hash(token)));
  }

  public void revoke(String token) {
    rows.computeIfPresent(hash(token), (k, r) -> new Row(r.username(), r.expiresAt(), true));
  }

  /** Revoke every refresh token of one user ("log out everywhere"). */
  public void revokeAll(String username) {
    rows.replaceAll((k, r) -> r.username().equals(username) ? new Row(r.username(), r.expiresAt(), true) : r);
  }

  public long activeFor(String username) {
    return rows.values().stream().filter(r -> r.username().equals(username) && !r.revoked()).count();
  }

  public Map<String, Row> rawRows() {
    return Map.copyOf(rows);
  }

  public void clear() {
    rows.clear();
  }
}
