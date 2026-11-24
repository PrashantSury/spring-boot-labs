package ledgerly;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.List;
import ledgerly.repo.Customer;
import ledgerly.repo.CustomerRepository;
import ledgerly.service.CustomerMapper;
import ledgerly.service.CustomerService;
import ledgerly.service.DuplicateCustomerException;
import ledgerly.web.CustomerController;
import ledgerly.web.CustomerResponse;
import ledgerly.web.OpenCustomerRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Checks for chapter 10. Tasks 1, 2 and 4 test plain Java (no Spring, no
 * MockMvc): that is the point of a service layer. Task 3 uses MockMvc.
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
class Chapter10CheckTest {

  private static final Customer ASHA = new Customer(101, "Asha Rao", "ABCPR1234F", "4821", "VIP", List.of());

  // ---------- Task 1: DTO + mapper ----------

  @Test
  @DisplayName("Task 1: CustomerResponse has exactly id, name, maskedPan")
  void task1a_components() {
    List<String> names = Arrays.stream(CustomerResponse.class.getRecordComponents()).map(RecordComponent::getName).toList();
    assertThat(names).withFailMessage("[task 1: DTO] expected the components [id, name, maskedPan] but got %s", names)
        .containsExactly("id", "name", "maskedPan");
  }

  @Test
  @DisplayName("Task 1: mask(ABCPR1234F) is XXXXXX234F")
  void task1b_mask() {
    assertThat(CustomerMapper.mask("ABCPR1234F")).withFailMessage("[task 1: mask] expected XXXXXX234F but got %s",
        CustomerMapper.mask("ABCPR1234F")).isEqualTo("XXXXXX234F");
  }

  @Test
  @DisplayName("Task 1: mask leaves a 4-character value alone, and masks a 5-character one to X + 4")
  void task1c_maskEdges() {
    assertThat(CustomerMapper.mask("1234")).isEqualTo("1234");
    assertThat(CustomerMapper.mask("12345")).isEqualTo("X2345");
  }

  @Test
  @DisplayName("Task 1: toResponse copies id and name and masks the pan")
  void task1d_toResponse() {
    Object dto = CustomerMapper.toResponse(ASHA);
    assertThat(dto).withFailMessage("[task 1: toResponse] returned null").isNotNull();
    assertThat(comp(dto, "id")).isEqualTo(101L);
    assertThat(comp(dto, "name")).isEqualTo("Asha Rao");
    assertThat(comp(dto, "maskedPan")).isEqualTo("XXXXXX234F");
  }

  // ---------- Task 2: rules in the service ----------

  @Test
  @DisplayName("Task 2: open() trims the name, upper-cases the pan and returns the DTO (plain Java, no Spring)")
  void task2a_open() {
    Object dto = new CustomerService(new CustomerRepository()).open(new OpenCustomerRequest("  Ravi Kumar ", "bxkpk4321l", "1234"));
    assertThat(dto).withFailMessage("[task 2: open] returned null").isNotNull();
    assertThat(comp(dto, "name")).isEqualTo("Ravi Kumar");
    assertThat(comp(dto, "maskedPan")).isEqualTo("XXXXXX321L");
    assertThat((Long) comp(dto, "id")).isGreaterThan(101L);
  }

  @Test
  @DisplayName("Task 2: blank name, bad PAN and a 3-digit pin are refused with IllegalArgumentException")
  void task2b_rules() {
    CustomerService s = new CustomerService(new CustomerRepository());
    assertThatThrownBy(() -> s.open(new OpenCustomerRequest("  ", "BXKPK4321L", "1234")))
        .isInstanceOf(IllegalArgumentException.class).hasMessage("name is required");
    assertThatThrownBy(() -> s.open(new OpenCustomerRequest("Ravi", "BXKPK432", "1234")))
        .isInstanceOf(IllegalArgumentException.class).hasMessage("invalid PAN");
    assertThatThrownBy(() -> s.open(new OpenCustomerRequest("Ravi", "BXKPK4321L", "123")))
        .isInstanceOf(IllegalArgumentException.class).hasMessage("pin must be 4 digits");
  }

  @Test
  @DisplayName("Task 2: a PAN that is already registered throws DuplicateCustomerException")
  void task2c_duplicate() {
    CustomerService s = new CustomerService(new CustomerRepository());
    assertThatThrownBy(() -> s.open(new OpenCustomerRequest("Asha R", "abcpr1234f", "1111")))
        .withFailMessage("[task 2: duplicate] Asha's PAN ABCPR1234F exists (lower case too)")
        .isInstanceOf(DuplicateCustomerException.class);
  }

  // ---------- Task 3: thin controller ----------

  @Test
  @DisplayName("Task 3: CustomerController takes a CustomerService, not the repository")
  void task3a_dependsOnService() {
    Constructor<?>[] cs = CustomerController.class.getConstructors();
    List<Class<?>> params = Arrays.stream(cs).flatMap(c -> Arrays.stream(c.getParameterTypes())).toList();
    assertThat(params).withFailMessage("[task 3: layers] the controller's constructor takes %s."
        + " It must take CustomerService and never CustomerRepository", params)
        .contains(CustomerService.class).doesNotContain(CustomerRepository.class);
  }

