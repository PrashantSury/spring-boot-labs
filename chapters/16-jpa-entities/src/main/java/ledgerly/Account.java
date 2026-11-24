package ledgerly;

import java.math.BigDecimal;

/**
 * A Ledgerly account, stored by JPA in the table "accounts".
 *
 * <p>Task 1: make it an entity.
 *   - @Entity and @Table(name = "accounts")
 *   - id: @Id @GeneratedValue(strategy = GenerationType.IDENTITY) (the database picks it)
 *   - number: not null, unique, length 20.   owner: not null, length 80.
 *   - JPA creates objects with a no-arg constructor: add a protected one.
 */
public class Account {

  private Long id;

  private String number;

  private String owner;

  // Task 2: store the NAME of the enum ("SAVINGS"), not its position (0).
  private AccountType type;

  // Task 3: money is BigDecimal (never double). Column: not null, precision 19, scale 2.
  private BigDecimal balance;


  public Account(String number, String owner, AccountType type, BigDecimal balance) {
    this.number = number;
    this.owner = owner;
    this.type = type;
    this.balance = balance;
  }

  public void deposit(BigDecimal amount) {
    balance = balance.add(amount);
  }

  public Long getId() {
    return id;
  }

  public String getNumber() {
    return number;
  }

  public String getOwner() {
    return owner;
  }

  public AccountType getType() {
    return type;
  }

  public BigDecimal getBalance() {
    return balance;
  }

  // Task 4: equals and hashCode that survive persisting.
  //   equals: same class and the SAME NON-NULL id. Two unsaved accounts (id null) are never equal
  //           unless they are the same object.
  //   hashCode: getClass().hashCode(), a constant, so it does not change when persist() sets the id
  //             (an id-based hashCode would "lose" the object inside a HashSet).

  @Override
  public String toString() {
    return "Account[id=" + id + ", " + number + ", " + owner + ", " + type + ", " + balance + "]";
  }
}
