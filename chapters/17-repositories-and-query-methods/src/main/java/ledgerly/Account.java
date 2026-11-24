package ledgerly;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/** The entity from chapter 16, plus customerId and status. Finished; NOT one of your tasks. */
@Entity
@Table(name = "accounts")
public class Account {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 20)
  private String number;

  @Column(nullable = false, length = 80)
  private String owner;

  @Column(nullable = false)
  private long customerId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private AccountStatus status = AccountStatus.ACTIVE;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal balance;

  protected Account() {}

  public Account(String number, String owner, long customerId, BigDecimal balance) {
    this.number = number;
    this.owner = owner;
    this.customerId = customerId;
    this.balance = balance;
  }

  public Long getId() {
    return id;
  }

  public String getNumber() {
    return number;
  }

  public String getOwner() {
    return owner;
  }

  public long getCustomerId() {
    return customerId;
  }

  public AccountStatus getStatus() {
    return status;
  }

  public BigDecimal getBalance() {
    return balance;
  }

  @Override
  public boolean equals(Object o) {
    return this == o || o instanceof Account other && id != null && id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return Account.class.hashCode();
  }

  @Override
  public String toString() {
    return number + "(" + owner + ", cust " + customerId + ", " + status + ", " + balance + ")";
  }
}
