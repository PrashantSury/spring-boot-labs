package ledgerly;

import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;

/** Background jobs. The work itself is finished; task 4 schedules it. */
@Component
public class BankJobs {

  private final AtomicInteger interestRuns = new AtomicInteger();
  private final AtomicInteger sweeps = new AtomicInteger();

  // Task 4: post interest every night at 01:00 India time:
  //   @Scheduled(cron = "0 0 1 * * *", zone = "Asia/Kolkata")     (sec min hour day month weekday)
  public void postInterest() {
    interestRuns.incrementAndGet();
  }

  // Task 4: retry unsent SMS 30 s after the previous sweep FINISHED (a slow sweep must never overlap the next):
  //   @Scheduled(fixedDelay = 30_000, initialDelay = 30_000)
  public void sweepOutbox() {
    sweeps.incrementAndGet();
  }

  public int interestRuns() {
    return interestRuns.get();
  }
}
