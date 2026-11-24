package ledgerly;

/** The ledger refuses to post a transfer above 100,000 (it happens AFTER the event was published). Finished. */
public class LedgerRejected extends RuntimeException {

  public LedgerRejected() {
    super("ledger rejected the posting");
  }
}
