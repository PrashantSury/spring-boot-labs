package ledgerly;

/** CHECKED (extends Exception): does NOT roll back by default. Finished. */
public class DailyLimitExceededException extends Exception {
  public DailyLimitExceededException(long amount) {
    super("Daily limit is 5000, asked for " + amount);
  }
}
