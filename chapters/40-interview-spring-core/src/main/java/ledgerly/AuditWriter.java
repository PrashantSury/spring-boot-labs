package ledgerly;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

/**
 * Task 4: the audit line must SURVIVE when the transfer rolls back (auditors want failed attempts too).
 *   1. Implement write(...): insert the line into the audit table:
 *        jdbc.sql("insert into audit(line) values (?)").param(line).update();
 *   2. Run it in its OWN transaction: @Transactional(propagation = Propagation.REQUIRES_NEW)
 *   3. In TransferService, call auditWriter.write(...) instead of this.audit(...).
 */
@Component
public class AuditWriter {

  private final JdbcClient jdbc;

  public AuditWriter(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  public void write(String line) {
    throw new UnsupportedOperationException("Task 4: AuditWriter.write");
  }
}
