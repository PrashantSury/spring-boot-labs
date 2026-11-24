package ledgerly;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/** Checks for chapter 26. Plain JUnit, no Spring: a fixed clock and a fixed secret. */
@TestMethodOrder(MethodOrderer.MethodName.class)
class Chapter26CheckTest {

  static final byte[] SECRET = "ledgerly-demo-secret-32-bytes!!!".getBytes(StandardCharsets.UTF_8);
  static final Clock NOW = Clock.fixed(Instant.parse("2026-11-24T10:00:00Z"), ZoneOffset.UTC);
  /** The well-known example token from jwt.io, signed with the secret "your-256-bit-secret". */
  static final String JWT_IO = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

  private final JwtCodec codec = new JwtCodec(SECRET, NOW);

  private static <T> T run(String task, java.util.function.Supplier<T> s) {
    try {
      return s.get();
    } catch (UnsupportedOperationException e) {
      return fail("[task " + task + "] still the stub: " + e.getMessage());
    }
  }

  private static String b64(String s) {
    return JwtCodec.b64(s.getBytes(StandardCharsets.UTF_8));
  }

  // ---------- Task 1 ----------

  @Test
  @DisplayName("Task 1: sign({sub: asha}) starts with the standard HS256 header eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.")
  void task1a_header() {
    Map<String, Object> claims = new LinkedHashMap<>(Map.of("sub", "asha"));
    String token = run("1", () -> codec.sign(claims));
    assertThat(token.split("\\.")).hasSize(3);
    assertThat(token).startsWith("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.");
  }

  @Test
  @DisplayName("Task 1: the payload part is base64url of {\"sub\":\"asha\"} and the signature is HMAC-SHA256 of header.payload")
  void task1b_parts() {
    String token = run("1", () -> codec.sign(new LinkedHashMap<>(Map.of("sub", "asha"))));
    String[] p = token.split("\\.");
    assertThat(p[1]).isEqualTo(b64("{\"sub\":\"asha\"}"));
    assertThat(p[2]).isEqualTo(JwtCodec.b64(codec.hmac(p[0] + "." + p[1])));
    assertThat(token).doesNotContain("=");
  }

  @Test
  @DisplayName("Task 1: signing the jwt.io claims with 'your-256-bit-secret' gives exactly the jwt.io token")
  void task1c_jwtIo() {
    JwtCodec io = new JwtCodec("your-256-bit-secret".getBytes(StandardCharsets.UTF_8), NOW);
    Map<String, Object> claims = new LinkedHashMap<>();
    claims.put("sub", "1234567890");
    claims.put("name", "John Doe");
    claims.put("iat", 1516239022);
    assertThat(run("1", () -> io.sign(claims))).isEqualTo(JWT_IO);
  }

  // ---------- Task 2 ----------

  @Test
  @DisplayName("Task 2: the jwt.io token is valid with 'your-256-bit-secret' and NOT valid with Ledgerly's secret")
  void task2a_valid() {
    assertThat(new JwtCodec("your-256-bit-secret".getBytes(StandardCharsets.UTF_8), NOW).signatureValid(JWT_IO)).isTrue();
    assertThat(codec.signatureValid(JWT_IO)).withFailMessage("[task 2: wrong secret] a token signed with another secret"
        + " was accepted").isFalse();
  }

  @Test
  @DisplayName("Task 2: changing one character of the payload (sub asha -> ops) breaks the signature")
  void task2b_tampered() {
    String token = run("1", () -> codec.issue("asha", 900));
    String[] p = token.split("\\.");
    String forged = p[0] + "." + b64("{\"sub\":\"ops\",\"iat\":1795600800,\"exp\":1795601700}") + "." + p[2];
    assertThat(codec.signatureValid(forged)).withFailMessage("[task 2: tampered] a changed payload was accepted").isFalse();
    assertThat(codec.signatureValid("abc.def")).isFalse();
  }

  // ---------- Task 3 ----------

  @Test
  @DisplayName("Task 3: a fresh 15-minute token for asha verifies and returns sub = asha")
  void task3a_ok() {
    String token = run("1", () -> codec.issue("asha", 900));
    assertThat(codec.verify(token)).containsEntry("sub", "asha");
  }

