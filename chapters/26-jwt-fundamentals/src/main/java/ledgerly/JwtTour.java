package ledgerly;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;

/** A finished demo you can run. NOT one of your tasks. See "Try it" in the README. */
public class JwtTour {

  public static void main(String[] args) {
    byte[] secret = "ledgerly-demo-secret-32-bytes!!!".getBytes(StandardCharsets.UTF_8);
    Clock at1000 = Clock.fixed(Instant.parse("2026-11-24T10:00:00Z"), ZoneOffset.UTC);
    JwtCodec codec = new JwtCodec(secret, at1000);

    String token = codec.issue("asha", 900);
    System.out.println("token      " + token);
    String[] p = token.split("\\.");
    System.out.println("header     " + new String(Base64.getUrlDecoder().decode(p[0]), StandardCharsets.UTF_8));
    System.out.println("payload    " + new String(Base64.getUrlDecoder().decode(p[1]), StandardCharsets.UTF_8));
    System.out.println("verify     " + codec.verify(token));

    String tampered = p[0] + "." + JwtCodec.b64("{\"sub\":\"ops\",\"iat\":1795600800,\"exp\":1795601700}"
        .getBytes(StandardCharsets.UTF_8)) + "." + p[2];
    try2("sub changed to ops", codec, tampered);
    String none = JwtCodec.b64("{\"alg\":\"none\"}".getBytes(StandardCharsets.UTF_8)) + "." + p[1] + ".";
    try2("alg none", codec, none);
    JwtCodec at1020 = new JwtCodec(secret, Clock.offset(at1000, java.time.Duration.ofMinutes(20)));
    try2("20 min later", at1020, token);
  }

  private static void try2(String label, JwtCodec codec, String token) {
    try {
      System.out.println(label + " -> accepted?! " + codec.verify(token));
    } catch (JwtException e) {
      System.out.println(label + " -> rejected: " + e.getMessage());
    }
  }
}
