package ledgerly;

/** Finished. */
public class AccountFrozen extends RuntimeException {

  public AccountFrozen(String number) {
    super("account %s is frozen and can't receive money".formatted(number));
  }
}
