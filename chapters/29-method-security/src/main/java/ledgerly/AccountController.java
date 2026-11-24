package ledgerly;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Finished; NOT one of your tasks. Thin on purpose: the rules live on the service. */
@RestController
public class AccountController {

  private final AccountService service;

  public AccountController(AccountService service) {
    this.service = service;
  }

  @GetMapping("/accounts/{id}")
  public Account one(@PathVariable long id) {
    return service.find(id);
  }

  @GetMapping("/customers/{owner}/accounts")
  public List<Account> of(@PathVariable String owner) {
    return service.accountsOf(owner);
  }

  @PostMapping("/transfers")
  public String transfer(@RequestParam long from, @RequestParam long to, @RequestParam BigDecimal amount) {
    return service.transfer(from, to, amount);
  }

  @DeleteMapping("/accounts/{id}")
  public String close(@PathVariable long id) {
    service.close(id);
    return "closed " + id;
  }
}
