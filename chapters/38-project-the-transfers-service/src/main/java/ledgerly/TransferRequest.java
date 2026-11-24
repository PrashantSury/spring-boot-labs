package ledgerly;

import java.math.BigDecimal;

/**
 * Body of POST /transfers: {"from":"ACC-1","to":"ACC-2","amount":100.00}
 *
 * Task 1: validate it (the controller needs @Valid too):
 *   from, to: @NotBlank and @Pattern(regexp = "ACC-\\d+", message = "must look like ACC-123")
 *   amount:   @NotNull, @Positive, @Digits(integer = 9, fraction = 2)
 *   and the method isDifferentAccounts() with @AssertTrue(message = "from and to must differ")
 */
public record TransferRequest(
    String from,
    String to,
    BigDecimal amount) {

  public boolean isDifferentAccounts() {
    return from == null || !from.equals(to);
  }
}
