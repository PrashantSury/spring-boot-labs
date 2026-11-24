package ledgerly;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import java.math.BigDecimal;

/** A Ledgerly account. */
@Entity
public class Account {

  @Id @GeneratedValue private Long id;
  private String owner;

  @Column(precision = 19, scale = 2)
  private BigDecimal balance;

  // Task 1: optimistic locking. Add a version column Hibernate checks on every UPDATE:
  //   @Version private Long version;     (UPDATE ... SET version = v+1 WHERE id = ? AND version = v)

  protected Account() {}

  public Account(String owner, BigDecimal balance) {
    this.owner = owner;
    this.balance = balance;
  }

  public Long getId() {
    return id;
  }

  public String getOwner() {
    return owner;
  }

  public BigDecimal getBalance() {
    return balance;
  }

  public void setBalance(BigDecimal balance) {
    this.balance = balance;
  }

  /** Task 1: return the version field (stub: always null). */
  public Long getVersion() {
    return null;
  }
}
