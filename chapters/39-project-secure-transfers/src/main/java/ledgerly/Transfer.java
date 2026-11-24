package ledgerly;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import java.math.BigDecimal;

/** A booked transfer. idempotencyKey is unique: the database refuses a second row with the same key. Finished. */
@Entity
public class Transfer {

  @Id @GeneratedValue private Long id;

  @Column(unique = true, nullable = false)
  private String idempotencyKey;

  private String fromNumber;
  private String toNumber;

  @Column(precision = 19, scale = 2)
  private BigDecimal amount;

  protected Transfer() {}

  public Transfer(String idempotencyKey, String fromNumber, String toNumber, BigDecimal amount) {
    this.idempotencyKey = idempotencyKey;
    this.fromNumber = fromNumber;
    this.toNumber = toNumber;
    this.amount = amount;
  }

  public Long getId() {
    return id;
  }

  public String getIdempotencyKey() {
    return idempotencyKey;
  }

  public String getFromNumber() {
    return fromNumber;
  }

  public String getToNumber() {
    return toNumber;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  /** Same request = same accounts and same amount (2 decimals). */
  public boolean sameRequestAs(TransferRequest r) {
    return fromNumber.equals(r.from()) && toNumber.equals(r.to()) && amount.compareTo(r.amount()) == 0;
  }
}
