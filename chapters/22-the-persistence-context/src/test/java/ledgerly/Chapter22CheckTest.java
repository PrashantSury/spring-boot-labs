package ledgerly;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import jakarta.persistence.EntityManagerFactory;
import java.util.List;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** Checks for chapter 22. NOT @Transactional: each service call is its own real transaction. */
@SpringBootTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@TestMethodOrder(MethodOrderer.MethodName.class)
class Chapter22CheckTest {

  @Autowired AccountRepository repo;
  @Autowired AccountService service;
  @Autowired EntityManagerFactory emf;

  long acc1;

  @BeforeEach
  void reset() {
    repo.deleteAll();
    acc1 = repo.save(new Account("ACC-1", "Asha Rao")).getId();
    repo.save(new Account("ACC-2", "Vikram Shah"));
  }

  private String owner(String n) {
    return repo.findByNumber(n).orElseThrow().getOwner();
  }

  private Statistics stats() {
    Statistics s = emf.unwrap(SessionFactory.class).getStatistics();
    s.clear();
    return s;
  }

  // ---------- Task 1 ----------

  @Test
  @DisplayName("Task 1: rename(ACC-1) is stored without any save() call (dirty checking)")
  void task1a_dirty() {
    service.rename("ACC-1", "Asha R. Kulkarni");
    assertThat(owner("ACC-1")).withFailMessage("[task 1: dirty checking] owner is still '%s'. Without a transaction"
        + " the loaded account is detached at once and the change is lost", owner("ACC-1")).isEqualTo("Asha R. Kulkarni");
  }

  @Test
  @DisplayName("Task 1: rename sends exactly one UPDATE")
  void task1b_oneUpdate() {
    Statistics s = stats();
    service.rename("ACC-1", "Asha R. Kulkarni");
    assertThat(s.getEntityUpdateCount()).isEqualTo(1);
  }

  // ---------- Task 2 ----------

  @Test
  @DisplayName("Task 2: findById twice in one transaction returns the SAME object")
  void task2a_same() {
    assertThat(service.loadTwiceIsSameObject(acc1)).withFailMessage("[task 2: first-level cache] two different"
        + " objects: each findById ran in its own persistence context. Add @Transactional(readOnly = true)").isTrue();
  }

  @Test
  @DisplayName("Task 2: ...and only ONE select is sent")
  void task2b_oneSelect() {
    Statistics s = stats();
    service.loadTwiceIsSameObject(acc1);
    assertThat(s.getPrepareStatementCount()).withFailMessage("[task 2: one select] %d statements were sent",
        s.getPrepareStatementCount()).isEqualTo(1);
  }

  // ---------- Task 3 ----------

  @Test
  @DisplayName("Task 3: a changed detached ACC-2 is stored by updateDetached")
  void task3a_merge() {
    Account detached = repo.findByNumber("ACC-2").orElseThrow();
    detached.setOwner("Vikram S. Shah");
    service.updateDetached(detached);
    assertThat(owner("ACC-2")).withFailMessage("[task 3: merge] owner is still '%s'", owner("ACC-2"))
        .isEqualTo("Vikram S. Shah");
  }

  @Test
  @DisplayName("Task 3: merge returns a different (managed) object, not the one passed in")
  void task3b_copy() {
    Account detached = repo.findByNumber("ACC-2").orElseThrow();
    detached.setOwner("Vikram S. Shah");
    assertThat(service.updateDetached(detached)).isNotSameAs(detached);
  }

  // ---------- Task 4 ----------

  @Test
  @DisplayName("Task 4: changing ACC-2 to ACC-1 throws DuplicateAccountException (flush inside the try)")
  void task4a_flush() {
    Throwable t = catchThrowable(() -> service.changeNumber("ACC-2", "ACC-1"));
    assertThat(t).withFailMessage("[task 4: flush] got %s. The constraint fired at commit, after your try/catch",
        t == null ? "no exception" : t.getClass().getSimpleName()).isInstanceOf(DuplicateAccountException.class);
  }

  @Test
  @DisplayName("Task 4: changing ACC-2 to ACC-3 works")
  void task4b_ok() {
    service.changeNumber("ACC-2", "ACC-3");
    assertThat(repo.findByNumber("ACC-3")).isPresent();
    assertThat(repo.findAll().stream().map(Account::getNumber).sorted().toList()).isEqualTo(List.of("ACC-1", "ACC-3"));
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
  @DisplayName("Task 5: the answer covers the key ideas (managed, detached, dirty checking, first-level, flush, merge)")
  void task5c_keyIdeas() {
    String body = answerBody().toLowerCase();
    java.util.List<String> missing = java.util.Arrays.stream(new String[] {"managed", "detached", "dirty checking", "first-level|same object", "flush", "merge"})
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
