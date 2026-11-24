package ledgerly;

/** Finished. */
public class DuplicatePayee extends RuntimeException {

  public DuplicatePayee(String accountNumber) {
    super("a payee with account " + accountNumber + " already exists");
  }
}
