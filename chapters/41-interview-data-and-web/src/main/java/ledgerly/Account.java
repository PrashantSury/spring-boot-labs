package ledgerly;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;

/** Finished. */
@Entity
public class Account {

  @Id private Long id;
  private String number;

  @ManyToOne(fetch = FetchType.LAZY)
  private Customer customer;

  @Column(precision = 19, scale = 2)
  private BigDecimal balance;

  protected Account() {}

  public Account(Long id, String number, Customer customer, BigDecimal balance) {
    this.id = id;
    this.number = number;
    this.customer = customer;
    this.balance = balance;
  }

  public String getNumber() {
    return number;
  }

  public BigDecimal getBalance() {
    return balance;
  }
}
