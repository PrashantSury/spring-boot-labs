package ledgerly;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Finished; NOT one of your tasks. Notice: no try/catch anywhere. Errors are ApiErrors' job. */
@RestController
public class AccountController {

  private final Accounts accounts;

  public AccountController(Accounts accounts) {
    this.accounts = accounts;
  }

  @GetMapping("/accounts/{number}/balance")
  public long balance(@PathVariable String number) {
    return accounts.balance(number);
  }

  @PostMapping("/transfers")
  public ResponseEntity<String> transfer(@Valid @RequestBody TransferRequest t) {
    accounts.transfer(t.from(), t.to(), t.amount());
    return ResponseEntity.status(201).body("sent " + t.amount() + " from " + t.from() + " to " + t.to());
  }

  /** A bug on purpose: the statement service is not wired yet. */
  @GetMapping("/accounts/{number}/statement")
  public String statement(@PathVariable String number) {
    String service = null;
    return service.toUpperCase() + number;
  }
}
