package ledgerly;

import org.springframework.stereotype.Component;

/** 1% of the amount, at least 10. The logic is finished. Task 2 is here. */
// Task 2 (part 1): when someone asks for "a FeePolicy", this one should win.
// There are two FeePolicy beans (standardFees and zeroFees). Mark this one
// as the default choice. ZeroFeePolicy stays normal.
@Component("standardFees")
public class StandardFeePolicy implements FeePolicy {

  @Override
  public long feeFor(long amount) {
    return Math.max(amount / 100, 10);
  }
}
