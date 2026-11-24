package ledgerly;

/** Finished. */
public class InsufficientFunds extends RuntimeException {

  public InsufficientFunds(String number) {
    super("insufficient funds in %s".formatted(number));
  }
}
