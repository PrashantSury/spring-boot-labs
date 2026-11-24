package ledgerly;

import java.math.BigDecimal;
import java.time.Duration;

/** The transfer rules, from the keys ledgerly.transfer.* */
// Task 2: a validated @ConfigurationProperties record
//
//   - bind it to the prefix "ledgerly.transfer"
//     (daily-limit -> dailyLimit, fee-percent -> feePercent, otp-validity -> otpValidity)
//   - validate it, so a bad value STOPS the application at startup:
//       dailyLimit   must be set and more than 0
//       feePercent   must be set, between 0.0 and 5.0
//       otpValidity  must be set
//   - add the three keys to application.properties (see the comment there)
//   - add @ConfigurationPropertiesScan to LedgerlyApplication
public record TransferProperties(
    Long dailyLimit,
    BigDecimal feePercent,
    Duration otpValidity) {}
