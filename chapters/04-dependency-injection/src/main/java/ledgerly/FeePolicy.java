package ledgerly;

/** How much a transfer costs. Two implementations exist: standardFees and zeroFees. */
public interface FeePolicy {
  long feeFor(long amount);
}
