package ledgerly;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

/** Checks for chapter 12. Bodies are built from one valid transfer with one field changed. */
@TestMethodOrder(MethodOrderer.MethodName.class)
class Chapter12CheckTest {

  private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new TransferController()).build();
  private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

  /** A valid body. Each test replaces one value. */
  private static String body(String from, String amount, String remark, String email, String beneficiary) {
    return "{\"from\":" + from + ",\"amount\":" + amount + ",\"remark\":" + remark + ",\"notifyEmail\":" + email
        + ",\"beneficiary\":" + beneficiary + "}";
  }

  private static final String BEN = "{\"name\":\"Vikram Shah\",\"ifsc\":\"HDFC0001234\"}";

  private static String valid() {
    return body("\"ACC-1\"", "5000", "\"rent\"", "\"asha@example.com\"", BEN);
  }

  // ---------- Task 1: @Valid + standard constraints ----------

  @Test
  @DisplayName("Task 1: a valid transfer answers 201")
  void task1a_valid() {
    MvcResult r = send(valid());
    assertThat(r.getResponse().getStatus()).isEqualTo(201);
  }

  @Test
  @DisplayName("Task 1: amount 0 and amount -5 are 400 on field 'amount'")
  void task1b_amountPositive() {
    assertField(send(body("\"ACC-1\"", "0", "null", "null", BEN)), "amount");
    assertField(send(body("\"ACC-1\"", "-5", "null", "null", BEN)), "amount");
  }

  @Test
  @DisplayName("Task 1: amount 200000 is fine, 200001 is 400 (the max is inclusive)")
  void task1c_amountMax() {
    assertThat(send(body("\"ACC-1\"", "200000", "null", "null", BEN)).getResponse().getStatus()).isEqualTo(201);
    assertField(send(body("\"ACC-1\"", "200001", "null", "null", BEN)), "amount");
  }

  @Test
  @DisplayName("Task 1: a blank or missing 'from' is 400 on field 'from'")
  void task1d_fromBlank() {
    assertField(send(body("\"  \"", "5000", "null", "null", BEN)), "from");
    assertField(send(body("null", "5000", "null", "null", BEN)), "from");
  }

  @Test
  @DisplayName("Task 1: remark of 50 chars is fine, 51 is 400; null remark is fine")
  void task1e_remark() {
    assertThat(send(body("\"ACC-1\"", "5", "\"" + "x".repeat(50) + "\"", "null", BEN)).getResponse().getStatus()).isEqualTo(201);
    assertField(send(body("\"ACC-1\"", "5", "\"" + "x".repeat(51) + "\"", "null", BEN)), "remark");
  }

  @Test
  @DisplayName("Task 1: notifyEmail 'asha.example.com' is 400; missing email is fine")
  void task1f_email() {
    assertField(send(body("\"ACC-1\"", "5", "null", "\"asha.example.com\"", BEN)), "notifyEmail");
    assertThat(send(body("\"ACC-1\"", "5", "null", "null", BEN)).getResponse().getStatus()).isEqualTo(201);
  }

  @Test
  @DisplayName("Task 1: two broken fields are BOTH reported in one 400")
  void task1g_allErrors() {
    MvcResult r = send(body("\"\"", "0", "null", "null", BEN));
    assertThat(fields(r)).withFailMessage("[task 1: all errors] expected errors on from AND amount but got %s", fields(r))
        .contains("from", "amount");
  }

  // ---------- Task 3: nested ----------

  @Test
  @DisplayName("Task 3: a missing beneficiary is 400 on 'beneficiary'")
  void task3a_beneficiaryRequired() {
    assertField(send(body("\"ACC-1\"", "5", "null", "null", "null")), "beneficiary");
  }

  @Test
  @DisplayName("Task 3: a bad IFSC inside the beneficiary is 400 on 'beneficiary.ifsc' (cascade)")
  void task3b_cascade() {
    assertField(send(body("\"ACC-1\"", "5", "null", "null", "{\"name\":\"Vikram\",\"ifsc\":\"HDFC1234\"}")),
        "beneficiary.ifsc");
  }

  @Test
  @DisplayName("Task 3: a blank beneficiary name is 400 on 'beneficiary.name'")
  void task3c_nestedName() {
    assertField(send(body("\"ACC-1\"", "5", "null", "null", "{\"name\":\"\",\"ifsc\":\"HDFC0001234\"}")),
        "beneficiary.name");
  }

  // ---------- Task 4: custom constraint ----------

  @Test
  @DisplayName("Task 4: ACC-1 and ACC-12345678 are valid account numbers")
  void task4a_valid() {
    assertThat(new AccountNumberValidator().isValid("ACC-1", null)).isTrue();
    assertThat(new AccountNumberValidator().isValid("ACC-12345678", null)).isTrue();
  }

  @Test
  @DisplayName("Task 4: ACC-, ACC-123456789, acc-12, ACC-12a and ' ACC-1' are invalid")
  void task4b_invalid() {
    for (String bad : List.of("ACC-", "ACC-123456789", "acc-12", "ACC-12a", " ACC-1")) {
      assertThat(new AccountNumberValidator().isValid(bad, null))
          .withFailMessage("[task 4: invalid] '%s' should NOT be a valid account number", bad).isFalse();
    }
  }

  @Test
  @DisplayName("Task 4: null is valid for @AccountNumber (null is @NotBlank's job)")
  void task4c_null() {
    assertThat(new AccountNumberValidator().isValid(null, null))
        .withFailMessage("[task 4: null] return true for null; @NotBlank reports a missing value").isTrue();
  }

  @Test
  @DisplayName("Task 4: 'from' = SAV-9 is 400 on 'from' with the message 'must look like ACC-12345'")
  void task4d_onRecord() {
    MvcResult r = send(body("\"SAV-9\"", "5", "null", "null", BEN));
    assertField(r, "from");
    var violations = VALIDATOR.validate(new TransferRequest("SAV-9", 5, null, null, new Beneficiary("V", "HDFC0001234")));
    assertThat(violations).extracting(v -> v.getMessage()).contains("must look like ACC-12345");
  }

  // ---------- Task 5: path variable and query parameter ----------

  @Test
  @DisplayName("Task 5: /accounts/ACC-1/statement?months=6 answers 200")
  void task5a_ok() {
    MvcResult r = call(get("/accounts/ACC-1/statement").param("months", "6"));
    assertThat(r.getResponse().getStatus()).isEqualTo(200);
  }

  @Test
  @DisplayName("Task 5: months=0 and months=13 are 400 from HandlerMethodValidationException")
  void task5b_months() {
    for (String m : List.of("0", "13")) {
      MvcResult r = call(get("/accounts/ACC-1/statement").param("months", m));
      assertThat(r.getResponse().getStatus()).withFailMessage("[task 5: months=%s] expected 400 but got %d", m,
          r.getResponse().getStatus()).isEqualTo(400);
      assertThat(r.getResolvedException()).isInstanceOf(HandlerMethodValidationException.class);
    }
  }

  @Test
  @DisplayName("Task 5: /accounts/SAV-9/statement is 400 (the path variable is validated too)")
  void task5c_path() {
    MvcResult r = call(get("/accounts/SAV-9/statement"));
    assertThat(r.getResponse().getStatus()).withFailMessage("[task 5: path] expected 400 for SAV-9 but got %d",
        r.getResponse().getStatus()).isEqualTo(400);
    assertThat(r.getResolvedException()).isInstanceOf(HandlerMethodValidationException.class);
  }

  // ---------- Task 6 (Interview) ----------

  @Test
  @DisplayName("Task 6: interview-answer.md has your answer, not the TODO line")
  void task6a_answered() {
    assertThat(answer()).withFailMessage("[task 6: interview answer] interview-answer.md still has the TODO line")
        .doesNotContain("TODO: write your answer here");
  }

  @Test
  @DisplayName("Task 6: the answer is at least 40 words")
  void task6b_longEnough() {
    int words = answerBody().isBlank() ? 0 : answerBody().split("\\s+").length;
    assertThat(words).withFailMessage("[task 6: length] your answer has %d words. Write at least 40", words)
        .isGreaterThanOrEqualTo(40);
  }

  @Test
  @DisplayName("Task 6: the answer covers the key ideas (@valid, 400, methodargumentnotvalidexception, cascade, null)")
  void task6c_keyIdeas() {
    String body = answerBody().toLowerCase();
    java.util.List<String> missing = java.util.Arrays.stream(new String[] {"@valid", "400", "methodargumentnotvalidexception", "cascade|nested", "null"})
        .filter(w -> java.util.Arrays.stream(w.split("\\|")).noneMatch(body::contains)).toList();
    assertThat(missing).withFailMessage("[task 6: key ideas] your answer should also mention: %s", missing).isEmpty();
  }

  private static String answer() {
    java.nio.file.Path file = java.nio.file.Path.of(System.getProperty("user.dir")).resolve("interview-answer.md");
    try {
      return java.nio.file.Files.readString(file);
    } catch (java.io.IOException e) {
      return org.junit.jupiter.api.Assertions.fail("[task 6] cannot read " + file.toAbsolutePath());
    }
  }

  private static String answerBody() {
    String text = answer();
    String marker = "Delete the line that says TODO.";
    int i = text.indexOf(marker);
    String body = i >= 0 ? text.substring(i + marker.length()) : text;
    return body.replace("TODO: write your answer here", "").trim();
  }

  // ---------- helpers ----------

  private MvcResult send(String json) {
    return call(post("/transfers").contentType("application/json").content(json));
  }

  private MvcResult call(RequestBuilder rb) {
    try {
      return mvc.perform(rb).andReturn();
    } catch (Exception e) {
      return fail("[request] the request threw " + e);
    }
  }

  private static List<String> fields(MvcResult r) {
    if (r.getResolvedException() instanceof MethodArgumentNotValidException e) {
      return e.getBindingResult().getFieldErrors().stream().map(FieldError::getField).toList();
    }
    return List.of();
  }

  private static void assertField(MvcResult r, String field) {
    assertThat(r.getResponse().getStatus()).withFailMessage("[validation: %s] expected 400 but got %d."
        + " Is the rule on the record, and is the body @Valid?", field, r.getResponse().getStatus()).isEqualTo(400);
    assertThat(fields(r)).withFailMessage("[validation: %s] expected a field error on '%s' but the errors are %s",
        field, field, fields(r)).contains(field);
  }
}
