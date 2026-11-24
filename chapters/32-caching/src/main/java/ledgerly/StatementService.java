package ledgerly;

import java.time.YearMonth;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;

/** Monthly statements: expensive to build. */
@Service
public class StatementService {

  private final AtomicInteger builds = new AtomicInteger();

  /** Finished: how many statements were really built. (A public field would read null through the cache proxy.) */
  public int builds() {
    return builds.get();
  }

  public void resetBuilds() {
    builds.set(0);
  }

  // Task 2: cache statements in "statements" with
  //   key       = "#accountId + ':' + #month"        (the 'format' flag must NOT be part of the key)
  //   condition = "#month.isBefore(T(java.time.YearMonth).now())"   (only CLOSED months; this month still changes)
  public List<String> statement(long accountId, YearMonth month, String format) {
    builds.incrementAndGet();
    return List.of("statement " + accountId + " " + month, "lines: 42");
  }
}
