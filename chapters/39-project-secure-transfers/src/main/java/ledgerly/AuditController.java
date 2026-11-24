package ledgerly;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** GET /audit/transfers: every transfer. Who may call it is decided in SecurityConfig (task 1). Finished. */
@RestController
public class AuditController {

  private final TransferRepository transfers;

  public AuditController(TransferRepository transfers) {
    this.transfers = transfers;
  }

  @GetMapping("/audit/transfers")
  public List<TransferDto> all() {
    return transfers.findAll().stream().map(TransferDto::of).toList();
  }
}