  @Test
  @DisplayName("Task 3: POST /customers answers 201 + Location + {id, name, maskedPan} only")
  void task3b_post() {
    MvcResult r = call(post("/customers").contentType("application/json")
        .content("{\"name\":\"Ravi Kumar\",\"pan\":\"BXKPK4321L\",\"pin\":\"1234\"}"));
    assertThat(r.getResponse().getStatus()).withFailMessage("[task 3: POST] expected 201 but got %d",
        r.getResponse().getStatus()).isEqualTo(201);
    assertThat(r.getResponse().getHeader("Location")).matches("/customers/\\d+");
    assertThat(body(r)).matches("\\{\"id\":\\d+,\"name\":\"Ravi Kumar\",\"maskedPan\":\"XXXXXX321L\"}");
  }

  @Test
  @DisplayName("Task 3: GET /customers/101 never leaks pin, internalNotes or the raw PAN")
  void task3c_noLeak() {
    MvcResult r = call(get("/customers/101"));
    assertThat(r.getResponse().getStatus()).isEqualTo(200);
    assertThat(body(r)).withFailMessage("[task 3: leak] the JSON is %s", body(r))
        .doesNotContain("4821").doesNotContain("VIP").doesNotContain("ABCPR1234F").doesNotContain("pin")
        .contains("\"maskedPan\":\"XXXXXX234F\"");
  }

  @Test
  @DisplayName("Task 3: 404 for an unknown id, 400 for a bad PAN, 409 for a duplicate PAN")
  void task3d_statuses() {
    assertThat(call(get("/customers/999")).getResponse().getStatus()).isEqualTo(404);
    assertThat(call(post("/customers").contentType("application/json")
        .content("{\"name\":\"R\",\"pan\":\"nope\",\"pin\":\"1234\"}")).getResponse().getStatus()).isEqualTo(400);
    assertThat(call(post("/customers").contentType("application/json")
        .content("{\"name\":\"R\",\"pan\":\"ABCPR1234F\",\"pin\":\"1234\"}")).getResponse().getStatus()).isEqualTo(409);
  }

  // ---------- Task 4: summary in the service ----------

  @Test
  @DisplayName("Task 4: Asha's summary is 3 transactions, 50000 in, 2000 out (plain Java)")
  void task4a_summary() {
    var s = new CustomerService(new CustomerRepository()).summary(101);
    assertThat(s).withFailMessage("[task 4: summary] expected a summary for 101").isPresent();
    assertThat(s.get().transactions()).isEqualTo(3);
    assertThat(s.get().totalIn()).isEqualTo(50_000);
    assertThat(s.get().totalOut()).withFailMessage("[task 4: totalOut] expected 2000 (a positive number) but got %d",
        s.get().totalOut()).isEqualTo(2_000);
  }

  @Test
  @DisplayName("Task 4: a new customer's summary is all zeros; an unknown id is empty")
  void task4b_edges() {
    CustomerService s = new CustomerService(new CustomerRepository());
    Object dto = s.open(new OpenCustomerRequest("Ravi", "BXKPK4321L", "1234"));
    var sum = s.summary((Long) comp(dto, "id"));
    assertThat(sum).isPresent();
    assertThat(sum.get().transactions()).isZero();
    assertThat(sum.get().totalIn()).isZero();
    assertThat(sum.get().totalOut()).isZero();
    assertThat(s.summary(999)).isEmpty();
  }

  @Test
  @DisplayName("Task 4: GET /customers/101/summary answers the JSON summary; 999 is 404")
  void task4c_endpoint() {
    MvcResult r = call(get("/customers/101/summary"));
    assertThat(body(r)).isEqualTo("{\"id\":101,\"transactions\":3,\"totalIn\":50000,\"totalOut\":2000}");
    assertThat(call(get("/customers/999/summary")).getResponse().getStatus()).isEqualTo(404);
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
  @DisplayName("Task 5: the answer covers the key ideas (dto, controller, service, repository, pin)")
  void task5c_keyIdeas() {
    String body = answerBody().toLowerCase();
    java.util.List<String> missing = java.util.Arrays.stream(new String[] {"dto", "controller", "service", "repository", "pin|entity"})
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

  // ---------- helpers ----------

  private static Object comp(Object record, String name) {
    if (record == null) {
      return fail("[task 1/2] got null instead of a CustomerResponse. Finish tasks 1 and 2 first");
    }
    for (RecordComponent rc : record.getClass().getRecordComponents()) {
      if (rc.getName().equals(name)) {
        try {
          return rc.getAccessor().invoke(record);
        } catch (Exception e) {
          return fail(e.toString());
        }
      }
    }
    return fail("[task 1: DTO] " + record.getClass().getSimpleName() + " has no component '" + name + "'");
  }

  private static MvcResult call(RequestBuilder rb) {
    Object controller;
    try {
      controller = CustomerController.class.getConstructor(CustomerService.class)
          .newInstance(new CustomerService(new CustomerRepository()));
    } catch (Exception e) {
      return fail("[task 3: constructor] CustomerController needs a public constructor (CustomerService)");
    }
    MockMvc mvc = MockMvcBuilders.standaloneSetup(controller).build();
    try {
      return mvc.perform(rb).andReturn();
    } catch (Exception e) {
      return fail("[request] the request threw " + e);
    }
  }

  private static String body(MvcResult r) {
    try {
      return r.getResponse().getContentAsString();
    } catch (Exception e) {
      return fail("cannot read body: " + e);
    }
  }
}
