package ledgerly;

/** Thrown when a transfer is larger than the balance. Finished. */
public class InsufficientFundsException extends RuntimeException {

  private final long shortBy;

  public InsufficientFundsException(String number, long shortBy) {
    super("Account " + number + " is short by " + shortBy);
    this.shortBy = shortBy;
  }

  public long shortBy() {
    return shortBy;
  }
}
