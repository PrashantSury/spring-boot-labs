package ledgerly;

import java.math.BigDecimal;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.FixedDelayTask;
import org.springframework.scheduling.config.ScheduledTaskHolder;
import org.springframework.stereotype.Component;

/** Walks through one good and one rejected transfer and lists the scheduled jobs (mvn spring-boot:run). Finished. */
@Component
@ConditionalOnProperty(name = "ledgerly.tour", havingValue = "true", matchIfMissing = true)
public class EventsTour implements CommandLineRunner {

  private final TransferService transfers;
  private final Notifier notifier;
  private final EventLog eventLog;
  private final ObjectProvider<ScheduledTaskHolder> scheduled;

  public EventsTour(TransferService transfers, Notifier notifier, EventLog eventLog,
      ObjectProvider<ScheduledTaskHolder> scheduled) {
    this.transfers = transfers;
    this.notifier = notifier;
    this.eventLog = eventLog;
    this.scheduled = scheduled;
  }

  @Override
  public void run(String... args) throws Exception {
    long t = System.nanoTime();
    transfers.transfer("ACC-1", "ACC-2", new BigDecimal("500"));
    System.out.printf("transfer 500      : returned in %d ms%n", (System.nanoTime() - t) / 1_000_000);
    try {
      transfers.transfer("ACC-1", "ACC-3", new BigDecimal("250000"));
    } catch (LedgerRejected e) {
      System.out.println("transfer 250000   : " + e.getMessage() + " (rolled back)");
    }
    Thread.sleep(800);
    System.out.println("rows in transfers : " + transfers.count());
    System.out.println("events published  : " + eventLog.events().size());
    System.out.println("SMS sent          : " + notifier.sent());
    var holder = scheduled.getIfAvailable();
    if (holder == null || holder.getScheduledTasks().isEmpty()) {
      System.out.println("scheduled jobs    : none");
    } else {
      for (var st : holder.getScheduledTasks()) {
        String what = st.getTask() instanceof CronTask c ? "cron " + c.getExpression()
            : st.getTask() instanceof FixedDelayTask f ? "fixedDelay " + f.getIntervalDuration() : st.getTask().toString();
        System.out.println("scheduled job     : " + st.getTask().getRunnable() + " -> " + what);
      }
      System.out.println("(the scheduler keeps the app running for those jobs; press Ctrl+C to stop)");
    }
  }
}
