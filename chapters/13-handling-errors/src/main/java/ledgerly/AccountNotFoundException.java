package ledgerly;

/** Thrown when an account number is unknown. Finished. */
public class AccountNotFoundException extends RuntimeException {

  public AccountNotFoundException(String number) {
    super("No account " + number);
  }
}
