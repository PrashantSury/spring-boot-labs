package ledgerly;

import org.springframework.stereotype.Component;

/** A finished demo. NOT one of your tasks. Premium customers pay no fee. */
@Component("zeroFees")
public class ZeroFeePolicy implements FeePolicy {

  @Override
  public long feeFor(long amount) {
    return 0;
  }
}
