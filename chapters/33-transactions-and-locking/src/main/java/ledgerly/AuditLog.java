package ledgerly;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** The regulator wants EVERY withdrawal attempt recorded, including the ones that fail and roll back. */
@Service
public class AuditLog {

  private final AuditEntryRepository entries;

  public AuditLog(AuditEntryRepository entries) {
    this.entries = entries;
  }

  // Task 4: record in its OWN transaction, so it commits even when the caller rolls back:
  //   @Transactional(propagation = Propagation.REQUIRES_NEW)
  @Transactional
  public void record(String message) {
    entries.save(new AuditEntry(message));
  }
}
