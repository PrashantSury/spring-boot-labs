package ledgerly;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

/** Someone a customer can send money to. accountNumber is unique. Finished. */
@Entity
public class Payee {

  @Id @GeneratedValue private Long id;
  private String name;
  private String ifsc;

  @Column(unique = true)
  private String accountNumber;

  protected Payee() {}

  public Payee(String name, String ifsc, String accountNumber) {
    this.name = name;
    this.ifsc = ifsc;
    this.accountNumber = accountNumber;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getIfsc() {
    return ifsc;
  }

  public String getAccountNumber() {
    return accountNumber;
  }
}
