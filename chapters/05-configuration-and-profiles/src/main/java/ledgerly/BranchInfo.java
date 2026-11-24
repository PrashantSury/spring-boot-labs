package ledgerly;

import org.springframework.stereotype.Component;

/** Shows the bank name and support phone on every statement. */
// Task 1: read two properties with @Value
//
// Take both through the constructor:
//   bankName      <- ${ledgerly.bank-name}                   (no default: it must be set)
//   supportPhone  <- ${ledgerly.support-phone:1800-123-4567} (default when the key is missing)
// Then return them from the two methods.
@Component
public class BranchInfo {


  public String bankName() {
    return null; // TODO task 1
  }

  public String supportPhone() {
    return null; // TODO task 1
  }
}
