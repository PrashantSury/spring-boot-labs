package ledgerly;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Version;
import java.math.BigDecimal;

/** An account. The money rules (debit, credit) are finished. */
@Entity
public class Account {

  @Id private Long id;

  @Column(unique = true, nullable = false)
  private String number;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private Customer customer;

  @Column(precision = 19, scale = 2, nullable = false)
  private BigDecimal balance;

  private boolean frozen;

  @Version private Long version;

  protected Account() {}

  public Account(Long id, String number, Customer customer, BigDecimal balance, boolean frozen) {
    this.id = id;
    this.number = number;
    this.customer = customer;
    this.balance = balance;
    this.frozen = frozen;
  }

  public void debit(BigDecimal amount) {
    if (balance.compareTo(amount) < 0) throw new InsufficientFunds(number);
    balance = balance.subtract(amount);
  }

  /** A frozen account can't receive money: the credit fails AFTER the debit happened. */
  public void credit(BigDecimal amount) {
    if (frozen) throw new AccountFrozen(number);
    balance = balance.add(amount);
  }

  public Long getId() {
    return id;
  }

  public String getNumber() {
    return number;
  }

  public Customer getCustomer() {
    return customer;
  }

  public BigDecimal getBalance() {
    return balance;
  }

  public void setBalance(BigDecimal balance) {
    this.balance = balance;
  }
}
