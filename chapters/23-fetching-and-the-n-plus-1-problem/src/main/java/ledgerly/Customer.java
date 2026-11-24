package ledgerly;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

/** A customer with accounts (lazy, the default for collections). */
@Entity
public class Customer {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 80)
  private String name;

  // Task 3: when code touches the accounts of many customers one by one, Hibernate sends one
  //   select PER customer. @BatchSize(size = 50) makes it load the accounts of up to 50 customers
  //   in ONE select (where customer_id in (?, ?, ...)). Do NOT switch to FetchType.EAGER.
  @OneToMany(mappedBy = "customer")
  private List<Account> accounts = new ArrayList<>();

  protected Customer() {}

  public Customer(String name) {
    this.name = name;
  }

  public void addAccount(Account a) {
    accounts.add(a);
    a.setCustomer(this);
  }

  public String getName() {
    return name;
  }

  public List<Account> getAccounts() {
    return accounts;
  }
}
