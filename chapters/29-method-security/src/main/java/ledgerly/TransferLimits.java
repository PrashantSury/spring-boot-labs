package ledgerly;

import java.math.BigDecimal;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * A custom permission check as a bean, called from SpEL as @transferLimits.allowed(authentication, #amount).
 */
@Component("transferLimits")
public class TransferLimits {

  static final BigDecimal USER_LIMIT = new BigDecimal("50000");
  static final BigDecimal TELLER_LIMIT = new BigDecimal("500000");

  // Task 4 (part 1): ROLE_TELLER (or ROLE_ADMIN) may move up to 5,00,000; everyone else up to 50,000.
  //   Read the roles from auth.getAuthorities(). Amount must also be positive.
  public boolean allowed(Authentication auth, BigDecimal amount) {
    throw new UnsupportedOperationException("Task 4: TransferLimits.allowed");
  }
}