  @Test
  @DisplayName("Task 3: 20 minutes later the same token is rejected: 'expired'")
  void task3b_expired() {
    String token = run("1", () -> codec.issue("asha", 900));
    JwtCodec later = new JwtCodec(SECRET, Clock.offset(NOW, Duration.ofMinutes(20)));
    Throwable t = catchThrowable(() -> later.verify(token));
    assertThat(t).withFailMessage("[task 3: exp] an expired token was accepted").isInstanceOf(JwtException.class)
        .hasMessage("expired");
  }

  @Test
  @DisplayName("Task 3: header {\"alg\":\"none\"} with an empty signature is rejected: 'unsupported alg'")
  void task3c_none() {
    String token = run("1", () -> codec.issue("asha", 900));
    String none = b64("{\"alg\":\"none\"}") + "." + token.split("\\.")[1] + ".";
    Throwable t = catchThrowable(() -> codec.verify(none));
    assertThat(t).withFailMessage("[task 3: none] an unsigned token was accepted").isInstanceOf(JwtException.class)
        .hasMessage("unsupported alg");
  }

  @Test
  @DisplayName("Task 3: a tampered payload is rejected: 'bad signature'; a token without exp: 'expired'")
  void task3d_badSig() {
    String token = run("1", () -> codec.issue("asha", 900));
    String[] p = token.split("\\.");
    String forged = p[0] + "." + b64("{\"sub\":\"ops\",\"exp\":9999999999}") + "." + p[2];
    assertThat(catchThrowable(() -> codec.verify(forged))).isInstanceOf(JwtException.class).hasMessage("bad signature");
    String noExp = run("1", () -> codec.sign(new LinkedHashMap<>(Map.of("sub", "asha"))));
    assertThat(catchThrowable(() -> codec.verify(noExp))).isInstanceOf(JwtException.class).hasMessage("expired");
  }

  // ---------- Task 4 ----------

  @Test
  @DisplayName("Task 4: readUnverified(jwt.io token) shows name = John Doe without knowing the secret")
  void task4a_readable() {
    assertThat(run("4", () -> codec.readUnverified(JWT_IO))).containsEntry("name", "John Doe").containsEntry("sub", "1234567890");
  }

  // ---------- Task 5 (Interview) ----------

  @Test
  @DisplayName("Task 5: interview-answer.md has your answer, not the TODO line")
  void task5a_answered() {
    assertThat(answer()).withFailMessage("[task 5: interview answer] interview-answer.md still has the TODO line")
        .doesNotContain("TODO: write your answer here");
  }

  @Test
  @DisplayName("Task 5: the answer is at least 40 words")
  void task5b_longEnough() {
    int words = answerBody().isBlank() ? 0 : answerBody().split("\\s+").length;
    assertThat(words).withFailMessage("[task 5: length] your answer has %d words. Write at least 40", words)
        .isGreaterThanOrEqualTo(40);
  }

  @Test
  @DisplayName("Task 5: the answer covers the key ideas (header, payload, signature, signed, exp, none)")
  void task5c_keyIdeas() {
    String body = answerBody().toLowerCase();
    java.util.List<String> missing = java.util.Arrays.stream(new String[] {"header", "payload", "signature", "signed|not encrypted", "exp", "none"})
        .filter(w -> java.util.Arrays.stream(w.split("\\|")).noneMatch(body::contains)).toList();
    assertThat(missing).withFailMessage("[task 5: key ideas] your answer should also mention: %s", missing).isEmpty();
  }

  private static String answer() {
    java.nio.file.Path file = java.nio.file.Path.of(System.getProperty("user.dir")).resolve("interview-answer.md");
    try {
      return java.nio.file.Files.readString(file);
    } catch (java.io.IOException e) {
      return org.junit.jupiter.api.Assertions.fail("[task 5] cannot read " + file.toAbsolutePath());
    }
  }

  private static String answerBody() {
    String text = answer();
    String marker = "Delete the line that says TODO.";
    int i = text.indexOf(marker);
    String body = i >= 0 ? text.substring(i + marker.length()) : text;
    return body.replace("TODO: write your answer here", "").trim();
  }
}
