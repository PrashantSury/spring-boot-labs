package ledgerly;

import java.time.LocalDate;

/** One transaction as JSON. Finished. */
public record TxnView(long id, long amount, LocalDate bookedAt, String description) {

  public static TxnView of(Txn t) {
    return new TxnView(t.getId(), t.getAmount(), t.getBookedAt(), t.getDescription());
  }
}
