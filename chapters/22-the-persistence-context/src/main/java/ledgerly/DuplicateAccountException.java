package ledgerly;

/** Finished. */
public class DuplicateAccountException extends RuntimeException {
  public DuplicateAccountException(String number) {
    super("Account number " + number + " is taken");
  }
}
