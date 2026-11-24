package ledgerly;

import org.springframework.beans.factory.annotation.Autowired; //@@stub
import org.springframework.stereotype.Service;

/**
 * Moves money and charges the default fee. The transfer logic is finished.
 * Your job in task 1 is HOW its two collaborators get in.
 */
// Task 1: constructor injection
//
// Today the two collaborators arrive by FIELD injection, and the fee field
// is the concrete StandardFeePolicy class. That has three problems:
//   - a plain unit test cannot build this class with `new` and a fake fee
//   - the fields cannot be final, so anything can swap them later
//   - the class depends on one implementation, not on the FeePolicy interface
//
// Change it to:
//   - two private FINAL fields: FeePolicy fees, AuditTrail audit
//   - one public constructor TransferService(FeePolicy fees, AuditTrail audit)
//   - no @Autowired anywhere (a class with one constructor does not need it)
//
// Trap: after task 1 alone, the context fails with "expected single matching
// bean but found 2: standardFees,zeroFees". That is task 2.
@Service
public class TransferService {

  @Autowired
  private StandardFeePolicy fees;
  
  @Autowired
  private AuditTrail audit;

  public Transfer transfer(String from, String to, long amount) {
    long fee = fees.feeFor(amount);
    audit.record(from + " -> " + to + " " + amount + " fee " + fee);
    return new Transfer(from, to, amount, fee);
  }
}
