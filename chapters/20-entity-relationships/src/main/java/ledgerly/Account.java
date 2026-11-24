package ledgerly;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;

/** An account belongs to exactly one customer. */
@Entity
public class Account {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 20)
  private String number;

  @Column(nullable = false)
  private long balance;

  // Task 1: the OWNING side: this table gets the foreign key column customer_id.
  //   @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "customer_id")
  //   (Replace @Transient: it tells JPA to ignore the field.)
  // Task 4: Customer -> accounts -> customer -> accounts ... is an endless loop for JSON.
  //   Put @JsonIgnore on this back-reference so a Customer serialises with its accounts, once.
  @Transient
  private Customer customer;

  protected Account() {}

  public Account(String number, long balance) {
    this.number = number;
    this.balance = balance;
  }

  public Long getId() {
    return id;
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
