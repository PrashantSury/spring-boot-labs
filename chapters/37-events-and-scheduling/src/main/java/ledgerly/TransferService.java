package ledgerly;

import java.math.BigDecimal;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Books transfers. */
@Service
public class TransferService {

  static final BigDecimal LEDGER_LIMIT = new BigDecimal("100000");

  private final JdbcClient jdbc;
  private final ApplicationEventPublisher events;

  public TransferService(JdbcClient jdbc, ApplicationEventPublisher events) {
    this.jdbc = jdbc;
    this.events = events;
  }

  @Transactional
  public long transfer(String from, String to, BigDecimal amount) {
    var keys = new GeneratedKeyHolder();
    jdbc.sql("insert into transfers(from_acc, to_acc, amount) values (?, ?, ?)").params(from, to, amount)
        .update(keys, "id");
    long id = keys.getKey().longValue();

    // Task 1: tell the rest of the app WITHOUT calling the notifier here:
    //   events.publishEvent(new TransferCompleted(id, from, to, amount));

    // Finished: the ledger posting can still fail AFTER the event was published -> the whole transaction rolls back.
    if (amount.compareTo(LEDGER_LIMIT) > 0) throw new LedgerRejected();
    return id;
  }

  public int count() {
    return jdbc.sql("select count(*) from transfers").query(Integer.class).single();
  }
}
