package ledgerly;

import org.springframework.stereotype.Component;

/** Decides whether a transfer is allowed. */
// Task 4: an optional dependency
//
// Rules for allowed(account, amount):
//   - amount must be more than 0 and at most 2,00,000 (200_000 is allowed)
//   - IF a FraudScreen bean exists, a blocked account is never allowed
//
// There is usually NO FraudScreen bean. A plain FraudScreen constructor
// parameter would stop the application from starting. Ask for it in a way
// that may be empty: ObjectProvider<FraudScreen> (or Optional<FraudScreen>).
@Component
public class LimitChecker {

  static final long MAX_TRANSFER = 200_000;

  public boolean allowed(String account, long amount) {
    return false; // TODO task 4
  }
}
