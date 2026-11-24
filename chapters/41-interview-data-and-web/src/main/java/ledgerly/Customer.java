package ledgerly;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

/** A customer and their accounts (LAZY: loaded only when touched, inside a transaction). Finished. */
@Entity
public class Customer {

  @Id private Long id;
  private String name;
  private String username;

  @OneToMany(mappedBy = "customer")
  private List<Account> accounts = new ArrayList<>();

  protected Customer() {}

  public Customer(Long id, String name, String username) {
    this.id = id;
    this.name = name;
    this.username = username;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getUsername() {
    return username;
  }

  public List<Account> getAccounts() {
    return accounts;
  }
}
