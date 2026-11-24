package ledgerly;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.time.Duration;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/** Checks for chapter 30. The LabClock lets a test jump forward in time. */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.MethodName.class)
class Chapter30CheckTest {

  @Autowired MockMvc mvc;
  @Autowired TokenService tokens;
  @Autowired RefreshTokenStore store;
  @Autowired LabClock clock;

  @BeforeEach
  void clean() {
    store.clear();
  }

  private static <T> T run(String task, Supplier<T> s) {
    try {
      return s.get();
    } catch (UnsupportedOperationException e) {
      return fail("[" + task + "] " + e.getMessage());
    }
  }

  private String failure(String task, Runnable r) {
    try {
      r.run();
      return fail("[" + task + "] expected InvalidRefreshToken");
    } catch (InvalidRefreshToken e) {
      return e.getMessage();
    } catch (UnsupportedOperationException e) {
      return fail("[" + task + "] " + e.getMessage());
    }
  }

  private int me(String access) throws Exception {
    return mvc.perform(get("/me").header("Authorization", "Bearer " + access)).andReturn().getResponse().getStatus();
  }

  // ---------- Task 1 ----------

  @Test
  @DisplayName("Task 1: issue() gives a 15-minute JWT access token and an opaque refresh token")
  void task1a_pair() throws Exception {
    var pair = run("task 1: issue", () -> tokens.issue("asha"));
    assertThat(pair.expiresIn()).isEqualTo(900);
    assertThat(pair.accessToken().split("\\.")).hasSize(3);
    assertThat(pair.refreshToken()).withFailMessage("[task 1: opaque] refresh should be random Base64URL, not a JWT")
        .doesNotContain(".").matches("[A-Za-z0-9_-]{43}");
    assertThat(me(pair.accessToken())).isEqualTo(200);
  }

  @Test
  @DisplayName("Task 1: the store keeps a SHA-256 hash, never the refresh token itself; 7-day expiry")
  void task1b_hashed() {
    var pair = run("task 1: issue", () -> tokens.issue("asha"));
    assertThat(store.rawRows()).withFailMessage("[task 1: save] nothing stored").hasSize(1);
    assertThat(store.rawRows()).doesNotContainKey(pair.refreshToken()).containsKey(RefreshTokenStore.hash(pair.refreshToken()));
    assertThat(store.find(pair.refreshToken()).orElseThrow().expiresAt())
        .isEqualTo(clock.instant().plus(Duration.ofDays(7)));
  }

  @Test
  @DisplayName("Task 1: two logins give two different refresh tokens")
  void task1c_random() {
    assertThat(run("task 1: issue", () -> tokens.issue("asha")).refreshToken())
        .isNotEqualTo(tokens.issue("asha").refreshToken());
  }

  // ---------- Task 2 ----------

  @Test
  @DisplayName("Task 2: 20 minutes later the access token is 401, but refresh() gives a working new one")
  void task2a_refresh() throws Exception {
    var pair = run("task 1: issue", () -> tokens.issue("asha"));
    clock.advance(Duration.ofMinutes(20));
    assertThat(me(pair.accessToken())).isEqualTo(401);
    var fresh = run("task 2: refresh", () -> tokens.refresh(pair.refreshToken()));
    assertThat(me(fresh.accessToken())).isEqualTo(200);
  }

  @Test
  @DisplayName("Task 2: rotation: the new refresh token differs and the old one is revoked")
  void task2b_rotate() {
    var pair = run("task 1: issue", () -> tokens.issue("asha"));
    var fresh = run("task 2: refresh", () -> tokens.refresh(pair.refreshToken()));
    assertThat(fresh.refreshToken()).isNotEqualTo(pair.refreshToken());
    assertThat(store.find(pair.refreshToken()).orElseThrow().revoked()).withFailMessage("[task 2: rotate] old token"
        + " still active").isTrue();
    assertThat(store.activeFor("asha")).isEqualTo(1);
  }

  @Test
  @DisplayName("Task 2: unknown -> 'unknown'; after 8 days -> 'expired'")
  void task2c_invalid() {
    assertThat(failure("task 2: unknown", () -> tokens.refresh("made-up"))).isEqualTo("unknown");
    var pair = run("task 1: issue", () -> tokens.issue("asha"));
    clock.advance(Duration.ofDays(8));
    assertThat(failure("task 2: expired", () -> tokens.refresh(pair.refreshToken()))).isEqualTo("expired");
  }

  // ---------- Task 3 ----------

  @Test
  @DisplayName("Task 3: replaying a used refresh token -> 'reused', and ALL of asha's refresh tokens die")
  void task3a_reuse() {
    var stolen = run("task 1: issue", () -> tokens.issue("asha"));
    var userKeepsGoing = run("task 2: refresh", () -> tokens.refresh(stolen.refreshToken()));
    assertThat(failure("task 3: reuse", () -> tokens.refresh(stolen.refreshToken()))).isEqualTo("reused");
    assertThat(store.activeFor("asha")).withFailMessage("[task 3: revokeAll] asha still has active tokens").isZero();
    assertThat(failure("task 3: family", () -> tokens.refresh(userKeepsGoing.refreshToken()))).isEqualTo("reused");
  }

  @Test
  @DisplayName("Task 3: reuse by one user does not touch other users")
  void task3b_others() {
    var asha = run("task 1: issue", () -> tokens.issue("asha"));
    tokens.issue("ops");
    tokens.refresh(asha.refreshToken());
    failure("task 3: reuse", () -> tokens.refresh(asha.refreshToken()));
    assertThat(store.activeFor("ops")).isEqualTo(1);
  }

  // ---------- Task 4 ----------

  @Test
  @DisplayName("Task 4: after logout the refresh token is refused, but the access token still works (stateless)")
  void task4a_logout() throws Exception {
    var pair = run("task 1: issue", () -> tokens.issue("asha"));
    run("task 4: logout", () -> {
      tokens.logout(pair.refreshToken());
      return null;
    });
    assertThat(store.activeFor("asha")).withFailMessage("[task 4: logout] token still active").isZero();
    assertThat(me(pair.accessToken())).isEqualTo(200);
  }

  @Test
  @DisplayName("Task 4: HTTP flow: login -> refresh -> logout -> refresh is 401")
  void task4b_http() throws Exception {
    String login = mvc.perform(post("/auth/login").contentType("application/json")
        .content("{\"username\":\"asha\",\"password\":\"asha-pass\"}")).andReturn().getResponse().getContentAsString();
    assertThat(login).withFailMessage("[task 1: login] %s", login).contains("refreshToken");
    String rt = login.replaceAll(".*\"refreshToken\":\"([^\"]+)\".*", "$1");
    String body = "{\"refreshToken\":\"" + rt + "\"}";
    var res = mvc.perform(post("/auth/logout").contentType("application/json").content(body)).andReturn().getResponse();
    assertThat(res.getStatus()).withFailMessage("[task 4: logout] status %s", res.getStatus()).isEqualTo(204);
    assertThat(mvc.perform(post("/auth/refresh").contentType("application/json").content(body)).andReturn()
        .getResponse().getStatus()).isEqualTo(401);
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
  @DisplayName("Task 5: the answer covers the key ideas (refresh, rotat, revok, expir, stateless)")
  void task5c_keyIdeas() {
    String body = answerBody().toLowerCase();
    java.util.List<String> missing = java.util.Arrays.stream(new String[] {"refresh", "rotat", "revok", "expir|short", "stateless|logout"})
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
