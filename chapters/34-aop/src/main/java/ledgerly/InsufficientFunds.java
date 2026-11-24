package ledgerly;

/** Thrown when the source account cannot cover a transfer. Finished. */
public class InsufficientFunds extends RuntimeException {

  public InsufficientFunds(String account) {
    super("insufficient funds in " + account);
  }
}
