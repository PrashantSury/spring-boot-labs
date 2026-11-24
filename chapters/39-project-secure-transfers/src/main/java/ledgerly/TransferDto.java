package ledgerly;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** What the API returns for a transfer; amounts always have 2 decimals. Finished. */
public record TransferDto(long id, String from, String to, BigDecimal amount) {

  static TransferDto of(Transfer t) {
    return new TransferDto(t.getId(), t.getFromNumber(), t.getToNumber(), t.getAmount().setScale(2, RoundingMode.UNNECESSARY));
  }
}
