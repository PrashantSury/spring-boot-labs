package ledgerly;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/** GET /accounts/{number}. Finished. */
@RestController
public class AccountController {

  private final TransferService service;

  public AccountController(TransferService service) {
    this.service = service;
  }

  @GetMapping("/accounts/{number}")
  public AccountDto account(@PathVariable String number) {
    return service.account(number);
  }
}
