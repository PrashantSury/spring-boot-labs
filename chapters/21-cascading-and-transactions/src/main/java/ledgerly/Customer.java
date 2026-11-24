package ledgerly;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

/** A customer and the payees (saved beneficiaries) they can send money to. */
@Entity
public class Customer {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 80)
  private String name;

  // Task 1: payees live and die with their customer.
  //   cascade = CascadeType.ALL: saving/deleting the customer saves/deletes its payees.
  //   orphanRemoval = true: a payee removed from this list is deleted from the table.
  @OneToMany(mappedBy = "customer")
  private List<Payee> payees = new ArrayList<>();

  protected Customer() {}

  public Customer(String name) {
    this.name = name;
  }

  /** Finished helper (chapter 20). */
  public void addPayee(Payee p) {
    payees.add(p);
    p.setCustomer(this);
  }

  /** Finished helper. */
  public void removePayee(String nickname) {
    payees.removeIf(p -> p.getNickname().equals(nickname));
  }

  public Long getId() {
    return id;
  }

  public List<Payee> getPayees() {
    return payees;
  }
}
