package ledgerly;

/**
 * An interface projection: Spring Data selects ONLY these columns and backs this interface with them.
 * Finished; used by task 1.
 */
public interface TxnLine {
  String getDescription();

  long getAmount();
}
