package ledgerly;

import java.math.BigDecimal;
import java.util.List;

/** Response DTOs (never return entities). Finished. */
public final class Views {

  private Views() {}

  public record CustomerSummary(long id, String name, int accounts) {}

  public record AccountLine(String number, BigDecimal balance) {}

  public record CustomerDetail(long id, String name, List<AccountLine> accounts) {}

  public record PayeeView(long id, String name, String ifsc, String accountNumber) {

    static PayeeView of(Payee p) {
      return new PayeeView(p.getId(), p.getName(), p.getIfsc(), p.getAccountNumber());
    }
  }
}
