package ledgerly;

import java.math.BigDecimal;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** POST /transfers?amount=100 -> {"status":"ok"}; above 50000 -> 422. Finished. Also POST /demo/gateway?up=false. */
@RestController
public class TransferController {

  private final TransferService transfers;
  private final PaymentsGateway gateway;

  public TransferController(TransferService transfers, PaymentsGateway gateway) {
    this.transfers = transfers;
    this.gateway = gateway;
  }

  @PostMapping("/transfers")
  public Map<String, String> transfer(@RequestParam BigDecimal amount) {
    return Map.of("status", transfers.transfer(amount));
  }

  /** Demo switch so you can watch health go DOWN and back UP with curl. */
  @PostMapping("/demo/gateway")
  public Map<String, Boolean> gateway(@RequestParam boolean up) {
    gateway.setReachable(up);
    return Map.of("reachable", up);
  }
}
