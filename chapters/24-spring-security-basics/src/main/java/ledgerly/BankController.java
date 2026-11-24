package ledgerly;

import java.security.Principal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Finished; NOT one of your tasks. It has no security code at all: that is SecurityConfig's job. */
@RestController
public class BankController {

  @GetMapping("/public/rates")
  public String rates() {
    return "FD 1y 7.10%";
  }

  @GetMapping("/accounts/me")
  public String me(Principal principal) {
    return "accounts of " + principal.getName();
  }

  @PostMapping("/transfers")
  public ResponseEntity<String> transfer(@RequestBody String body, Principal principal) {
    return ResponseEntity.status(201).body("transfer by " + principal.getName());
  }

  @GetMapping("/admin/audit")
  public String audit() {
    return "3 failed logins today";
  }
}
