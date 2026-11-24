package ledgerly;

import java.time.LocalDate;
import java.util.List;

/** The same 7 transactions for the tour and the checks. Finished. */
public final class SampleData {

  private SampleData() {}

  public static List<Txn> txns() {
    return List.of(
        new Txn("ACC-1", "RENT", 25_000, LocalDate.of(2026, 10, 1), "Rent October"),
        new Txn("ACC-1", "GROCERY", 1_200, LocalDate.of(2026, 10, 3), "BigBasket"),
        new Txn("ACC-1", "GROCERY", 800, LocalDate.of(2026, 10, 20), "DMart"),
        new Txn("ACC-1", "RENT", 25_000, LocalDate.of(2026, 11, 1), "Rent November"),
        new Txn("ACC-1", "GROCERY", 450, LocalDate.of(2026, 11, 2), "Zepto"),
        new Txn("ACC-1", "TRAVEL", 3_200, LocalDate.of(2026, 11, 10), "IndiGo PNQ-BLR"),
        new Txn("ACC-2", "GROCERY", 300, LocalDate.of(2026, 11, 5), "Blinkit"));
  }
}
