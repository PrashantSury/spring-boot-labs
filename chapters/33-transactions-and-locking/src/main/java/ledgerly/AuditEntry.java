package ledgerly;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

/** Finished. */
@Entity
public class AuditEntry {
  @Id @GeneratedValue private Long id;
  private String message;

  protected AuditEntry() {}

  public AuditEntry(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }
}
