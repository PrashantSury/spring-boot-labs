package ledgerly;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Transfers for premium customers: they must get the zeroFees policy. */
// Task 2 (part 2): choose a bean by name
//
// Same change as task 1 (final field, one constructor, no @Autowired), with
// one difference: this service must NOT get the default (primary) policy.
// Put @Qualifier("zeroFees") on the constructor parameter.
@Service
public class PremiumTransferService {

  @Autowired
  private ZeroFeePolicy fees;

  public Transfer transfer(String from, String to, long amount) {
    return new Transfer(from, to, amount, fees.feeFor(amount));
  }
}
