package ledgerly;

import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/** Issues access tokens. */
@Service
public class TokenService {

  private final JwtCodec codec;
  private final Clock clock;
  private final long ttl;

  public TokenService(JwtCodec codec, Clock clock, @Value("${ledgerly.jwt.ttl-seconds}") long ttl) {
    this.codec = codec;
    this.clock = clock;
    this.ttl = ttl;
  }

  // Task 1: put the user's authorities INTO the token as a claim "authorities": a JSON list of
  //   strings, e.g. ["ROLE_USER","transfer:write"]. Then the filter needs no user lookup per request.
  public String issueFor(UserDetails user) {
    long now = clock.instant().getEpochSecond();
    Map<String, Object> claims = new LinkedHashMap<>();
    claims.put("sub", user.getUsername());
    claims.put("iat", now);
    claims.put("exp", now + ttl);
    return codec.sign(claims);
  }

  /** Finished: the claims of a valid token (throws JwtException otherwise). */
  public Map<String, Object> read(String token) {
    return codec.verify(token);
  }

  // Task 2 (part 1): turn the "authorities" claim back into strings; missing claim -> empty list.
  @SuppressWarnings("unchecked")
  public static java.util.List<String> authorities(Map<String, Object> claims) {
    return java.util.List.of();
  }
}
