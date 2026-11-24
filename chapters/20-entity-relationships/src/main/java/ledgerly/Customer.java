package ledgerly;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import java.util.ArrayList;
import java.util.List;

/** A Ledgerly customer. One customer has many accounts. */
@Entity
public class Customer {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 80)
  private String name;

  // Task 2: the INVERSE side. The account table already holds customer_id (task 1), so this list
  //   must point at Account.customer with mappedBy = "customer". Without mappedBy JPA invents an
  //   extra join table customer_accounts.
  @Transient
  private List<Account> accounts = new ArrayList<>();

  protected Customer() {}

  public Customer(String name) {
    this.name = name;
  }

  // Task 3: keep BOTH sides in sync. JPA only stores what the OWNING side (Account.customer) says;
  //   adding to this list alone saves nothing. Add the account to the list AND set its customer.
  public void addAccount(Account account) {
    accounts.add(account);
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public List<Account> getAccounts() {
    return accounts;
  }
}
