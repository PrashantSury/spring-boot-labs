package ledgerly;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Accounts in memory. GET /accounts/{number} is finished. Tasks 1, 2, 3 and 5
 * are here; each method already has the right return type.
 */
@RestController
public class AccountController {

  static final LocalDate TODAY = LocalDate.of(2026, 11, 24);

  static final List<TransactionView> TRANSACTIONS = List.of(
      new TransactionView("T1", LocalDate.of(2026, 11, 1), 50_000, "CBS-99812"),
      new TransactionView("T2", LocalDate.of(2026, 11, 3), -1_200, "CBS-99840"),
      new TransactionView("T3", LocalDate.of(2026, 11, 20), -800, "CBS-99977"));

  private final Map<String, Account> accounts = new LinkedHashMap<>(Map.of(
      "ACC-1", new Account("ACC-1", "Asha Rao", 52_000),
      "ACC-2", new Account("ACC-2", "Vikram Shah", 700),
      "ACC-3", new Account("ACC-3", "Meera Iyer", 0)));

  @GetMapping("/accounts/{number}")
  public ResponseEntity<Account> byNumber(@PathVariable String number) {
    Account a = accounts.get(number);
    return a == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(a);
  }

  // Task 1: POST /accounts creates an account
  //
  //   body {"holder":"Asha Rao","openingBalance":5000}
  //   -> 201 Created
  //      Location: /accounts/ACC-4        (the next free number: ACC-4, ACC-5, ...)
  //      body {"number":"ACC-4","holder":"Asha Rao","balance":5000}
  //   a blank or missing holder, or a negative openingBalance -> 400 with no body
  //
  // Map it, read the body with @RequestBody, store it, and build the answer
  // with ResponseEntity.created(URI.create(...)).body(...).
  public ResponseEntity<Account> open(OpenAccountRequest request) {
    return null; // TODO task 1
  }

  // Task 2: DELETE /accounts/{number}
  //
  //   known number   -> 204 No Content, no body, and the account is gone
  //   unknown number -> 404
  public ResponseEntity<Void> close(String number) {
    return null; // TODO task 2
  }

  // Task 3: GET /accounts/{number}/transactions
  //
  //   ?from=2026-11-02   optional ISO date: only transactions ON or after it
  //   ?limit=2           optional, default 10: at most that many
  //   header X-Request-Id (optional): if sent, send it back as a response header
  //   (every account shares the same sample TRANSACTIONS list)
  //
  // Use @RequestParam(required = false) with @DateTimeFormat(iso = ISO.DATE)
  // for from, @RequestParam(defaultValue = "10") for limit, and
  // @RequestHeader(name = "X-Request-Id", required = false).
  public ResponseEntity<List<TransactionView>> transactions(String number, LocalDate from, int limit,
      String requestId) {
    return null; // TODO task 3
  }

  // Task 5: GET /accounts/{number}/balance
  //
  //   known  -> 200, header Cache-Control: no-store (balances must never be cached),
  //             body {"number":"ACC-1","balance":52000,"asOf":"2026-11-24"} (use TODAY)
  //   unknown -> 404 with no body
  public ResponseEntity<Balance> balance(String number) {
    return null; // TODO task 5
  }
}
