package ledgerly;

import java.math.BigDecimal;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** The @Transactional story: a transfer, its audit line, and a rollback. */
@Service
public class TransferService {

  static final BigDecimal LIMIT = new BigDecimal("50000");

  private final JdbcClient jdbc;
  private final AuditWriter auditWriter;

  public TransferService(JdbcClient jdbc, AuditWriter auditWriter) {
    this.jdbc = jdbc;
    this.auditWriter = auditWriter;
  }

  @Transactional
  public void transfer(String from, String to, BigDecimal amount) {
    audit("ATTEMPT " + from + "->" + to + " " + amount);
    jdbc.sql("insert into transfers(from_acc, to_acc, amount) values (?, ?, ?)").params(from, to, amount).update();
    if (amount.compareTo(LIMIT) > 0) throw new IllegalStateException("over the limit: " + amount);
  }

  // Task 4: this LOOKS like its own transaction, but this.audit(...) never goes through the proxy,
  //   so REQUIRES_NEW is ignored and the line is rolled back with the transfer. Move the work to AuditWriter.
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void audit(String line) {
    jdbc.sql("insert into audit(line) values (?)").param(line).update();
  }

  public int count(String table) {
    return jdbc.sql("select count(*) from " + ("audit".equals(table) ? "audit" : "transfers"))
        .query(Integer.class).single();
  }

  public void clear() {
    jdbc.sql("delete from audit").update();
    jdbc.sql("delete from transfers").update();
  }
}
