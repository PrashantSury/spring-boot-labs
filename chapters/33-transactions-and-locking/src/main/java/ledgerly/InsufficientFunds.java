package ledgerly;

/** Finished. Unchecked, so it rolls back a @Transactional method. */
public class InsufficientFunds extends RuntimeException {
  public InsufficientFunds(String message) {
    super(message);
  }
}
