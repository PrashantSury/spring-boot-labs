package ledgerly;

import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Finished; NOT one of your tasks. Login skips the password check here to keep the lab short. */
@RestController
public class BankController {

  private final TokenService tokens;
  private final UserDetailsService users;

  public BankController(TokenService tokens, UserDetailsService users) {
    this.tokens = tokens;
    this.users = users;
  }

  /** Demo login: a token for the named user (chapter 27 did the real password check). */
  @PostMapping("/auth/demo-token")
  public Map<String, String> demoToken(@RequestBody Map<String, String> body) {
    return Map.of("token", tokens.issueFor(users.loadUserByUsername(body.get("username"))));
  }

  @GetMapping("/me")
  public String me(Authentication a) {
    return a.getName() + " " + a.getAuthorities().stream().map(Object::toString).sorted().collect(Collectors.joining(",", "[", "]"));
  }

  @PostMapping("/transfers")
  public ResponseEntity<String> transfer(@RequestBody String body, Authentication a) {
    return ResponseEntity.status(201).body("transfer by " + a.getName());
  }

  @GetMapping("/reports/daily")
  public String daily() {
    return "daily report: 1,204 transfers";
  }

  @GetMapping("/teller/cash-drawer")
  public String drawer() {
    return "drawer: 2,40,000";
  }

  @GetMapping("/admin/audit")
  public String audit() {
    return "3 failed logins today";
  }
}
