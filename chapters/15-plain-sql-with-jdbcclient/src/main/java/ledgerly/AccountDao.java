package ledgerly;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * All SQL for the account table, written by hand with JdbcClient.
 * JdbcClient is auto-configured from the DataSource; just ask for it in the constructor.
 */
@Repository
public class AccountDao {

  private final JdbcClient jdbc;

  public AccountDao(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  // Task 1: every account, ordered by number.
  //   jdbc.sql("select ...").query(Account.class).list()
  public List<Account> findAll() {
    return List.of();
  }

  // Task 2: one account by number, or Optional.empty().
  //   Use a NAMED parameter (:number) and .param("number", number), then .optional().
  public Optional<Account> findByNumber(String number) {
    return Optional.empty();
  }

  // Task 3: accounts whose owner CONTAINS the fragment, ordered by number.
  //   The code below works for "Asha" but glues user input into the SQL: an injection bug.
  //   Rewrite it with a named parameter. The % signs belong in the parameter VALUE.
  public List<Account> searchByOwner(String fragment) {
    return jdbc.sql("select id, number, owner, balance from account where owner like '%" + fragment + "%' order by number")
        .query(Account.class).list();
  }

  // Task 4: insert a new account and return the id the DATABASE generated.
  //   .update(keyHolder, "id") fills a GeneratedKeyHolder; keyHolder.getKey().longValue().
  //   A duplicate number must fail (the unique constraint throws DuplicateKeyException); don't catch it.
  public long insert(String number, String owner, long balance) {
    return 0;
  }

  // Task 5a: add amount to the balance. Return true if exactly one row changed,
  //   false for an unknown number. update() returns the row count; check it.
  public boolean deposit(String number, long amount) {
    return false;
  }

  // Task 5b: take amount off, but ONLY if the balance covers it, in ONE statement:
  //   ... where number = :number and balance >= :amount
  //   Return true if a row changed. No select-then-update (two requests could both pass the check).
  public boolean withdraw(String number, long amount) {
    return false;
  }
}
