package ledgerly;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/** Finished; NOT one of your tasks. */
@Entity
public class Account {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 20)
  private String number;

  @Column(nullable = false)
  private long balance;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "customer_id")
  private Customer customer;

  protected Account() {}

  public Account(String number, long balance) {
    this.number = number;
    this.balance = balance;
  }

  public String getNumber() {
    return number;
  }

  public long getBalance() {
    return balance;
  }

  public Customer getCustomer() {
    return customer;
  }

  void setCustomer(Customer customer) {
    this.customer = customer;
  }
}
