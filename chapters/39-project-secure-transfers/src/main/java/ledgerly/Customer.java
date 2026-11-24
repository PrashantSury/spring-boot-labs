package ledgerly;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/** A bank customer. Finished. */
@Entity
public class Customer {

  @Id private Long id;
  private String name;

  /** The login name of this customer (links a JWT "sub" to the customer). */
  private String username;

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
}
