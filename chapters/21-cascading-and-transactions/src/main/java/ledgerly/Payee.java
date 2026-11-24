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
public class Payee {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 40)
  private String nickname;

  @Column(nullable = false, length = 20)
  private String accountNumber;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "customer_id")
  private Customer customer;

  protected Payee() {}

  public Payee(String nickname, String accountNumber) {
    this.nickname = nickname;
    this.accountNumber = accountNumber;
  }

  public String getNickname() {
    return nickname;
  }

  void setCustomer(Customer customer) {
    this.customer = customer;
  }
}
