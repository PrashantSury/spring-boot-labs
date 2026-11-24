package ledgerly;

import java.security.Principal;
import java.util.NoSuchElementException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/** Finished; NOT one of your tasks. */
@RestController
public class LedgerController {

  private final Freezer freezer;

  public LedgerController(Freezer freezer) {
    this.freezer = freezer;
  }

  @GetMapping("/public/rates")
  public String rates() {
    return "FD 7.1%";
  }

  @GetMapping("/me")
  public String me(Principal p) {
    return p.getName();
  }

  @GetMapping("/accounts/{id}")
  public String account(@PathVariable long id) {
    if (id != 1) throw new NoSuchElementException("no account " + id);
    return "account 1: 82,000";
  }

  @GetMapping("/admin/audit")
  public String audit() {
    return "3 failed logins today";
  }

  /** Not under /admin, so only the method rule protects it. */
  @PostMapping("/accounts/{id}/freeze")
  public String freeze(@PathVariable long id) {
    return freezer.freeze(id);
  }

  /** Finished. */
  @Service
  public static class Freezer {
    @PreAuthorize("hasRole('ADMIN')")
    public String freeze(long id) {
      return "account " + id + " frozen";
    }
  }
}
