package ledgerly;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;

/** One booked transaction. Finished; NOT one of your tasks. */
@Entity
public class Txn {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 20)
  private String accountNumber;

  @Column(nullable = false)
  private long amount;

  @Column(nullable = false)
  private LocalDate bookedAt;

  @Column(nullable = false, length = 80)
  private String description;

  protected Txn() {}

  public Txn(String accountNumber, long amount, LocalDate bookedAt, String description) {
    this.accountNumber = accountNumber;
    this.amount = amount;
    this.bookedAt = bookedAt;
    this.description = description;
  }

  public Long getId() {
    return id;
  }

  public String getAccountNumber() {
    return accountNumber;
  }

  public long getAmount() {
    return amount;
  }

  public LocalDate getBookedAt() {
    return bookedAt;
  }

  public String getDescription() {
    return description;
  }
}
