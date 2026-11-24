package ledgerly;

import java.math.BigDecimal;

/**
 * Task 3 (part 1): bind ledgerly.limits.* into this record (type-safe, validated at startup, one place to look):
 *   @ConfigurationProperties("ledgerly.limits")
 *   perTransfer default 50000 and transfersPerDay default 10:  @DefaultValue("50000") BigDecimal perTransfer, ...
 * Task 3 (part 3): in application-prod.properties set ledgerly.limits.per-transfer=200000
 *   (application.properties already sets transfers-per-day=20 for every profile).
 */
public record LimitsProperties(
    BigDecimal perTransfer,
    int transfersPerDay) {}
