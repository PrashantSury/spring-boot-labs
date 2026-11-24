package ledgerly;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** Accounts. /accounts/{number} and /fee are finished. Task 1 is here. */
@RestController
public class AccountController {

  static final List<Account> ACCOUNTS = List.of(
      new Account("ACC-1", "Asha Rao", 52_000, false),
      new Account("ACC-2", "Vikram Shah", 700, false),
      new Account("ACC-3", "Meera Iyer", 0, true));

  @GetMapping("/accounts/{number}")
  public Account byNumber(@PathVariable String number) {
    return ACCOUNTS.stream().filter(a -> a.number().equals(number)).findFirst()
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "no account " + number));
  }

  /** Finished demo for the built-in 400: amount must be a whole number. */
  @GetMapping("/fee")
  public long fee(@RequestParam long amount) {
    return Math.max(amount / 100, 10);
  }

  // Task 1: a fixed path next to a pattern
  //
  // GET /accounts/dormant must answer the list of dormant account NUMBERS:
  // ["ACC-3"]. Today it lands in byNumber("dormant") and answers 404
  // "no account dormant". Add a method mapped to the exact path
  // /accounts/dormant. Spring prefers the more specific mapping, so you do
  // not need to change byNumber.
}
