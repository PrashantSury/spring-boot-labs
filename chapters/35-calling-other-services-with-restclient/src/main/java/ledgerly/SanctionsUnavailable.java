package ledgerly;

/** The partner is down, overloaded or too slow. retryable = worth one more try. Finished. */
public class SanctionsUnavailable extends RuntimeException {

  private final boolean retryable;

  public SanctionsUnavailable(String message, boolean retryable) {
    super(message);
    this.retryable = retryable;
  }

  public boolean retryable() {
    return retryable;
  }
}
