package ledgerly;

import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Finished; NOT one of your tasks. */
@RestController
public class BankController {

  @GetMapping("/me")
  public String me(Authentication auth) {
    return auth.getName() + " " + auth.getAuthorities().stream().map(Object::toString).sorted()
        .collect(Collectors.joining(",", "[", "]"));
  }

  @GetMapping("/admin/audit")
  public String audit() {
    return "3 failed logins today";
  }
}
