package ledgerly;

/** Projection for the native monthly query (task 4). Column aliases must match the getters. Finished. */
public interface MonthTotal {
  String getPeriod();

  long getTotal();
}
