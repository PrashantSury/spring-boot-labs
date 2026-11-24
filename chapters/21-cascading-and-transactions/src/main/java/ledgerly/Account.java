package ledgerly;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

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

  protected Account() {}

  public Account(String number, long balance) {
    this.number = number;
    this.balance = balance;
  }

  public void debit(long amount) {
    balance -= amount;
  }

  public void credit(long amount) {
    balance += amount;
  }

  public String getNumber() {
    return number;
  }

  public long getBalance() {
    return balance;
  }
}
