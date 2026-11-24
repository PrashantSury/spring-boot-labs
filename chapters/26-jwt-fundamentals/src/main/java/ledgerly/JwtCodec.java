package ledgerly;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * A tiny HS256 JWT implementation, written by hand so you see every step. (Chapter 27 uses a library.)
 * A JWT is three base64url parts joined by dots: header.payload.signature
 *   header    {"alg":"HS256","typ":"JWT"}
 *   payload   the claims, e.g. {"sub":"asha","iat":1795000000,"exp":1795000900}
 *   signature HMAC-SHA256(secret, header + "." + payload)
 */
public class JwtCodec {

  static final String HEADER_JSON = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
  private static final JsonMapper JSON = JsonMapper.builder().build();
  private static final Base64.Encoder B64 = Base64.getUrlEncoder().withoutPadding();
  private static final Base64.Decoder B64D = Base64.getUrlDecoder();

  private final byte[] secret;
  private final Clock clock;

  public JwtCodec(byte[] secret, Clock clock) {
    this.secret = secret;
    this.clock = clock;
  }

  /** Finished helper: HMAC-SHA256 of the text with the secret. */
  byte[] hmac(String text) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret, "HmacSHA256"));
      return mac.doFinal(text.getBytes(StandardCharsets.UTF_8));
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  /** Finished helper: base64url without padding (what JWTs use). */
  static String b64(byte[] bytes) {
    return B64.encodeToString(bytes);
  }

  // Task 1: sign. Build header.payload.signature:
  //   header  = b64(HEADER_JSON bytes)
  //   payload = b64(JSON of the claims)       JSON.writeValueAsString(claims)
  //   signature = b64(hmac(header + "." + payload))
  public String sign(Map<String, Object> claims) {
    throw new UnsupportedOperationException("Task 1: header.payload.signature");
  }

  /** Finished: a login token for a customer: sub, iat (now) and exp (now + ttl), in epoch seconds. */
  public String issue(String subject, long ttlSeconds) {
    long now = clock.instant().getEpochSecond();
    Map<String, Object> claims = new LinkedHashMap<>();
    claims.put("sub", subject);
    claims.put("iat", now);
    claims.put("exp", now + ttlSeconds);
    return sign(claims);
  }

  // Task 2: is the signature right? Recompute hmac(parts[0] + "." + parts[1]) and compare it with
  //   the decoded third part using MessageDigest.isEqual (constant time: equals() leaks timing).
  //   Not exactly 3 parts -> false.
  public boolean signatureValid(String token) {
    return true;
  }

  // Task 3: full verification. Throw JwtException with these messages, in this order:
  //   header alg is not HS256 (e.g. "none")      -> "unsupported alg"   (check BEFORE the signature)
  //   signature wrong                            -> "bad signature"
  //   no exp, or exp <= now (clock, epoch secs)  -> "expired"
  //   otherwise return the claims.
  public Map<String, Object> verify(String token) {
    return readUnverified(token);
  }

  // Task 4: anyone can read a JWT. Decode the payload (second part) and parse the JSON, WITHOUT
  //   checking anything. (This is why a JWT must never carry a password or a card number.)
  public Map<String, Object> readUnverified(String token) {
    throw new UnsupportedOperationException("Task 4: base64url-decode part 1 and parse the JSON");
  }

  /** Finished helper: decode part i of the token as a JSON object. */
  static Map<String, Object> part(String token, int i) {
    String[] parts = token.split("\\.", -1);
    if (parts.length < 2) {
      throw new JwtException("malformed");
    }
    try {
      return JSON.readValue(B64D.decode(parts[i]), new TypeReference<LinkedHashMap<String, Object>>() {});
    } catch (RuntimeException e) {
      throw new JwtException("malformed");
    }
  }
}
