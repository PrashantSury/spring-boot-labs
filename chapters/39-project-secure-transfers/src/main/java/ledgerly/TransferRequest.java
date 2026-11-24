package ledgerly;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/** Body of POST /transfers: {"from":"ACC-1","to":"ACC-2","amount":100.00}. Finished (chapter 38). */
public record TransferRequest(
    @NotBlank @Pattern(regexp = "ACC-\\d+", message = "must look like ACC-123")
    String from,
    @NotBlank @Pattern(regexp = "ACC-\\d+", message = "must look like ACC-123")
    String to,
    @NotNull @Positive @Digits(integer = 9, fraction = 2)
    BigDecimal amount) {

  @AssertTrue(message = "from and to must differ")
  public boolean isDifferentAccounts() {
    return from == null || !from.equals(to);
  }
}
