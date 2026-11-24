package ledgerly;

/**
 * Blocks accounts under investigation. Ledgerly has NO bean of this type
 * yet (the vendor is still being chosen). Some environments will add one.
 */
public interface FraudScreen {
  boolean isBlocked(String account);
}
