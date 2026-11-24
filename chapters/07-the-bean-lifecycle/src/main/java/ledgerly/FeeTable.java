package ledgerly;

import org.springframework.stereotype.Component;

/** A finished demo. NOT @Metered, so it must stay a plain object. */
@Component
public class FeeTable {

  public long feeFor(long amount) {
    return Math.max(amount / 100, 10);
  }
}
