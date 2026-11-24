package ledgerly;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Checks for chapter 28. asha = ROLE_USER + transfer:write, meera = ROLE_USER,
 * ravi = ROLE_TELLER + report:read, ops = ROLE_ADMIN.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.MethodName.class)
class Chapter28CheckTest {

  @Autowired MockMvc mvc;
  @Autowired TokenService tokens;
  @Autowired UserDetailsService users;

  private String token(String user) {
    return tokens.issueFor(users.loadUserByUsername(user));
  }

  private int status(String user, MockHttpServletRequestBuilder rb) {
    try {
      return mvc.perform(rb.header("Authorization", "Bearer " + token(user))).andReturn().getResponse().getStatus();
    } catch (Exception e) {
      return fail("[request] " + e);
    }
  }

  private MockHttpServletRequestBuilder transfer() {
    return post("/transfers").contentType("application/json").content("{}");
  }

  // ---------- Task 1 ----------

  @Test
  @DisplayName("Task 1: asha's token carries authorities [ROLE_USER, transfer:write]")
  void task1a_claim() {
    Map<String, Object> claims = tokens.read(token("asha"));
    assertThat(claims.get("authorities")).withFailMessage("[task 1: claim] claims are %s", claims)
        .isEqualTo(java.util.List.of("ROLE_USER", "transfer:write"));
  }

  @Test
  @DisplayName("Task 1: ops's token carries [ROLE_ADMIN]")
  void task1b_admin() {
    assertThat(tokens.read(token("ops")).get("authorities")).isEqualTo(java.util.List.of("ROLE_ADMIN"));
  }

  // ---------- Task 2 ----------

  @Test
  @DisplayName("Task 2: GET /me with ravi's token is 'ravi [ROLE_TELLER,report:read]'")
  void task2a_me() throws Exception {
    String body = mvc.perform(get("/me").header("Authorization", "Bearer " + token("ravi"))).andReturn()
        .getResponse().getContentAsString();
    assertThat(body).withFailMessage("[task 2: authorities] /me says %s", body).isEqualTo("ravi [ROLE_TELLER,report:read]");
  }

  @Test
  @DisplayName("Task 2: authorities() of claims without the claim is an empty list")
  void task2b_empty() {
    assertThat(TokenService.authorities(Map.of("sub", "x"))).isEmpty();
    assertThat(TokenService.authorities(Map.of("authorities", java.util.List.of("ROLE_USER")))).containsExactly("ROLE_USER");
  }

  // ---------- Task 3 ----------

  @Test
  @DisplayName("Task 3: POST /transfers: asha (transfer:write) 201, meera (only ROLE_USER) 403")
  void task3a_transfer() {
    assertThat(status("asha", transfer())).isEqualTo(201);
    assertThat(status("meera", transfer())).withFailMessage("[task 3: transfer] meera has no transfer:write").isEqualTo(403);
  }

  @Test
  @DisplayName("Task 3: /reports/daily: ravi (report:read) 200, asha 403")
  void task3b_reports() {
    assertThat(status("ravi", get("/reports/daily"))).isEqualTo(200);
    assertThat(status("asha", get("/reports/daily"))).isEqualTo(403);
  }

  @Test
  @DisplayName("Task 3: /teller/cash-drawer: ravi (TELLER) 200, asha 403; /admin/audit: ravi 403")
  void task3c_roles() {
    assertThat(status("ravi", get("/teller/cash-drawer"))).isEqualTo(200);
    assertThat(status("asha", get("/teller/cash-drawer"))).isEqualTo(403);
    assertThat(status("ravi", get("/admin/audit"))).isEqualTo(403);
  }

  // ---------- Task 4 ----------

  @Test
  @DisplayName("Task 4: ops (ADMIN) opens /teller/cash-drawer through the hierarchy (200)")
  void task4a_hierarchy() {
    assertThat(status("meera", get("/teller/cash-drawer"))).withFailMessage("[task 4: needs task 3] /teller/** must be"
        + " TELLER-only first").isEqualTo(403);
    assertThat(status("ops", get("/teller/cash-drawer"))).withFailMessage("[task 4: hierarchy] ADMIN should include"
        + " TELLER. Add the RoleHierarchy bean").isEqualTo(200);
  }

  @Test
  @DisplayName("Task 4: the hierarchy grants roles, not authorities: ops on /reports/daily is still 403")
  void task4b_onlyRoles() {
    assertThat(status("ops", get("/reports/daily"))).isEqualTo(403);
    assertThat(status("ops", get("/admin/audit"))).isEqualTo(200);
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
  @DisplayName("Task 5: the answer covers the key ideas (role_, hasrole, hasauthority, hierarchy, token)")
  void task5c_keyIdeas() {
    String body = answerBody().toLowerCase();
    java.util.List<String> missing = java.util.Arrays.stream(new String[] {"role_|prefix", "hasrole", "hasauthority", "hierarchy", "token"})
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
