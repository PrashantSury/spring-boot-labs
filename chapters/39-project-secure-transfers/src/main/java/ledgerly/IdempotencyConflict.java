package ledgerly;

/** The same Idempotency-Key came back with a DIFFERENT request. Finished. */
public class IdempotencyConflict extends RuntimeException {

  public IdempotencyConflict(String key) {
    super("Idempotency-Key " + key + " was already used for a different transfer");
  }
}
