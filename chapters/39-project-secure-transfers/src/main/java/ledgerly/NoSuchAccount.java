package ledgerly;

/** Finished. */
public class NoSuchAccount extends RuntimeException {

  public NoSuchAccount(String number) {
    super("no account %s".formatted(number));
  }
}
