package ledgerly;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * The HS256 codec you wrote in chapter 26. Finished here; NOT one of your tasks.
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

  public String sign(Map<String, Object> claims) {
    String header = b64(HEADER_JSON.getBytes(StandardCharsets.UTF_8));
    String payload = b64(JSON.writeValueAsString(claims).getBytes(StandardCharsets.UTF_8));
    return header + "." + payload + "." + b64(hmac(header + "." + payload));
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

  public boolean signatureValid(String token) {
    String[] parts = token.split("\\.", -1);
    if (parts.length != 3) {
      return false;
    }
    try {
      return MessageDigest.isEqual(hmac(parts[0] + "." + parts[1]), B64D.decode(parts[2]));
    } catch (IllegalArgumentException badBase64) {
      return false;
    }
  }

  public Map<String, Object> verify(String token) {
    Map<String, Object> header = part(token, 0);
    if (!"HS256".equals(header.get("alg"))) {
      throw new JwtException("unsupported alg");
    }
    if (!signatureValid(token)) {
      throw new JwtException("bad signature");
    }
    Map<String, Object> claims = part(token, 1);
    Object exp = claims.get("exp");
    if (!(exp instanceof Number n) || n.longValue() <= clock.instant().getEpochSecond()) {
      throw new JwtException("expired");
    }
    return claims;
  }

  public Map<String, Object> readUnverified(String token) {
    return part(token, 1);
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
