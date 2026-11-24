package ledgerly;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/** Finished; NOT one of your tasks. It knows nothing about ids, keys or timing. */
@RestController
public class LedgerController {

  private static final Logger log = LoggerFactory.getLogger(LedgerController.class);

  @GetMapping("/whoami")
  public String whoami() {
    return "cid=" + MDC.get("cid");
  }

  @GetMapping("/accounts/{number}/balance")
  public String balance(@PathVariable String number) {
    log.info("balance asked for {}", number);
    return "52000";
  }

  @GetMapping("/ops/cache-stats")
  public String cacheStats() {
    return "hits=42 misses=3";
  }
}
