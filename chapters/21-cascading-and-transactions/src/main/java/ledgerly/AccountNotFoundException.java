package ledgerly;

/** Unchecked: rolls back a @Transactional method by default. Finished. */
public class AccountNotFoundException extends RuntimeException {
  public AccountNotFoundException(String number) {
    super("No account " + number);
  }
}
