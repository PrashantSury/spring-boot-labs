package ledgerly;

import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/** Issues access tokens with an "authorities" claim (chapter 28). Finished. */
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

  public String issueFor(UserDetails user) {
    long now = clock.instant().getEpochSecond();
    Map<String, Object> claims = new LinkedHashMap<>();
    claims.put("sub", user.getUsername());
    claims.put("authorities", user.getAuthorities().stream().map(GrantedAuthority::getAuthority).sorted().toList());
    claims.put("iat", now);
    claims.put("exp", now + ttl);
    return codec.sign(claims);
  }

  /** Finished: the claims of a valid token (throws JwtException otherwise). */
  public Map<String, Object> read(String token) {
    return codec.verify(token);
  }

  @SuppressWarnings("unchecked")
  public static java.util.List<String> authorities(Map<String, Object> claims) {
    Object a = claims.get("authorities");
    return a instanceof List<?> list ? (List<String>) list : List.of();
  }
}
