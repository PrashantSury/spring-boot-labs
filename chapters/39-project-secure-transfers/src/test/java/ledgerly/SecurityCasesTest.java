package ledgerly;

import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Task 4: YOUR security tests. Test the negative cases as hard as the happy path.
 * Replace each fail(...) with a real test using the helpers below. Each test must pass on your finished code.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityCasesTest {

  @Autowired MockMvc mvc;
  @Autowired DemoData demo;

  @BeforeEach
  void reset() {
    demo.reset();
  }

  // ---------- helpers (finished) ----------

  MockHttpServletResponse call(MockHttpServletRequestBuilder rb) {
    try {
      return mvc.perform(rb).andReturn().getResponse();
    } catch (Exception e) {
      return fail("[request] " + e);
    }
  }

  String body(MockHttpServletResponse r) {
    try {
      return r.getContentAsString();
    } catch (Exception e) {
      return "";
    }
  }

  /** Logs in (password = name + "-pass") and returns the access token. */
  String token(String user) {
    var r = call(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
        .content("{\"username\":\"" + user + "\",\"password\":\"" + user + "-pass\"}"));
    return JsonPath.read(body(r), "$.accessToken");
  }

  MockHttpServletResponse as(String user, MockHttpServletRequestBuilder rb) {
    return call(rb.header("Authorization", "Bearer " + token(user)));
  }

  MockHttpServletRequestBuilder transferRequest(String key, String from, String to, String amount) {
    return post("/transfers").header("Idempotency-Key", key).contentType(MediaType.APPLICATION_JSON)
        .content("{\"from\":\"" + from + "\",\"to\":\"" + to + "\",\"amount\":" + amount + "}");
  }

  // ---------- your tests ----------

  @Test
  @DisplayName("Task 4: a forged or tampered token is 401")
  void tamperedToken() {
    fail("Task 4: take asha's token, change one character in the middle part, call GET /accounts/ACC-1"
        + " with it, and assert 401");
  }

  @Test
  @DisplayName("Task 4: meera can't move money out of ravi's account, and his balance is unchanged")
  void customerCannotDrainOthers() {
    fail("Task 4: as meera, POST a transfer from ACC-2 to ACC-4; assert 403, then as tara read ACC-2"
        + " and assert the balance is still 500.00");
  }

  @Test
  @DisplayName("Task 4: a customer can't read the audit list or someone else's account")
  void customerCannotPeek() {
    fail("Task 4: as ravi, assert GET /audit/transfers is 403 and GET /accounts/ACC-4 is 403;"
        + " then as ravi assert his own GET /accounts/ACC-2 is 200");
  }
}
