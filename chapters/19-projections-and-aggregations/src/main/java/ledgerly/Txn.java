package ledgerly;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;

/** One booked transaction (amount = money spent, in rupees). Finished; NOT one of your tasks. */
@Entity
public class Txn {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 20)
  private String accountNumber;

  @Column(nullable = false, length = 20)
  private String category;

  @Column(nullable = false)
  private long amount;

  @Column(nullable = false)
  private LocalDate bookedAt;

  @Column(nullable = false, length = 80)
  private String description;

  @Column(length = 4000)
  private String rawPayload = "x".repeat(2000);

  protected Txn() {}

  public Txn(String accountNumber, String category, long amount, LocalDate bookedAt, String description) {
    this.accountNumber = accountNumber;
    this.category = category;
    this.amount = amount;
    this.bookedAt = bookedAt;
    this.description = description;
  }

  public String getAccountNumber() {
    return accountNumber;
  }

  public String getCategory() {
    return category;
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

  /** 2 KB of bank-network payload per row: the reason you don't load whole entities to read two fields. */
  public String getRawPayload() {
    return rawPayload;
  }
}
